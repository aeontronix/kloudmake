/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.systyrant;

import com.kloudtek.systyrant.context.Parameters;
import com.kloudtek.systyrant.exception.InvalidServiceException;
import com.kloudtek.systyrant.exception.STRuntimeException;

public interface ServiceManager {
    Object getService(String id) throws InvalidServiceException;

    <X> X getService(Class<X> classtype) throws InvalidServiceException;

    void addOverride(String id, Object overrideService) throws InvalidServiceException;

    void removeOverride(String id, Object overrideService);

    void assignService(String id, Object service);

    void close();

    void start() throws STRuntimeException;

    void stop();

    Object invokeMethod(String name, Parameters parameters) throws STRuntimeException;

    void registerService(String name, Object service) throws InvalidServiceException;

    void registerService(Class<?> clazz) throws InvalidServiceException;
}
