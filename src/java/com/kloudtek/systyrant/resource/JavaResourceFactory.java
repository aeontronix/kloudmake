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
import java.util.*;

import static com.kloudtek.systyrant.resource.JavaResourceFactory.AnnotationType.*;
import static com.kloudtek.util.StringUtils.isEmpty;
import static com.kloudtek.util.StringUtils.isNotEmpty;

public class JavaResourceFactory extends ResourceFactory {
    private static final Logger logger = LoggerFactory.getLogger(JavaResourceFactory.class);
    private final Class<?> clazz;
    private HashMap<Stage, List<ActionMethod>> actionMethods = new HashMap<>();
    private HashMap<Stage, List<ActionMethod>> postChildrenActionMethods = new HashMap<>();
    private HashMap<Field, String> attrInject = new HashMap<>();
    private HashMap<Field, String> serviceInject = new HashMap<>();
    private HashMap<String, String> defaultParamValues = new HashMap<>();

    public JavaResourceFactory(Class<?> clazz, FQName fqname, HashMap<String, String> packageMappings) throws InvalidResourceDefinitionException {
        super(findFQName(clazz, fqname, packageMappings));
        this.clazz = clazz;
        if (clazz.getAnnotation(Unique.class) != null) {
            setUnique(true);
        }
        for (Method method : clazz.getDeclaredMethods()) {
            registerActions(method, Stage.PREPARE, Prepare.class);
            registerActions(method, Stage.EXECUTE, Verify.class);
            registerActions(method, Stage.EXECUTE, Sync.class);
            registerActions(method, Stage.EXECUTE, Execute.class);
            registerActions(method, Stage.CLEANUP, Cleanup.class);
        }
        sort(actionMethods);
        for (Field field : clazz.getDeclaredFields()) {
            Attr attr = field.getAnnotation(Attr.class);
            if (attr != null) {
                field.setAccessible(true);
                String name = attr.value().isEmpty() ? field.getName() : attr.value();
                attrInject.put(field, name);
                if (isNotEmpty(attr.def())) {
                    defaultParamValues.put(name, attr.def());
                }
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

    private void sort(HashMap<Stage, List<ActionMethod>> map) {
        for (List<ActionMethod> methods : map.values()) {
            Collections.sort(methods, new Comparator<ActionMethod>() {
                @Override
                public int compare(ActionMethod o1, ActionMethod o2) {
                    if (o1.type == VERIFY && o2.type == SYNC) {
                        return -1;
                    } else if (o1.type == SYNC && o2.type == VERIFY) {
                        return 1;
                    } else {
                        return o1.order - o2.order;
                    }
                }
            });
        }
    }

    private void registerActions(Method method, Stage stage, Class<? extends Annotation> annotationClass) throws InvalidResourceDefinitionException {
        Annotation actionAnnotation = method.getAnnotation(annotationClass);
        if (actionAnnotation != null) {
            method.setAccessible(true);
            boolean postChildren = false;
            int order;
            if (actionAnnotation instanceof Prepare) {
                order = ((Prepare) actionAnnotation).order();
            } else if (actionAnnotation instanceof Verify) {
                Verify anno = (Verify) actionAnnotation;
                order = anno.order();
                postChildren = anno.postChildren();
                if (!Boolean.class.equals(method.getReturnType()) && !boolean.class.equals(method.getReturnType())) {
                    throw new InvalidResourceDefinitionException("@Verify method " + method.getDeclaringClass().getName() + "#" + method.getName() + " doesn't return a boolean");
                }
            } else if (actionAnnotation instanceof Sync) {
                Sync anno = (Sync) actionAnnotation;
                order = anno.order();
                postChildren = anno.postChildren();
            } else if (actionAnnotation instanceof Execute) {
                Execute anno = (Execute) actionAnnotation;
                order = anno.order();
                postChildren = anno.postChildren();
            } else if (actionAnnotation instanceof Cleanup) {
                order = ((Cleanup) actionAnnotation).order();
            } else {
                throw new RuntimeException("Invalid annotation class: " + actionAnnotation.getClass());
            }
            getActionMethods(stage, postChildren).add(new ActionMethod(method, actionAnnotation, order));
        }
    }

    @NotNull
    @Override
    public Resource create(STContext context) throws ResourceCreationException {
        Resource resource = new Resource(context, this);
        try {
            Object impl = clazz.newInstance();
            resource.addAction(new JavaImpl(impl));
            for (Map.Entry<String, String> entry : defaultParamValues.entrySet()) {
                resource.set(entry.getKey(), entry.getValue());
            }
        } catch (InstantiationException | IllegalAccessException e) {
            throw new ResourceCreationException("Java resource cannot be instantiated: " + clazz.getName(), e);
        } catch (InvalidAttributeException e) {
            throw new ResourceCreationException("Error while creating java resource " + clazz.getName() + ": " + e.getMessage(), e);
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
        private int order;
        private AnnotationType type;

        public ActionMethod(Method method, Annotation actionAnnotation, int order) {
            this.method = method;
            this.annotation = actionAnnotation;
            this.order = order;
            if (annotation instanceof Verify) {
                type = VERIFY;
            } else if (annotation instanceof Sync) {
                type = SYNC;
            } else if (annotation instanceof Execute) {
                type = EXECUTE;
            } else {
                type = OTHER;
            }
        }

        public String getClassAndMethodName() {
            return method.getDeclaringClass().getName() + "#" + method.getName();
        }
    }

    public enum AnnotationType {
        VERIFY, SYNC, EXECUTE, OTHER
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
