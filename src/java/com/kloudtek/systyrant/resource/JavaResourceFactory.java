/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.systyrant.resource;

import com.kloudtek.systyrant.FQName;
import com.kloudtek.systyrant.STAction;
import com.kloudtek.systyrant.STContext;
import com.kloudtek.systyrant.Stage;
import com.kloudtek.systyrant.annotation.*;
import com.kloudtek.systyrant.exception.*;
import com.kloudtek.systyrant.service.ServiceManager;
import com.kloudtek.systyrant.service.host.Host;
import com.kloudtek.util.validation.ValidationUtils;
import org.apache.commons.beanutils.ConvertUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.kloudtek.util.StringUtils.isEmpty;
import static com.kloudtek.util.StringUtils.isNotEmpty;

public class JavaResourceFactory extends ResourceFactory {
    private static final Logger logger = LoggerFactory.getLogger(JavaResourceFactory.class);
    private final Class<?> clazz;
    private HashMap<Stage, List<ActionMethod>> actionMethods = new HashMap<>();
    private HashMap<Stage, List<ActionMethod>> postChildrenActionMethods = new HashMap<>();
    private HashMap<Field, String> attrInject = new HashMap<>();
    private HashMap<Field, String> serviceInject = new HashMap<>();

    public JavaResourceFactory(Class<?> clazz, FQName fqname, HashMap<String, String> packageMappings) throws InvalidResourceDefinitionException {
        super(findFQName(clazz, fqname, packageMappings));
        this.clazz = clazz;
        if (clazz.getAnnotation(Unique.class) != null) {
            setUnique(true);
        }
        for (Method method : clazz.getDeclaredMethods()) {
            registerActions(method, Stage.PREPARE, Prepare.class);
            registerActions(method, Stage.VERIFY, Verify.class);
            registerActions(method, Stage.SYNC, Sync.class);
            registerActions(method, Stage.EXECUTE, Execute.class);
            registerActions(method, Stage.CLEANUP, Cleanup.class);
        }
        for (Field field : clazz.getDeclaredFields()) {
            Attr attr = field.getAnnotation(Attr.class);
            if (attr != null) {
                field.setAccessible(true);
                attrInject.put(field, attr.value().isEmpty() ? field.getName() : attr.value());
            }
            javax.annotation.Resource rs = field.getAnnotation(javax.annotation.Resource.class);
            if (rs != null) {
                field.setAccessible(true);
                serviceInject.put(field, rs.name());
            }
            Service s = field.getAnnotation(Service.class);
            if (s != null) {
                field.setAccessible(true);
                serviceInject.put(field, s.value());
            }
        }
    }

    private void registerActions(Method method, Stage stage, Class<? extends Annotation> annotationClass) throws InvalidResourceDefinitionException {
        Annotation actionAnnotation = method.getAnnotation(annotationClass);
        if (actionAnnotation != null) {
            method.setAccessible(true);
            boolean postChildren = false;
            if (actionAnnotation instanceof Execute) {
                postChildren = ((Execute) actionAnnotation).postChildren();
            } else if (actionAnnotation instanceof Verify) {
                postChildren = ((Verify) actionAnnotation).postChildren();
                if (!Boolean.class.equals(method.getReturnType()) && !boolean.class.equals(method.getReturnType())) {
                    throw new InvalidResourceDefinitionException("@Verify method " + method.getDeclaringClass().getName() + "#" + method.getName() + " doesn't return a boolean");
                }
            } else if (actionAnnotation instanceof Sync) {
                postChildren = ((Sync) actionAnnotation).postChildren();
            }
            getActionMethods(stage, postChildren).add(new ActionMethod(method, actionAnnotation));
        }
    }

    @NotNull
    @Override
    public Resource create(STContext context) throws ResourceCreationException {
        Resource resource = new Resource(context, this);
        try {
            Object impl = clazz.newInstance();
            resource.addAction(new JavaImpl(impl));
        } catch (InstantiationException | IllegalAccessException e) {
            throw new ResourceCreationException(e.getMessage(), e);
        }
        return resource;
    }

    public List<ActionMethod> getActionMethods(Stage stage, boolean postChildren) {
        final HashMap<Stage, List<ActionMethod>> map = postChildren ? postChildrenActionMethods : actionMethods;
        List<ActionMethod> methods = map.get(stage);
        if (methods == null) {
            methods = new ArrayList<>();
            map.put(stage, methods);
        }
        return methods;
    }

    public class ActionMethod {
        private Method method;
        private Annotation annotation;

        public ActionMethod(Method method, Annotation actionAnnotation) {
            this.method = method;
            this.annotation = actionAnnotation;
        }

        public String getClassAndMethodName() {
            return method.getDeclaringClass().getName() + "#" + method.getName();
        }
    }

    public class JavaImpl implements STAction {
        private Object impl;

        public JavaImpl(Object impl) {
            this.impl = impl;
        }

        public Object getImpl() {
            return impl;
        }

