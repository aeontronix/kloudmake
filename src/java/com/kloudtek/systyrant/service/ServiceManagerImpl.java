/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.systyrant.service;

import com.kloudtek.systyrant.STContext;
import com.kloudtek.systyrant.annotation.Method;
import com.kloudtek.systyrant.annotation.Service;
import com.kloudtek.systyrant.dsl.Parameters;
import com.kloudtek.systyrant.exception.InvalidServiceException;
import com.kloudtek.systyrant.exception.STRuntimeException;
import org.jetbrains.annotations.NotNull;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import static com.kloudtek.util.StringUtils.isEmpty;

/**
 * Simple implementation of the {@link ServiceManager} interface.
 */
public class ServiceManagerImpl implements ServiceManager {
    private static final Logger logger = LoggerFactory.getLogger(ServiceManagerImpl.class);
    private Map<String, Object> services = new HashMap<>();
    protected HashMap<String, LinkedList<Object>> overrides = new HashMap<>();
    protected HashMap<String, MethodInvoker> methods = new HashMap<>();
    private Reflections reflections;

    public ServiceManagerImpl(Reflections reflections) {
        this.reflections = reflections;
    }

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
    public synchronized final Object getService(@NotNull String id) throws InvalidServiceException {
        id = id.toLowerCase();
        LinkedList<Object> ovr = overrides.get(id);
        Object service;
        if (ovr != null && !ovr.isEmpty()) {
            service = ovr.getLast();
        } else {
            service = services.get(id);
        }
        if (service == null) {
            Set<Class<?>> services = reflections.getTypesAnnotatedWith(Service.class);
            for (Class<?> clazz : services) {
                Service annotation = clazz.getAnnotation(Service.class);
                Class defaultImpl = annotation.def();
                if (!defaultImpl.equals(ServiceManager.class)) {
                    clazz = defaultImpl;
                }
                String name = annotation.value();
                if (isEmpty(name)) {
                    name = clazz.getSimpleName().toLowerCase();
                }
                if (name.equalsIgnoreCase(id)) {
                    try {
                        service = clazz.newInstance();
                        registerService(name, service);
                    } catch (InstantiationException | IllegalAccessException e) {
                        throw new InvalidServiceException("Unable to instantiate java service " + clazz.getName());
                    }
                }
            }
        }
        return service;
    }

    @Override
    @SuppressWarnings("unchecked")
    public synchronized final <X> X getService(@NotNull Class<X> classtype) throws InvalidServiceException {
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
        logger.debug("added override {} for service {}", overrideService.toString(), id);
    }

    @Override
    public synchronized void removeOverride(@NotNull String id, @NotNull Object overrideService) {
        LinkedList<Object> list = overrides.get(id.toLowerCase());
        if (list != null) {
            list.remove(overrideService);
        }
        logger.debug("removed override {} for service {}", overrideService.toString(), id);
    }

    @Override
    public void registerService(String name, Object service) throws InvalidServiceException {
        scanForMethods(name, service.getClass());
        services.put(name, service);
    }

    @Override
    public void assignService(String id, Object service) {
        services.put(id, service);
    }

    @Override
    public void close() {
        for (Object service : services.values()) {
            if (service instanceof AutoCloseable) {
                try {
                    ((AutoCloseable) service).close();
                } catch (Exception e) {
                    logger.warn(e.getMessage(), e);
                }
            }
        }
    }

    @Override
    public void start() throws STRuntimeException {
        for (Object service : services.values()) {
            if (service instanceof Startable) {
                ((Startable) service).start();
            }
        }
    }

    @Override
    public void stop() {
        for (Object service : services.values()) {
            if (service instanceof Stoppable) {
                ((Stoppable) service).stop();
            }
        }
        for (LinkedList<Object> overrideList : overrides.values()) {
            for (Object ov : overrideList) {
                if (ov instanceof Stoppable) {
                    ((Stoppable) ov).stop();
                }
            }
        }
        overrides.clear();
    }
}
