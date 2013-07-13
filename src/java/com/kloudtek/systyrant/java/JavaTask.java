/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.systyrant.java;

import com.kloudtek.systyrant.*;
import com.kloudtek.systyrant.annotation.Alternative;
import com.kloudtek.systyrant.exception.*;
import com.kloudtek.systyrant.inject.Injector;
import com.kloudtek.systyrant.util.ReflectionHelper;
import com.kloudtek.util.validation.ValidationUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class JavaTask extends AbstractTask {
    private Class<?> implClass;
    private final List<Injector> injectors;
    @NotNull
    private final Set<EnforceOnlyIf> onlyIf = new HashSet<>();
    private Method method;
    private Method verifyMethod;

    public JavaTask(int order, Stage stage, boolean postChildren, Class<?> implClass, List<Injector> injectors,
                    @NotNull Set<EnforceOnlyIf> onlyIf, Method method) throws InvalidResourceDefinitionException {
        this(order, stage, postChildren, implClass, injectors, onlyIf, method, null);
    }

    public JavaTask(int order, @NotNull Stage stage, boolean postChildren, @NotNull Class<?> implClass, @NotNull List<Injector> injectors,
                    @NotNull Set<EnforceOnlyIf> onlyIf, @Nullable Method method, @Nullable Method verifyMethod) throws InvalidResourceDefinitionException {
        super(order, stage, postChildren);
        this.implClass = implClass;
        this.injectors = injectors;
        this.onlyIf.addAll(onlyIf);
        setMethod(method);
        setVerifyMethod(verifyMethod);
    }

    @Override
    public void execute(STContext context, Resource resource) throws STRuntimeException {
        invoke(context, resource, method);
    }

    private Object invoke(STContext context, Resource resource, Method method) throws STRuntimeException {
        Object javaImpl = resource.getJavaImpl(implClass);
        if (javaImpl == null) {
            try {
                javaImpl = implClass.newInstance();
                ((ResourceImpl) resource).addJavaImpl(javaImpl);
            } catch (InstantiationException | IllegalAccessException e) {
                throw new STRuntimeException("Unable to create class " + implClass.getName() + " " + e.getMessage(), e);
            }
        }
        assert javaImpl != null;
        injectAndValidate(resource, javaImpl, context);
        Object ret = ReflectionHelper.invoke(method, javaImpl);
        updateAttrs(resource, javaImpl);
        return ret;
    }

    @Override
    public boolean checkExecutionRequired(STContext context, Resource resource) throws STRuntimeException {
        if (verifyMethod != null) {
            return (boolean) invoke(context, resource, verifyMethod);
        }
        return true;
    }

    @Override
    public boolean supports(STContext context, Resource resource) throws STRuntimeException {
        if (onlyIf != null && !onlyIf.isEmpty()) {
            for (EnforceOnlyIf enforceOnlyIf : onlyIf) {
                if (!enforceOnlyIf.execAllowed(context, resource)) {
                    return false;
                }
            }
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
                throw new InvalidAttributeException("Unable to set field " + injector.getField().getName() + " of class " + implClass.getName());
            }
        }
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) throws InvalidResourceDefinitionException {
        if (this.method != null) {
            throw new IllegalArgumentException("Cannot override method " + ReflectionHelper.toString(this.method));
        }
        this.method = method;
        if (method != null) {
            this.onlyIf.addAll(EnforceOnlyIf.find(method));
            Alternative altAnno = method.getAnnotation(Alternative.class);
            if (altAnno != null) {
                alternative = altAnno.value();
            }
        }
    }

    public Method getVerifyMethod() {
        return verifyMethod;
    }

    public void setVerifyMethod(Method verifyMethod) throws InvalidResourceDefinitionException {
        if (this.verifyMethod != null) {
            throw new IllegalArgumentException("Cannot override method " + ReflectionHelper.toString(this.verifyMethod));
        }
        this.verifyMethod = verifyMethod;
    }

    private void handleAlternativeAnnotation(Alternative annotation) {
        if (annotation != null) {
            alternative = annotation.value();
        }
    }

}
