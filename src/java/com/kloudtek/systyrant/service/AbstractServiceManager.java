/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.systyrant.service;

import com.kloudtek.systyrant.STContext;
import com.kloudtek.systyrant.annotation.Method;
import com.kloudtek.systyrant.dsl.Parameters;
import com.kloudtek.systyrant.exception.InvalidServiceException;
import com.kloudtek.systyrant.exception.STRuntimeException;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.LinkedList;

import static com.kloudtek.util.StringUtils.isEmpty;

/**
 * Simple implementation of the {@link com.kloudtek.systyrant.service.ServiceManager} interface.
 */
public abstract class AbstractServiceManager implements ServiceManager {
    private static final Logger logger = LoggerFactory.getLogger(AbstractServiceManager.class);
    protected HashMap<String, LinkedList<Object>> overrides = new HashMap<>();
    protected HashMap<String, MethodInvoker> methods = new HashMap<>();

    protected synchronized void scanForMethods(String serviceName, Class<?> clazz) throws InvalidServiceException {
        for (java.lang.reflect.Method javaMethod : clazz.getDeclaredMethods()) {
            Method methodAnno = javaMethod.getAnnotation(Method.class);
            if (methodAnno != null) {
                String name = (isEmpty(methodAnno.value()) ? javaMethod.getName() : methodAnno.value()).toLowerCase();
                if (methods.containsKey(name)) {
                    throw new InvalidServiceException("Service method already registered: " + name);
                }
                methods.put(name, new MethodInvoker(serviceName, name, javaMethod));
            }
        }
    }

    @Override
    public Object invokeMethod(STContext ctx, String name, Parameters parameters) throws STRuntimeException {
        MethodInvoker methodInvoker = methods.get(name.toLowerCase());
        if (methodInvoker == null) {
            throw new STRuntimeException("There is no method named " + name);
        }
        return methodInvoker.invoke(ctx, parameters);
    }

    @Override
    public synchronized final Object getService(@NotNull String id) {
        id = id.toLowerCase();
        LinkedList<Object> ovr = overrides.get(id);
        if (ovr != null && !ovr.isEmpty()) {
            return ovr.getLast();
        } else {
            return doGetService(id);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public synchronized final <X> X getService(@NotNull Class<X> classtype) throws STRuntimeException {
        return (X) getService(classtype.getSimpleName().toLowerCase());
    }

    @Override
    public synchronized void addOverride(@NotNull String id, @NotNull Object overrideService) {
        id = id.toLowerCase();
        LinkedList<Object> list = overrides.get(id);
        if (list == null) {
            list = new LinkedList<>();
            overrides.put(id, list);
        }
        list.addLast(overrideService);
        logger.debug("added override {} for service {}" + overrideService.toString(), id);
    }

    @Override
    public synchronized void removeOverride(@NotNull String id, @NotNull Object overrideService) {
        LinkedList<Object> list = overrides.get(id.toLowerCase());
        if (list != null) {
            list.remove(overrideService);
        }
        logger.debug("removed override {} for service {}" + overrideService.toString(), id);
    }

    protected abstract Object doGetService(@NotNull String id);

}