        @Override
        public void execute(Resource resource, Stage stage, boolean postChildren) throws STRuntimeException {
            for (ActionMethod actionMethod : getActionMethods(stage, postChildren)) {
                try {
                    if (actionMethod.annotation instanceof Sync) {
                        String type = ((Sync) actionMethod.annotation).value();
                        if (resource.containsVerification(type)) {
                            logger.debug("Skipping sync method " + actionMethod.getClassAndMethodName() + " since verification was successful");
                            continue;
                        }
                    }
                    inject(resource);
                    try {
                        ValidationUtils.validate(impl, ResourceValidationException.class);
                    } catch (ResourceValidationException e) {
                        throw new ResourceValidationException("Failed to validate '" + resource.toString() + "' " + e.getMessage());
                    }
                    Object ret = actionMethod.method.invoke(impl);
                    if (actionMethod.annotation instanceof Verify) {
                        String type = ((Verify) actionMethod.annotation).value();
                        assert ret instanceof Boolean : "verify method didn't return a boolean";
                        if (((Boolean) ret)) {
                            resource.addVerification(type);
                        }
                    }
                    updateAttrs(resource);
                } catch (IllegalAccessException e) {
                    throw new STRuntimeException(e.getMessage(), e);
                } catch (InvocationTargetException e) {
                    throw new STRuntimeException(e.getTargetException().getMessage(), e.getTargetException());
                }
            }
        }

        @SuppressWarnings("unchecked")
        private void inject(Resource element) throws STRuntimeException, IllegalAccessException {
            STContext ctx = STContext.get();
            for (Map.Entry<Field, String> entry : attrInject.entrySet()) {
                Field field = entry.getKey();
                Class<?> fieldType = field.getType();
                String attrName = entry.getValue();
                String attrVal = element.get(attrName);
                if (attrVal != null) {
                    Object value;
                    if (isEmpty(attrVal)) {
                        if (fieldType.isPrimitive()) {
                            value = ConvertUtils.convert(0, fieldType);
                        } else {
                            value = null;
                        }
                    } else {
                        if (fieldType.isEnum()) {
                            try {
                                value = Enum.valueOf((Class<? extends Enum>) fieldType, attrVal.toUpperCase());
                            } catch (IllegalArgumentException e) {
                                throw new STRuntimeException("Unable to bind attribute '" + attrName + "' of resource " + element.toString() + " to java field " + clazz.getName() + "#" + field.getName());
                            }
                        } else {
                            value = ConvertUtils.convert(attrVal, fieldType);
                        }
                    }
                    field.set(impl, value);
                }
            }
            for (Map.Entry<Field, String> entry : serviceInject.entrySet()) {
                Field field = entry.getKey();
                String rsName = entry.getValue();
                Class<?> rsClass = field.getType();
                Object resource;
                if (rsClass.isInstance(STContext.class)) {
                    resource = STContext.get();
                } else if (rsClass.isAssignableFrom(Host.class)) {
                    resource = STContext.get().host();
                } else if (rsClass.isAssignableFrom(ResourceManagerImpl.class)) {
                    resource = STContext.get().getResourceManager();
                } else if (rsClass.isAssignableFrom(ServiceManager.class)) {
                    resource = STContext.get().getServiceManager();
                } else if (rsClass.equals(Resource.class)) {
                    resource = element;
                } else {
                    ServiceManager sm = ctx.getServiceManager();
                    resource = isEmpty(rsName) ? sm.getService(rsClass) : sm.getService(rsName);
                }
                field.set(impl, resource);
            }
        }

        private void updateAttrs(Resource resource) throws IllegalAccessException, InvalidAttributeException {
            for (Map.Entry<Field, String> entry : attrInject.entrySet()) {
                Field field = entry.getKey();
                String attrName = entry.getValue();
                Object newValObj = field.get(impl);
                String newVal = ConvertUtils.convert(newValObj);
                String oldVal = resource.get(attrName);
                // If old val was null and new val is boolean false, don't prepareForExecution it
                if (oldVal == null && Boolean.FALSE.equals(newValObj)) {
                    newVal = null;
                }
                if ((newVal == null && oldVal != null) || (newVal != null && !newVal.equals(oldVal))) {
                    logger.debug("Updating resource {}'s attribute {} to {}", resource, attrName, newVal);
                    resource.set(attrName, newVal);
                }
            }
        }

    }

    private static FQName findFQName(Class<?> clazz, FQName fqname, HashMap<String, String> packageMappings) throws InvalidResourceDefinitionException {
        if (fqname != null && fqname.getPkg() != null) {
            return fqname;
        }
        STResource rsAnno = clazz.getAnnotation(STResource.class);
        String annoVal = rsAnno != null ? rsAnno.value() : null;
        int annoValSep = annoVal.indexOf(":");
        String pkg = fqname != null ? fqname.getPkg() : null;
        if (isEmpty(pkg)) {
            if (annoVal != null && annoValSep != -1) {
                pkg = annoValSep == -1 ? annoVal : annoVal.substring(0, annoValSep);
            } else if (packageMappings != null) {
                String jpkg = clazz.getPackage().getName();
                String mappedPkg = packageMappings.get(jpkg);
                if (mappedPkg != null) {
                    pkg = mappedPkg;
                } else {
                    pkg = jpkg;
                }
            }
        }
        String name = fqname != null ? fqname.getName() : null;
        if (isEmpty(name) && isNotEmpty(annoVal)) {
            name = annoValSep == -1 ? annoVal : annoVal.substring(annoValSep + 1, annoVal.length());
        }
        if (isEmpty(name)) {
            name = clazz.getSimpleName().toLowerCase();
            if (name.endsWith("resource")) {
                name = name.substring(0, name.length() - 8);
            }
        }
        if (pkg == null || name == null) {
            throw new InvalidResourceDefinitionException("Unable to identity package and name for class " + clazz.getName());
        }
        return new FQName(pkg, name);
    }
}
