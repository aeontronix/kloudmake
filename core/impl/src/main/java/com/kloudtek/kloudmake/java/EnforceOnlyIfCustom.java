/*
 * Copyright (c) 2015. Kelewan Technologies Ltd
 */

package com.kloudtek.kloudmake.java;

import com.kloudtek.kloudmake.KMContextImpl;
import com.kloudtek.kloudmake.Resource;
import com.kloudtek.kloudmake.exception.InvalidResourceDefinitionException;
import com.kloudtek.kloudmake.exception.KMRuntimeException;
import com.kloudtek.kloudmake.util.ReflectionHelper;

import java.lang.reflect.Method;

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
        if (!returnType.equals(boolean.class)) {
            throw new InvalidResourceDefinitionException("Method annotated with @OnlyIf must return a boolean value");
        }
    }

    @Override
    public boolean execAllowed(KMContextImpl context, Resource resource) throws KMRuntimeException {
        return (boolean) ReflectionHelper.invoke(method, clazz, resource);
    }
}
