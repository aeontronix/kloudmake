/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.systyrant.resource;

import com.kloudtek.systyrant.STContext;
import com.kloudtek.systyrant.exception.FieldInjectionException;
import com.kloudtek.systyrant.exception.InvalidAttributeException;
import com.kloudtek.systyrant.exception.ResourceValidationException;
import com.kloudtek.systyrant.exception.STRuntimeException;
import com.kloudtek.systyrant.util.ReflectionHelper;
import com.kloudtek.util.validation.ValidationUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: yannick
 * Date: 24/03/13
 * Time: 00:01
 * To change this template use File | Settings | File Templates.
 */
public class JavaAction extends AbstractAction {
    private Class<?> implClass;
    private final List<Injector> injectors;
    private Method method;
    private Method verifyMethod;

    public JavaAction(int order, @NotNull Type type, @NotNull Class<?> implClass, @NotNull List<Injector> injectors, @Nullable Method method) {
        this(order, type, implClass, injectors, method, null);
    }

    public JavaAction(int order, @NotNull Type type, @NotNull Class<?> implClass, @NotNull List<Injector> injectors, @Nullable Method method, @Nullable Method verifyMethod) {
        super(order, type);
        this.implClass = implClass;
        this.injectors = injectors;
        this.method = method;
        this.verifyMethod = verifyMethod;
    }

    @Override
    public void execute(STContext context, Resource resource) throws STRuntimeException {
        invoke(context, resource, method);
    }

    private Object invoke(STContext context, Resource resource, Method method) throws STRuntimeException {
        Object javaImpl = resource.getJavaImpl(implClass);
        if( javaImpl == null ) {
            try {
                javaImpl = implClass.newInstance();
                resource.addJavaImpl(javaImpl);
            } catch (InstantiationException| IllegalAccessException e) {
                throw new STRuntimeException("Unable to create class "+implClass.getName()+" "+e.getMessage(),e);
            }
        }
        assert javaImpl != null;
        injectAndValidate(resource, javaImpl, context);
        try {
            Object ret = method.invoke(javaImpl);
            updateAttrs(resource, javaImpl);
            return ret;
        } catch (IllegalAccessException e) {
            throw new STRuntimeException("Method " + ReflectionHelper.toString(method) + " cannot be invoked: " + e.getMessage(), e);
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            if( cause instanceof STRuntimeException ) {
                throw (STRuntimeException)cause;
            } else {
                throw new STRuntimeException(cause.getMessage(), cause);
            }
        }
    }

    @Override
    public boolean checkExecutionRequired(STContext context, Resource resource) throws STRuntimeException {
        if (verifyMethod != null) {
            return (boolean) invoke(context, resource, verifyMethod);
        }
        return true;
    }


    protected void injectAndValidate(Resource resource, Object javaImpl, STContext ctx) throws FieldInjectionException, ResourceValidationException {
        for (Injector injector : injectors) {
            injector.inject(resource, javaImpl, ctx);
        }
        try {
            ValidationUtils.validate(javaImpl, ResourceValidationException.class);
        } catch (ResourceValidationException e) {
            throw new ResourceValidationException("Failed to validate '" + resource.toString() + "' " + e.getMessage());
        }
    }

    protected void updateAttrs(Resource resource, Object javaImpl) throws InvalidAttributeException {
        for (Injector injector : injectors) {
            try {
                injector.updateAttr(resource, javaImpl);
            } catch (IllegalAccessException e) {
                throw new InvalidAttributeException("Unable to set field "+injector.field.getName()+" of class "+implClass.getName());
            }
        }
    }


    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public Method getVerifyMethod() {
        return verifyMethod;
    }

    public void setVerifyMethod(Method verifyMethod) {
        this.verifyMethod = verifyMethod;
    }
}
