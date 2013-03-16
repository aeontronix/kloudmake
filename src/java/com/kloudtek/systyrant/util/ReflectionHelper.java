/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.systyrant.util;

import java.lang.reflect.Method;

public class ReflectionHelper {
    public static String toString(Method method) {
        return "Method " + method.getDeclaringClass().getName() + "#" + method.getName();
    }
}
