/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.systyrant.resource.java;

import com.kloudtek.systyrant.STContext;
import com.kloudtek.systyrant.annotation.OnlyIf;
import com.kloudtek.systyrant.annotation.OnlyIfOperatingSystem;
import com.kloudtek.systyrant.exception.InvalidResourceDefinitionException;
import com.kloudtek.systyrant.exception.STRuntimeException;
import com.kloudtek.systyrant.resource.Resource;
import com.kloudtek.systyrant.util.ReflectionHelper;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

/**
 * Use to enforce customer OnlyIf annotations (on a method).
*/
public class EnforceOnlyIfCustom extends EnforceOnlyIf {
    private Method method;
    private final Class<?> clazz;

    public EnforceOnlyIfCustom(Method method, Class<?> clazz) throws InvalidResourceDefinitionException {
        this.method = method;
        this.clazz = clazz;
        Class<?> returnType = method.getReturnType();
        if( ! returnType.equals(boolean.class) ) {
            throw new InvalidResourceDefinitionException("Method annotated with @OnlyIf must return a boolean value");
        }
    }

    @Override
    public boolean execAllowed(STContext context, Resource resource) throws STRuntimeException {
        return (boolean) ReflectionHelper.invoke(method, clazz, resource);
    }
}
