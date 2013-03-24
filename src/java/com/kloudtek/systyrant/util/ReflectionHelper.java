/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.systyrant.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class ReflectionHelper {
    public static String toString(Method method) {
        return "Method " + method.getDeclaringClass().getName() + "#" + method.getName();
    }

    public static void forceSet(Object obj, String name, Object value) {
        try {
            Field field = findField(obj, name);
            field.setAccessible(true);
            field.set(obj, value);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static void set(Object obj, String name, Object value) {
        try {
            findField(obj, name).set(obj, value);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private static Field findField(Object obj, String name) {
        Class<?> cl = obj.getClass();
        while (cl != null) {
            try {
                return cl.getDeclaredField(name);
            } catch (NoSuchFieldException e) {
                cl = cl.getSuperclass();
            }
        }
        throw new IllegalArgumentException("Field " + name + " not found in " + obj.getClass().getName());
    }
}
