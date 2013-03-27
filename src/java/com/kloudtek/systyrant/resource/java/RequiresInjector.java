/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.systyrant.resource.java;

import com.kloudtek.systyrant.STContext;
import com.kloudtek.systyrant.exception.FieldInjectionException;
import com.kloudtek.systyrant.resource.Injector;
import com.kloudtek.systyrant.resource.Resource;
import com.kloudtek.systyrant.util.ReflectionHelper;

import java.lang.reflect.Field;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: yannick
 * Date: 27/03/13
 * Time: 01:18
 * To change this template use File | Settings | File Templates.
 */
public class RequiresInjector extends Injector {
    private final String expr;
    private final FieldType type;

    public RequiresInjector(Class<?> clazz, Field field, String expr) {
        super(clazz, field);
        this.expr = expr;
        type = FieldType.valueOf(field, Resource.class);
    }

    @Override
    public void inject(Resource resource, Object obj, STContext ctx) throws FieldInjectionException {
        List<Resource> list = resource.getResolvedRequires(expr);
        if( list != null ) {
            int size = list.size();
            if( type == FieldType.OBJ ) {
                if( size == 1 ) {
                    inject(obj,list.get(0));
                } else if ( size > 1) {
                    throw new FieldInjectionException(field,"Cannot inject multiple objects in field");
                }
            } else if ( type == FieldType.COLLECTION ) {
                inject(obj,list);
            } else if( type == FieldType.ARRAY ) {
                inject(obj,list.toArray(new Resource[size]));
            } else {
                throw new FieldInjectionException(field,"Field is not a single/array/collection of resources");
            }
        }
    }
}
