/*
 * Copyright (c) 2024 Aeontronix Inc
 */

package com.aeontronix.aeonbuild.inject;

import com.aeontronix.aeonbuild.exception.FieldInjectionException;
import com.aeontronix.aeonbuild.exception.InvalidQueryException;
import com.aeontronix.aeonbuild.BuildContextImpl;
import com.aeontronix.aeonbuild.Resource;

import java.lang.reflect.Field;
import java.util.List;

public class ResourcesInjector extends Injector {
    private final String query;

    public ResourcesInjector(Class<?> clazz, Field field, String query) {
        super(clazz, field);
        this.query = query;
    }

    @Override
    public void inject(Resource resource, Object obj, BuildContextImpl ctx) throws FieldInjectionException {
        try {
            List<Resource> resources = ctx.findResources(query);
            inject(obj, resources);
        } catch (InvalidQueryException e) {
            throw new FieldInjectionException(field, e.getMessage(), e);
        }
    }
}
