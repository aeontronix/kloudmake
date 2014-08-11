/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.kloudmake.inject;

import com.kloudtek.kloudmake.KMContextImpl;
import com.kloudtek.kloudmake.Resource;
import com.kloudtek.kloudmake.exception.FieldInjectionException;
import com.kloudtek.kloudmake.exception.InvalidQueryException;

import java.lang.reflect.Field;
import java.util.List;

public class ResourcesInjector extends Injector {
    private final String query;

    public ResourcesInjector(Class<?> clazz, Field field, String query) {
        super(clazz, field);
        this.query = query;
    }

    @Override
    public void inject(Resource resource, Object obj, KMContextImpl ctx) throws FieldInjectionException {
        try {
            List<Resource> resources = ctx.findResources(query);
            inject(obj, resources);
        } catch (InvalidQueryException e) {
            throw new FieldInjectionException(field, e.getMessage(), e);
        }
    }
}
