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
    private HashMap<String, String> defaultAttrs = new HashMap<>();
    private Injector[] injectors;
    private HashMap<Stage, List<ActionMethod>> actionMethods = new HashMap<>();
    private HashMap<Stage, List<ActionMethod>> postChildrenActionMethods = new HashMap<>();
    private HashMap<Field, String> attrInject = new HashMap<>();

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
        ArrayList<Injector> injectorsList = new ArrayList<>();
        for (Field field : clazz.getDeclaredFields()) {
            for (Annotation annotation : field.getAnnotations()) {
                registerFieldInjection(injectorsList, field, annotation);
            }
        }
        injectors = injectorsList.toArray(new Injector[injectorsList.size()]);
    }

    private void registerFieldInjection(ArrayList<Injector> injectorsList, Field field, Annotation annotation) {
        if (annotation instanceof Attr) {
            Attr attr = (Attr) annotation;
            String name = attr.value().isEmpty() ? field.getName() : attr.value();
            injectorsList.add(new AttrInjector(name, field));
            if (isNotEmpty(attr.def())) {
                defaultAttrs.put(name, attr.def());
            }
        } else if (annotation instanceof Service) {
            injectorsList.add(new ServiceInjector(field, (Service) annotation));
        } else if (annotation instanceof Inject) {
            injectorsList.add(new GenericInjector(field));
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

    @NotNull
    @Override
    public Resource create(STContext context) throws ResourceCreationException {
        Resource resource = new Resource(context, this);
        try {
            Object impl = clazz.newInstance();
            resource.addAction(new JavaImpl(impl));
            for (Map.Entry<String, String> entry : defaultAttrs.entrySet()) {
                resource.set(entry.getKey(), entry.getValue());
            }
        } catch (InstantiationException | IllegalAccessException e) {
            throw new ResourceCreationException("Java resource cannot be instantiated: " + clazz.getName(), e);
        } catch (InvalidAttributeException e) {
            throw new ResourceCreationException("Error while creating java resource " + clazz.getName() + ": " + e.getMessage(), e);
        }
        return resource;
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
            STContext ctx = STContext.get();
            for (ActionMethod actionMethod : getActionMethods(stage, postChildren)) {
                try {
                    if (actionMethod.annotation instanceof Sync) {
                        String type = ((Sync) actionMethod.annotation).value();
                        if (resource.containsVerification(type)) {
                            logger.debug("Skipping sync method " + actionMethod.getClassAndMethodName() + " since verification was successful");
                            continue;
                        }
                    }
                    for (Injector injector : injectors) {
                        injector.inject(resource, impl, ctx);
                    }
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
