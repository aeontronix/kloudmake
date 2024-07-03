/*
 * Copyright (c) 2024 Aeontronix Inc
 */

package com.aeontronix.aeonbuild.java;

import com.aeontronix.aeonbuild.exception.InvalidResourceDefinitionException;
import com.aeontronix.aeonbuild.exception.KMRuntimeException;
import com.aeontronix.aeonbuild.util.ReflectionHelper;
import com.aeontronix.aeonbuild.BuildContextImpl;
import com.aeontronix.aeonbuild.Resource;

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
    public boolean execAllowed(BuildContextImpl context, Resource resource) throws KMRuntimeException {
        return (boolean) ReflectionHelper.invoke(method, clazz, resource);
    }
}
