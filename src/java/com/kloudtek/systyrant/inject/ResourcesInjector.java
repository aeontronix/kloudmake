/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.systyrant.inject;

import com.kloudtek.systyrant.Resource;
import com.kloudtek.systyrant.STContext;
import com.kloudtek.systyrant.exception.FieldInjectionException;
import com.kloudtek.systyrant.exception.InvalidQueryException;

import java.lang.reflect.Field;
import java.util.List;

public class ResourcesInjector extends Injector {
    private final String query;

    public ResourcesInjector(Class<?> clazz, Field field, String query) {
        super(clazz, field);
        this.query = query;
    }

    @Override
    public void inject(Resource resource, Object obj, STContext ctx) throws FieldInjectionException {
        try {
            List<Resource> resources = ctx.findResources(query);
            inject(obj, resources);
        } catch (InvalidQueryException e) {
            throw new FieldInjectionException(field, e.getMessage(), e);
        }
    }
}
