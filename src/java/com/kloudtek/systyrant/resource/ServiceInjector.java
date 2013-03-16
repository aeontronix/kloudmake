/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.systyrant.resource;

import com.kloudtek.systyrant.STContext;
import com.kloudtek.systyrant.annotation.Service;
import com.kloudtek.systyrant.exception.FieldInjectionException;
import com.kloudtek.systyrant.exception.InvalidAttributeException;
import com.kloudtek.systyrant.exception.InvalidServiceException;
import com.kloudtek.systyrant.service.ServiceManager;

import java.lang.reflect.Field;

import static com.kloudtek.util.StringUtils.isEmpty;
import static com.kloudtek.util.StringUtils.isNotEmpty;

/**
 * This class is used to inject a service into a java resource implementation (see {@link Service} annotation for more
 * details of injection logic).
 */
public class ServiceInjector extends Injector {
    private final String name;

    public ServiceInjector(Field field, Service service) {
        super(field);
        name = service.value();
    }

    @Override
    public void inject(Resource resource, Object obj, STContext ctx) throws FieldInjectionException {
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
