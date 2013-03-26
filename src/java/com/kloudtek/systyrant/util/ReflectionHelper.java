/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.systyrant.util;

import com.kloudtek.systyrant.exception.STRuntimeException;
import com.kloudtek.systyrant.resource.Resource;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ReflectionHelper {
    public static String toString(Method method) {
        return "Method " + method.getDeclaringClass().getName() + "#" + method.getName();
    }

    public static Object invoke( Method method, Class<?> clazz, Resource resource ) throws STRuntimeException {
        return invoke(method, resource.getJavaImpl(clazz));
    }

    public static Object invoke( Method method, Object obj ) throws STRuntimeException {
        try {
            return method.invoke(obj);
        } catch (IllegalAccessException e) {
            throw new STRuntimeException("Method " + ReflectionHelper.toString(method) + " cannot be invoked: " + e.getMessage(), e);
        } catch (InvocationTargetException e) {
            throw STRuntimeException.getCause(e);
        }
    }

    public static void set(Object obj, String name, Object value) {
        try {
            findField(obj, name).set(obj, value);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static Object get(Object obj, String name) {
        try {
            return findField(obj, name).get(obj);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private static Field findField(Object obj, String name) {
        Class<?> cl = obj.getClass();
        while (cl != null) {
            try {
                Field field = cl.getDeclaredField(name);
                if( ! field.isAccessible() ) {
                    field.setAccessible(true);
                }
                return field;
            } catch (NoSuchFieldException e) {
                cl = cl.getSuperclass();
            }
        }
        throw new IllegalArgumentException("Field " + name + " not found in " + obj.getClass().getName());
    }
}
