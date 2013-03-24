/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.systyrant.resource;

import com.kloudtek.systyrant.FQName;
import com.kloudtek.systyrant.STContext;
import com.kloudtek.systyrant.annotation.*;
import com.kloudtek.systyrant.exception.InvalidResourceDefinitionException;
import com.kloudtek.systyrant.exception.STRuntimeException;
import com.kloudtek.systyrant.util.ReflectionHelper;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotNull;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

import static com.kloudtek.systyrant.resource.Action.Type.*;
import static com.kloudtek.util.StringUtils.isNotEmpty;

public class JavaResourceDefinitionFactory {
    private static final Logger logger = LoggerFactory.getLogger(JavaResourceDefinitionFactory.class);

    public static ResourceDefinition create(@NotNull Class<?> clazz, @Nullable FQName fqname) throws InvalidResourceDefinitionException {
        ResourceDefinition resourceDefinition = new ResourceDefinition(fqname != null ? fqname : new FQName(clazz, null));
        resourceDefinition.addAction(new ResourceInitAction(clazz));
        Unique uq = clazz.getAnnotation(Unique.class);
        try {
            clazz.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new InvalidResourceDefinitionException("Resource class " + clazz.getName() + " cannot be instantiated", e);
        }
        String source = "class " + clazz.getName();
        if (uq != null) {
            resourceDefinition.addUniqueScope(uq.value());
        }
        Set<JavaAction> actions = new HashSet<>();
        HashMap<String, JavaAction> syncs = new HashMap<>();
        ArrayList<Injector> injectors = new ArrayList<>();
        for (Field field : clazz.getDeclaredFields()) {
            for (Annotation annotation : field.getAnnotations()) {
                registerFieldInjection(clazz, resourceDefinition, injectors, field, annotation, source);
            }
        }
        for (Method method : clazz.getDeclaredMethods()) {
            handlePrepareActions(clazz, actions, method, injectors);
            handleExecActions(clazz, actions, method, injectors);
            handleVerifyActions(clazz, actions, method, syncs, injectors);
            handleSyncMethods(clazz, actions, method, syncs, injectors);
            handleCleanupMethods(clazz, actions, method, injectors);
        }
        for (JavaAction javaAction : syncs.values()) {
            if (javaAction.getMethod() == null) {
                throw new InvalidResourceDefinitionException("Resource class verify method missing matching sync: " + ReflectionHelper.toString(javaAction.getVerifyMethod()));
            } else if (javaAction.getVerifyMethod() == null) {
                throw new InvalidResourceDefinitionException("Resource class verify method missing matching sync: " + ReflectionHelper.toString(javaAction.getMethod()));
            }
        }
        for (JavaAction action : actions) {
            resourceDefinition.addAction(action);
        }
        return resourceDefinition;
    }

    private static void handleCleanupMethods(Class<?> clazz, Set<JavaAction> actions, Method method, ArrayList<Injector> injectors) {
        Cleanup cleanup = method.getAnnotation(Cleanup.class);
        if (cleanup != null) {
            logger.debug("Added CLEANUP method: {} ", method);
            actions.add(new JavaAction(cleanup.order(), CLEANUP, clazz, injectors, method));
        }
    }

    private static void handleSyncMethods(Class<?> clazz, Set<JavaAction> actions, Method method, HashMap<String, JavaAction> syncs, ArrayList<Injector> injectors) throws InvalidResourceDefinitionException {
        Sync sync = method.getAnnotation(Sync.class);
        if (sync != null) {
            String syncId = sync.value();
            JavaAction existing = syncs.get(syncId);
            if (existing != null) {
                if (existing.getMethod() != null) {
                    throw new InvalidResourceDefinitionException("Duplicated sync methods found: "
                            + ReflectionHelper.toString(existing.getMethod())
                            + " and " + ReflectionHelper.toString(method));
                } else {
                    existing.setMethod(method);
                }
                existing.setOrder(sync.order());
                existing.setType(sync.postChildren() ? POSTCHILDREN_SYNC : SYNC);
            } else {
                JavaAction action = new JavaAction(sync.order(), sync.postChildren() ? POSTCHILDREN_SYNC : SYNC, clazz, injectors, method);
                actions.add(action);
                syncs.put(syncId,action);
            }
            logger.debug("Added SYNC ({}) method: {} ", syncId, method);
        }
    }

