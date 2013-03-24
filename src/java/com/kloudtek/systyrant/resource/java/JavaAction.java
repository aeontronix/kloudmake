/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.systyrant.resource.java;

import com.kloudtek.systyrant.STContext;
import com.kloudtek.systyrant.exception.*;
import com.kloudtek.systyrant.resource.AbstractAction;
import com.kloudtek.systyrant.resource.Injector;
import com.kloudtek.systyrant.resource.Resource;
import com.kloudtek.systyrant.util.ReflectionHelper;
import com.kloudtek.util.validation.ValidationUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;

public class JavaAction extends AbstractAction {
    private Class<?> implClass;
    private final List<Injector> injectors;
    @NotNull
    private final Set<EnforceOnlyIf> onlyIf;
    private Method method;
    private Method verifyMethod;

    public JavaAction(int order, Type type, Class<?> implClass, List<Injector> injectors,
                      @NotNull Set<EnforceOnlyIf> onlyIf, Method method) throws InvalidResourceDefinitionException {
        this(order, type, implClass, injectors, onlyIf, method, null);
    }

    public JavaAction(int order, @NotNull Type type, @NotNull Class<?> implClass, @NotNull List<Injector> injectors,
                      @NotNull Set<EnforceOnlyIf> onlyIf, @Nullable Method method, @Nullable Method verifyMethod) throws InvalidResourceDefinitionException {
        super(order, type);
        this.implClass = implClass;
        this.injectors = injectors;
        this.onlyIf = onlyIf;
        setMethod(method);
        setVerifyMethod(verifyMethod);
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
        Object ret = ReflectionHelper.invoke(method,javaImpl);
        updateAttrs(resource, javaImpl);
        return ret;
    }

    @Override
    public boolean checkExecutionRequired(STContext context, Resource resource) throws STRuntimeException {
        if( checkOnlyIf(context,resource) ) {
            return false;
        }
        if (verifyMethod != null) {
            return (boolean) invoke(context, resource, verifyMethod);
        }
        return true;
    }

    private boolean checkOnlyIf(STContext context, Resource resource) throws STRuntimeException {
        for (EnforceOnlyIf enforceOnlyIf : onlyIf) {
            if( ! enforceOnlyIf.execAllowed(context, resource) ) {
                return true;
            }
        }
        return false;
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
                throw new InvalidAttributeException("Unable to set field "+injector.getField().getName()+" of class "+implClass.getName());
            }
        }
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) throws InvalidResourceDefinitionException {
        if( this.method != null ) {
            throw new IllegalArgumentException("Cannot override method "+ReflectionHelper.toString(this.method));
        }
        this.method = method;
        if( method != null ) {
            onlyIf.addAll(EnforceOnlyIf.find(method,implClass));
        }
    }

    public Method getVerifyMethod() {
        return verifyMethod;
    }

    public void setVerifyMethod(Method verifyMethod) throws InvalidResourceDefinitionException {
        if( this.verifyMethod != null ) {
            throw new IllegalArgumentException("Cannot override method "+ReflectionHelper.toString(this.verifyMethod));
        }
        this.verifyMethod = verifyMethod;
        if( verifyMethod != null ) {
            onlyIf.addAll(EnforceOnlyIf.find(verifyMethod,implClass));
        }
    }
}
