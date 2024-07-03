/*
 * Copyright (c) 2024 Aeontronix Inc
 */

package com.aeontronix.aeonbuild.inject;

import com.aeontronix.aeonbuild.exception.FieldInjectionException;
import com.aeontronix.aeonbuild.exception.InvalidServiceException;
import com.aeontronix.aeonbuild.BuildContextImpl;
import com.aeontronix.aeonbuild.Resource;
import com.aeontronix.aeonbuild.ServiceManager;
import com.aeontronix.aeonbuild.annotation.Service;

import java.lang.reflect.Field;

import static com.kloudtek.util.StringUtils.isNotEmpty;

/**
 * This class is used to inject a service into a java resource implementation (see {@link Service} annotation for more
 * details of injection logic).
 */
public class ServiceInjector extends Injector {
    private final String name;

    public ServiceInjector(Class<?> clazz, Field field, Service service) {
        super(clazz, field);
        name = service.value();
    }

    @Override
    public void inject(Resource resource, Object obj, BuildContextImpl ctx) throws FieldInjectionException {
        try {
            ServiceManager serviceManager = ctx.getServiceManager();
            boolean nameSpecified = isNotEmpty(name);
            Object service = nameSpecified ? serviceManager.getService(name) : serviceManager.getService(field.getType());
            if (service == null) {
                throw new FieldInjectionException(field, "No service " + (nameSpecified ? "named '" + name + "' found" :
                        "of type " + field.getType()) + " found");
            }
            inject(obj, service);
        } catch (InvalidServiceException e) {
            throw new FieldInjectionException(field, e);
        }
    }
}
