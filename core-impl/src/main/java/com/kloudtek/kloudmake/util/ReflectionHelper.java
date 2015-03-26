/*
 * Copyright (c) 2015. Kelewan Technologies Ltd
 */

package com.kloudtek.kloudmake.util;

import com.kloudtek.kloudmake.Resource;
import com.kloudtek.kloudmake.exception.KMRuntimeException;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ReflectionHelper {
    public static String toString(Method method) {
        return "Function " + method.getDeclaringClass().getName() + "#" + method.getName();
    }

    public static String toString(Field field) {
        return field.getDeclaringClass().getName() + "#" + field.getName();
    }

    public static Object invoke(Method method, Class<?> clazz, Resource resource) throws KMRuntimeException {
        return invoke(method, resource.getJavaImpl(clazz));
    }

    public static Object invoke(Method method, Object obj) throws KMRuntimeException {
        try {
            return method.invoke(obj);
        } catch (IllegalAccessException e) {
            throw new KMRuntimeException("Function " + ReflectionHelper.toString(method) + " cannot be invoked: " + e.getMessage(), e);
        } catch (InvocationTargetException e) {
            throw KMRuntimeException.getCause(e);
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
                if (!field.isAccessible()) {
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
