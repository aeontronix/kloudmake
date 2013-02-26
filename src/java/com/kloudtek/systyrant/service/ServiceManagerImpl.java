/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.systyrant.service;

import com.kloudtek.systyrant.exception.InvalidServiceException;
import com.kloudtek.systyrant.exception.STRuntimeException;
import com.kloudtek.systyrant.service.credstore.CredStore;
import com.kloudtek.systyrant.service.filestore.FileStore;
import com.kloudtek.systyrant.service.host.Host;
import com.kloudtek.systyrant.service.host.LocalHost;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * Simple implementation of the {@link ServiceManager} interface.
 */
public class ServiceManagerImpl extends AbstractServiceManager {
    private static final Logger logger = LoggerFactory.getLogger(ServiceManagerImpl.class);
    private Map<String, Object> services = new HashMap<>();

    public ServiceManagerImpl() throws InvalidServiceException {
        this(null, null, null);
    }

    public ServiceManagerImpl(Host host, FileStore fileStore, CredStore credstore) throws InvalidServiceException {
        if (host == null) {
            host = new LocalHost();
        }
        if (fileStore == null) {
            fileStore = new FileStore();
        }
        if (credstore == null) {
            credstore = new CredStore();
        }
        registerService("host", host);
        registerService("filestore", fileStore);
        registerService("credstore", credstore);
    }

    public void registerService(String name, Object service) throws InvalidServiceException {
        scanForMethods(name, service.getClass());
        services.put(name, service);
    }

    @Override
    protected Object doGetService(@NotNull String id) {
        return services.get(id);
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
