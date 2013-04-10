/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.systyrant.context.ioc;

import com.kloudtek.systyrant.Resource;
import com.kloudtek.systyrant.STContext;
import com.kloudtek.systyrant.exception.FieldInjectionException;
import com.kloudtek.systyrant.exception.InvalidAttributeException;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.Collection;

/**
 * Created with IntelliJ IDEA.
 * User: yannick
 * Date: 10/03/13
 * Time: 12:54
 * To change this template use File | Settings | File Templates.
 */
public abstract class Injector {
    protected Class<?> clazz;
    protected final Field field;

    protected Injector(Class<?> clazz, Field field) {
        this.clazz = clazz;
        this.field = field;
        if (!field.isAccessible()) {
            field.setAccessible(true);
        }
    }

    protected void inject(Object obj, Object value) throws FieldInjectionException {
        try {
            field.set(obj, value);
        } catch (IllegalAccessException e) {
            throw new FieldInjectionException(field, e);
        }
    }

    public Field getField() {
        return field;
    }

    public abstract void inject(Resource resource, Object obj, STContext ctx) throws FieldInjectionException;

    public void updateAttr(Resource resource, Object obj) throws IllegalAccessException, InvalidAttributeException {
    }

    public enum FieldType {
        OTHER, OBJ, ARRAY, COLLECTION;

        public static FieldType valueOf(Field field, Class<?> expected) {
            Class<?> type = field.getType();
            if (type.equals(expected)) {
                return FieldType.OBJ;
            } else if (Collection.class.isAssignableFrom(type) && expected.equals(((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0])) {
                return FieldType.COLLECTION;
            } else if (type.isArray() && expected.equals(type.getComponentType())) {
                return FieldType.ARRAY;
            } else {
                return FieldType.OTHER;
            }
        }
    }
}
