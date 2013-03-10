/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.systyrant.resource;

import com.kloudtek.systyrant.STContext;
import com.kloudtek.systyrant.exception.FieldInjectionException;
import com.kloudtek.systyrant.exception.InvalidServiceException;

import java.lang.reflect.Field;

/**
 * Injector for the {@link com.kloudtek.systyrant.annotation.Inject} annotation.
 */
public class GenericInjector extends Injector {
    protected GenericInjector(Field field) {
        super(field);
    }

    @Override
    public void inject(Resource resource, Object obj, STContext ctx) throws FieldInjectionException {
        Class<?> type = field.getType();
        if(STContext.class.isAssignableFrom(type) ) {
            inject(obj,ctx);
        } else if( Resource.class.isAssignableFrom(type) ) {
            inject(obj,resource);
        } else {
            try {
                Object service = ctx.getServiceManager().getService(type);
                if( service == null ) {
                    throw new FieldInjectionException(field, "No service of type " + field.getType() + " found");
                }
            } catch (InvalidServiceException e) {
                throw new FieldInjectionException(field, e);
            }
        }
    }
}
