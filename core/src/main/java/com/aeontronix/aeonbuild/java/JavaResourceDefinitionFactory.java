/*
 * Copyright (c) 2024 Aeontronix Inc
 */

package com.aeontronix.aeonbuild.java;

import com.aeontronix.aeonbuild.annotation.*;
import com.aeontronix.aeonbuild.exception.InvalidResourceDefinitionException;
import com.aeontronix.aeonbuild.inject.*;
import com.aeontronix.aeonbuild.util.ReflectionHelper;
import com.aeontronix.aeonbuild.FQName;
import com.aeontronix.aeonbuild.ResourceDefinition;
import com.aeontronix.aeonbuild.annotation.*;
import com.aeontronix.aeonbuild.inject.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

import static com.aeontronix.aeonbuild.Stage.*;
import static com.kloudtek.util.StringUtils.isNotEmpty;

public class JavaResourceDefinitionFactory {
    private static final Logger logger = LoggerFactory.getLogger(JavaResourceDefinitionFactory.class);

    public static ResourceDefinition create(@NotNull Class<?> clazz, @Nullable FQName fqname) throws InvalidResourceDefinitionException {
        ResourceDefinition resourceDefinition = new ResourceDefinition(fqname != null ? fqname : new FQName(clazz, null));
        HashSet<String> requires = new HashSet<>();
        Dependency dep = clazz.getAnnotation(Dependency.class);
        resourceDefinition.addAction(new ResourceInitTask(clazz, requires, dep));
        Unique uq = clazz.getAnnotation(Unique.class);
        try {
            clazz.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new InvalidResourceDefinitionException("Resource class " + clazz.getName() + " cannot be instantiated", e);
        }
        if (uq != null) {
            resourceDefinition.addUniqueScope(uq.value());
        }
        Set<EnforceOnlyIf> onlyIf = EnforceOnlyIf.find(clazz);
        Set<JavaTask> actions = new HashSet<>();
        HashMap<String, JavaTask> syncs = new HashMap<>();
        ArrayList<Injector> injectors = new ArrayList<>();
        for (Field field : clazz.getDeclaredFields()) {
            for (Annotation annotation : field.getAnnotations()) {
                registerFieldInjection(clazz, resourceDefinition, injectors, field, annotation, requires);
            }
        }
        for (Method method : clazz.getDeclaredMethods()) {
            handlePrepareActions(clazz, actions, method, injectors, onlyIf);
            handleExecActions(clazz, actions, method, injectors, onlyIf);
            handleVerifyActions(clazz, actions, method, syncs, injectors, onlyIf);
            handleSyncMethods(clazz, actions, method, syncs, injectors, onlyIf);
            handleCleanupMethods(clazz, actions, method, injectors, onlyIf);
            handleNotificationHandling(clazz, resourceDefinition, method);
        }
        for (JavaTask javaAction : syncs.values()) {
            if (javaAction.getMethod() == null) {
                throw new InvalidResourceDefinitionException("Resource class verify method missing matching sync: " + ReflectionHelper.toString(javaAction.getVerifyMethod()));
            } else if (javaAction.getVerifyMethod() == null) {
                throw new InvalidResourceDefinitionException("Resource class verify method missing matching sync: " + ReflectionHelper.toString(javaAction.getMethod()));
            }
        }
        for (JavaTask action : actions) {
            resourceDefinition.addAction(action);
        }
        return resourceDefinition;
    }

    private static void handleNotificationHandling(Class<?> clazz, ResourceDefinition resourceDefinition, Method method) {
        HandleNotification handleNotification = method.getAnnotation(HandleNotification.class);
        if (handleNotification != null) {
            resourceDefinition.addNotificationHandler(new JavaNotificationHandler(method, handleNotification, clazz));
        }
    }

    private static void handleCleanupMethods(Class<?> clazz, Set<JavaTask> actions, Method method, ArrayList<Injector> injectors, Set<EnforceOnlyIf> onlyIf) throws InvalidResourceDefinitionException {
        Cleanup cleanup = method.getAnnotation(Cleanup.class);
        if (cleanup != null) {
            logger.debug("Added CLEANUP method: {} ", method);
            actions.add(new JavaTask(cleanup.order(), CLEANUP, false, clazz, injectors, onlyIf, method));
        }
    }

