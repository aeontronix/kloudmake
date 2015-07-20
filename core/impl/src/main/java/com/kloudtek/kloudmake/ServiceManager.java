/*
 * Copyright (c) 2015. Kelewan Technologies Ltd
 */

package com.kloudtek.kloudmake;

import com.kloudtek.kloudmake.exception.InvalidServiceException;
import com.kloudtek.kloudmake.exception.KMRuntimeException;

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
