/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.systyrant.resource;

import com.kloudtek.systyrant.FQName;
import com.kloudtek.systyrant.STContext;
import com.kloudtek.systyrant.Stage;
import com.kloudtek.systyrant.annotation.*;
import com.kloudtek.systyrant.exception.*;
import com.kloudtek.systyrant.util.ListHashMap;
import com.kloudtek.systyrant.util.ReflectionHelper;
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
import java.util.Map;

import static com.kloudtek.util.StringUtils.isNotEmpty;

public class JavaResourceFactory extends ResourceFactory {
    private static final Logger logger = LoggerFactory.getLogger(JavaResourceFactory.class);
    private final Class<?> clazz;
    private Injector[] injectors;
    protected ListHashMap<Stage, ActionDef> actions = new ListHashMap<>();
    protected HashMap<String, SyncActionDef> syncActions = new HashMap<>();

    public JavaResourceFactory(Class<?> clazz, FQName fqname) throws InvalidResourceDefinitionException {
        super(new FQName(clazz, fqname));
        logger.debug("Creating factory for java {}",clazz);
        this.clazz = clazz;
        Unique uq = clazz.getAnnotation(Unique.class);
        if (uq != null) {
            logger.debug("Class is unique with scope {}",uq.value());
            setUnique(true);
        }
        for (Method method : clazz.getDeclaredMethods()) {
            if (method.getAnnotation(Prepare.class) != null) {
                logger.debug("Adding PREPARE method: {}",method);
                actions.get(Stage.PREPARE).add(new ActionDef(method, 0, false));
            }
            handleExecMethod(method);
            handleVerifyMethod(method);
            handleSyncMethod(method);
            Cleanup cleanup = method.getAnnotation(Cleanup.class);
            if (cleanup != null) {
                logger.debug("Added CLEANUP method: {} ",method);
                actions.get(Stage.CLEANUP).add(new ActionDef(method, 0, false));
            }
        }
        verifySyncMethod(syncActions);
        ArrayList<Injector> injectorsList = new ArrayList<>();
        for (Field field : clazz.getDeclaredFields()) {
            for (Annotation annotation : field.getAnnotations()) {
                registerFieldInjection(injectorsList, field, annotation);
            }
        }
        injectors = injectorsList.toArray(new Injector[injectorsList.size()]);
        logger.debug("Finished setting up factory for java "+clazz);
    }

    private void verifySyncMethod(HashMap<String, SyncActionDef> syncActionDef) throws InvalidResourceDefinitionException {
        for (SyncActionDef actionDef : syncActionDef.values()) {
            if (actionDef.verifyMethod == null) {
                throw new InvalidResourceDefinitionException("Sync method missing a matching Verify method: " + ReflectionHelper.toString(actionDef.method));
            } else if (actionDef.method == null) {
                throw new InvalidResourceDefinitionException("Verify method missing a matching Sync method: " + ReflectionHelper.toString(actionDef.verifyMethod));
            }
        }
    }

    private void handleVerifyMethod(Method method) throws InvalidResourceDefinitionException {
        Verify verify = method.getAnnotation(Verify.class);
        if (verify != null) {
            String v = verify.value();
            if (!method.getReturnType().equals(boolean.class)) {
                throw new InvalidResourceDefinitionException("Verify method must return a boolean " + ReflectionHelper.toString(method));
            }
            SyncActionDef syncActionDef = getSyncAction(v);
            if (syncActionDef.verifyMethod != null) {
                throw new InvalidResourceDefinitionException("Duplicated verify methods found: " + ReflectionHelper.toString(syncActionDef.verifyMethod)
                        + " and " + ReflectionHelper.toString(method));
            } else {
                syncActionDef.verifyMethod = method;
            }
            logger.debug("Added VERIFY ({}) method: {} ",v,method);
        }
    }

    private void handleSyncMethod(Method method) throws InvalidResourceDefinitionException {
        Sync sync = method.getAnnotation(Sync.class);
        if (sync != null) {
            String v = sync.value();
            SyncActionDef syncAction = getSyncAction(v);
            syncAction.postChildren = sync.postChildren();
            if (syncAction.method != null) {
                throw new InvalidResourceDefinitionException("Duplicated sync methods found: " + ReflectionHelper.toString(syncAction.verifyMethod)
                        + " and " + ReflectionHelper.toString(method));
            } else {
                syncAction.method = method;
                syncAction.order = sync.order();
            }
            logger.debug("Added SYNC ({}) method: {} ",v,method);
        }
    }

    private void handleExecMethod(Method method) {
        Execute exec = method.getAnnotation(Execute.class);
        if (exec != null) {
            logger.debug("Adding EXEC method: {}",method);
            actions.get(Stage.EXECUTE).add(new ActionDef(method, exec.order(), exec.postChildren()));
        }
    }

    private void registerFieldInjection(ArrayList<Injector> injectorsList, Field field, Annotation annotation) {
        if (annotation instanceof Attr) {
            Attr attr = (Attr) annotation;
            String name = attr.value().isEmpty() ? field.getName() : attr.value();
            injectorsList.add(new AttrInjector(name, field));
            if (isNotEmpty(attr.def())) {
                addDefaultAttr(name, attr.def());
            }
        } else if (annotation instanceof Service) {
            injectorsList.add(new ServiceInjector(field, (Service) annotation));
        } else if (annotation instanceof Inject) {
            injectorsList.add(new GenericInjector(field));
        } else if (annotation instanceof Resources) {
            injectorsList.add(new ResourcesInjector(field, ((Resources) annotation).value()));
        }
    }

