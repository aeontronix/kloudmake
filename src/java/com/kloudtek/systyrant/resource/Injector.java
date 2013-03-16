/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.systyrant.resource;

import com.kloudtek.systyrant.STContext;
import com.kloudtek.systyrant.exception.FieldInjectionException;
import com.kloudtek.systyrant.exception.InvalidAttributeException;
import com.kloudtek.systyrant.exception.STRuntimeException;

import java.lang.reflect.Field;

/**
 * Created with IntelliJ IDEA.
 * User: yannick
 * Date: 10/03/13
 * Time: 12:54
 * To change this template use File | Settings | File Templates.
 */
public abstract class Injector {
    protected final Field field;

    protected Injector(Field field) {
        this.field = field;
        if( ! field.isAccessible() ) {
            field.setAccessible(true);
        }
    }

    protected void inject(Object obj, Object value) throws FieldInjectionException {
        try {
            field.set(obj,value);
        } catch (IllegalAccessException e) {
            throw new FieldInjectionException(field,e);
        }
    }

    public abstract void inject(Resource resource, Object obj, STContext ctx) throws FieldInjectionException;

    public void updateAttr(Resource resource, Object obj) throws IllegalAccessException, InvalidAttributeException {
    }
}