    private static void handleSyncMethods(Class<?> clazz, Set<JavaTask> actions, Method method, HashMap<String, JavaTask> syncs, ArrayList<Injector> injectors, Set<EnforceOnlyIf> onlyIf) throws InvalidResourceDefinitionException {
        Sync sync = method.getAnnotation(Sync.class);
        if (sync != null) {
            String syncId = sync.value();
            JavaTask existing = syncs.get(syncId);
            if (existing != null) {
                if (existing.getMethod() != null) {
                    throw new InvalidResourceDefinitionException("Duplicated sync methods found: "
                            + ReflectionHelper.toString(existing.getMethod())
                            + " and " + ReflectionHelper.toString(method));
                } else {
                    existing.setMethod(method);
                }
                existing.setOrder(sync.order());
                existing.setPostChildren(sync.postChildren());
                existing.setStage(sync.stage());
            } else {
                JavaTask action = new JavaTask(sync.order(), sync.stage(), sync.postChildren(), clazz, injectors, onlyIf, method);
                actions.add(action);
                syncs.put(syncId, action);
            }
            logger.debug("Added SYNC ({}) method: {} ", syncId, method);
        }
    }

    private static void handleVerifyActions(Class<?> clazz, Set<JavaTask> actions, Method method, HashMap<String, JavaTask> syncs, ArrayList<Injector> injectors, Set<EnforceOnlyIf> onlyIf) throws InvalidResourceDefinitionException {
        Verify verify = method.getAnnotation(Verify.class);
        if (verify != null) {
            String syncId = verify.value();
            if (!method.getReturnType().equals(boolean.class)) {
                throw new InvalidResourceDefinitionException("Verify method must return a boolean " + ReflectionHelper.toString(method));
            }
            JavaTask existing = syncs.get(syncId);
            if (existing != null) {
                if (existing.getVerifyMethod() != null) {
                    throw new InvalidResourceDefinitionException("Duplicated verify methods found: "
                            + ReflectionHelper.toString(existing.getVerifyMethod())
                            + " and " + ReflectionHelper.toString(method));
                } else {
                    existing.setVerifyMethod(method);
                }
            } else {
                JavaTask action = new JavaTask(0, EXECUTE, false, clazz, injectors, onlyIf, null, method);
                actions.add(action);
                syncs.put(syncId, action);
            }
            logger.debug("Added VERIFY ({}) method: {} ", syncId, method);
        }
    }

    private static void handleExecActions(Class<?> clazz, Set<JavaTask> actions, Method method, ArrayList<Injector> injectors, Set<EnforceOnlyIf> onlyIf) throws InvalidResourceDefinitionException {
        Execute exec = method.getAnnotation(Execute.class);
        if (exec != null) {
            logger.debug("Adding EXEC method: {}", method);
            actions.add(new JavaTask(exec.order(), EXECUTE, exec.postChildren(), clazz, injectors, onlyIf, method));
        }
    }

    private static void handlePrepareActions(Class<?> clazz, Set<JavaTask> actions, Method method, List<Injector> injectors, Set<EnforceOnlyIf> onlyIf) throws InvalidResourceDefinitionException {
        Prepare prepareAnno = method.getAnnotation(Prepare.class);
        if (prepareAnno != null) {
            logger.debug("Adding PREPARE method: {}", method);
            actions.add(new JavaTask(prepareAnno.order(), PREPARE, false, clazz, injectors, onlyIf, method));
        }
    }

    private static void registerFieldInjection(Class<?> clazz, ResourceDefinition resourceDefinition,
                                               ArrayList<Injector> injectorsList, Field field, Annotation annotation, HashSet<String> requires) throws InvalidResourceDefinitionException {
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
        } else if (annotation instanceof Requires) {
            String reqExpr = ((Requires) annotation).value();
            injectorsList.add(new RequiresInjector(clazz, field, reqExpr));
            requires.add(reqExpr);
        }
    }
}