    @NotNull
    @Override
    protected void configure(STContext context, Resource resource) throws ResourceCreationException {
        try {
            Object impl = clazz.newInstance();
            resource.addJavaImpl(impl);
            for (Stage stage : Stage.values()) {
                for (ActionDef actionDef : actions.get(stage)) {
                    resource.addAction(stage, actionDef.postChildren, new JavaAction(impl, actionDef.method, actionDef.order));
                }
            }
            for (SyncActionDef syncActionDef : syncActions.values()) {
                resource.addAction(Stage.EXECUTE, syncActionDef.postChildren, new JavaSyncAction(impl,
                        syncActionDef.method, syncActionDef.order, syncActionDef.verifyMethod));
            }
            for (Map.Entry<String, String> entry : defaultAttrs.entrySet()) {
                resource.set(entry.getKey(), entry.getValue());
            }
        } catch (InstantiationException | IllegalAccessException e) {
            throw new ResourceCreationException("Java resource cannot be instantiated: " + clazz.getName(), e);
        } catch (InvalidAttributeException e) {
            throw new ResourceCreationException("Error while creating java resource " + clazz.getName() + ": " + e.getMessage(), e);
        }
    }

    public Map<String,String> getAttributesInObject( Object obj ) {
        if( ! clazz.isInstance(obj) ) {
            throw new IllegalArgumentException("Object "+obj.getClass()+" is not of class "+clazz.getName());
        }
        HashMap<String,String> attrs = new HashMap<>();
//        for (Map.Entry<Field, String> entry : attrInject.entrySet()) {
//            attrs.put(entry.get)
//        }
        return attrs;
    }

    private SyncActionDef getSyncAction(String name) {
        SyncActionDef javaSyncActionFactory = syncActions.get(name);
        if (javaSyncActionFactory == null) {
            javaSyncActionFactory = new SyncActionDef();
            syncActions.put(name, javaSyncActionFactory);
        }
        return javaSyncActionFactory;
    }

    public class ActionDef {
        protected Method method;
        protected int order;
        protected boolean postChildren;

        public ActionDef() {
        }

        public ActionDef(Method method, int order, boolean postChildren) {
            this.method = method;
            this.order = order;
            this.postChildren = postChildren;
        }
    }

    public class SyncActionDef extends ActionDef {
        protected Method verifyMethod;

        public SyncActionDef() {
        }
    }

    public class JavaAction extends Action {
        protected final Object obj;
        protected Method method;

        public JavaAction(Object obj, Method method, int order) {
            this.obj = obj;
            this.method = method;
            this.order = order;
        }

        @Override
        public void execute(STContext context, Resource resource, Stage stage, boolean postChildren) throws STRuntimeException {
            try {
                injectAndValidate(resource, context);
                method.invoke(obj);
                updateAttrs(resource);
            } catch (IllegalAccessException e) {
                throw new STRuntimeException(e.getMessage(), e);
            } catch (InvocationTargetException e) {
                throw new STRuntimeException(e.getTargetException().getMessage(), e.getTargetException());
            }
        }

        protected void injectAndValidate(Resource resource, STContext ctx) throws FieldInjectionException, ResourceValidationException {
            for (Injector injector : injectors) {
                injector.inject(resource, obj, ctx);
            }
            try {
                ValidationUtils.validate(obj, ResourceValidationException.class);
            } catch (ResourceValidationException e) {
                throw new ResourceValidationException("Failed to validate '" + resource.toString() + "' " + e.getMessage());
            }
        }

        protected void updateAttrs(Resource resource) throws IllegalAccessException, InvalidAttributeException {
            for (Injector injector : injectors) {
                injector.updateAttr(resource,obj);
            }
//            for (Map.Entry<Field, String> entry : attrInject.entrySet()) {
//                Field field = entry.getKey();
//                String attrName = entry.getValue();
//                Object newValObj = field.get(obj);
//                String newVal = ConvertUtils.convert(newValObj);
//                String oldVal = resource.get(attrName);
//                // If old val was null and new val is boolean false, don't prepareForExecution it
//                if (oldVal == null && Boolean.FALSE.equals(newValObj)) {
//                    newVal = null;
//                }
//                if ((newVal == null && oldVal != null) || (newVal != null && !newVal.equals(oldVal))) {
//                    logger.debug("Updating resource {}'s attribute {} to {}", resource, attrName, newVal);
//                    resource.set(attrName, newVal);
//                }
//            }
        }

        @Override
        public String toString() {
            return "Java action "+ReflectionHelper.toString(method);
        }
    }

    public class JavaSyncAction extends JavaAction implements SyncAction {
        private Method verifyMethod;

        public JavaSyncAction(Object obj, Method method, int order, Method verifyMethod) {
            super(obj, method, order);
            this.verifyMethod = verifyMethod;
        }

        @Override
        public boolean verify(STContext context, Resource resource, Stage stage, boolean postChildren) throws STRuntimeException {
            try {
                injectAndValidate(resource, context);
                Boolean res = (Boolean) verifyMethod.invoke(obj);
                updateAttrs(resource);
                return res;
            } catch (IllegalAccessException e) {
                throw new STRuntimeException(e.getMessage(), e);
            } catch (InvocationTargetException e) {
                throw new STRuntimeException(e.getTargetException().getMessage(), e.getTargetException());
            }
        }

        @Override
        public String toString() {
            return "Java sync action "+ReflectionHelper.toString(verifyMethod)+" / "+ReflectionHelper.toString(method);
        }
    }
}