    private static void handleVerifyActions(Class<?> clazz, Set<JavaAction> actions, Method method, HashMap<String, JavaAction> syncs, ArrayList<Injector> injectors) throws InvalidResourceDefinitionException {
        Verify verify = method.getAnnotation(Verify.class);
        if (verify != null) {
            String syncId = verify.value();
            if (!method.getReturnType().equals(boolean.class)) {
                throw new InvalidResourceDefinitionException("Verify method must return a boolean " + ReflectionHelper.toString(method));
            }
            JavaAction existing = syncs.get(syncId);
            if (existing != null) {
                if (existing.getVerifyMethod() != null) {
                    throw new InvalidResourceDefinitionException("Duplicated verify methods found: "
                            + ReflectionHelper.toString(existing.getVerifyMethod())
                            + " and " + ReflectionHelper.toString(method));
                } else {
                    existing.setVerifyMethod(method);
                }
            } else {
                JavaAction action = new JavaAction(0, SYNC, clazz, injectors, null, method);
                actions.add(action);
                syncs.put(syncId,action);
            }
            logger.debug("Added VERIFY ({}) method: {} ", syncId, method);
        }
    }

    private static void handleExecActions(Class<?> clazz, Set<JavaAction> actions, Method method, ArrayList<Injector> injectors) {
        Execute exec = method.getAnnotation(Execute.class);
        if (exec != null) {
            logger.debug("Adding EXEC method: {}", method);
            actions.add(new JavaAction(exec.order(), exec.postChildren() ? POSTCHILDREN_EXECUTE : EXECUTE, clazz, injectors, method));
        }
    }

    private static void handlePrepareActions(Class<?> clazz, Set<JavaAction> actions, Method method, List<Injector> injectors) {
        Prepare prepareAnno = method.getAnnotation(Prepare.class);
        if (prepareAnno != null) {
            logger.debug("Adding PREPARE method: {}", method);
            actions.add(new JavaAction(prepareAnno.order(), PREPARE, clazz, injectors, method));
        }
    }

    private static void registerFieldInjection(Class<?> clazz, ResourceDefinition resourceDefinition,
                                               ArrayList<Injector> injectorsList, Field field, Annotation annotation,
                                               String source) throws InvalidResourceDefinitionException {
        if (annotation instanceof Attr) {
            Attr attr = (Attr) annotation;
            String name = attr.value().isEmpty() ? field.getName() : attr.value();
            injectorsList.add(new AttrInjector(clazz, name, field));
            if (isNotEmpty(attr.def())) {
                resourceDefinition.addDefaultAttr(name, attr.def());
            }
        } else if (annotation instanceof Service) {
            injectorsList.add(new ServiceInjector(clazz, field, (Service) annotation));
        } else if (annotation instanceof Inject) {
            injectorsList.add(new GenericInjector(clazz, field));
        } else if (annotation instanceof Resources) {
            injectorsList.add(new ResourcesInjector(clazz, field, ((Resources) annotation).value()));
        }
    }

    public static class ResourceInitAction extends AbstractAction {
        private Class<?> clazz;

        public ResourceInitAction(Class<?> clazz) {
            type = INIT;
            this.clazz = clazz;
        }

        @Override
        public void execute(STContext context, Resource resource) throws STRuntimeException {
            try {
                resource.addJavaImpl(clazz.newInstance());
            } catch (InstantiationException | IllegalAccessException e) {
                throw new STRuntimeException("Unable to create java resource instance "+e.getMessage(),e);
            }
        }
    }
}
