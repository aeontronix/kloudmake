/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.systyrant.service;

import com.kloudtek.systyrant.STContext;
import com.kloudtek.systyrant.dsl.Parameters;
import com.kloudtek.systyrant.exception.STRuntimeException;

public interface ServiceManager {
    Object getService(String id);

    <X> X getService(Class<X> classtype) throws STRuntimeException;

    void addOverride(String id, Object overrideService);

    void removeOverride(String id, Object overrideService);

    void assignService(String id, Object service);

    void close();

    void start() throws STRuntimeException;

    void stop();

    Object invokeMethod(STContext ctx, String name, Parameters parameters) throws STRuntimeException;
}
