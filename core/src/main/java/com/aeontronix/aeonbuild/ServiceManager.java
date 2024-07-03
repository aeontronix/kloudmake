/*
 * Copyright (c) 2024 Aeontronix Inc
 */

package com.aeontronix.aeonbuild;

import com.aeontronix.aeonbuild.exception.InvalidServiceException;
import com.aeontronix.aeonbuild.exception.KMRuntimeException;

public interface ServiceManager {
    Object getService(String id) throws InvalidServiceException;

    <X> X getService(Class<X> classtype) throws InvalidServiceException;

    void addOverride(String id, Object overrideService) throws InvalidServiceException;

    void removeOverride(String id, Object overrideService);

    void assignService(String id, Object service);

    void close();

    void start() throws KMRuntimeException;

    void stop();

    Object invokeMethod(String name, Parameters parameters) throws KMRuntimeException;

    void registerService(String name, Object service) throws InvalidServiceException;

    void registerService(Class<?> clazz) throws InvalidServiceException;
}
