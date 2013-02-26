/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.systyrant.service;

import com.kloudtek.systyrant.STContext;
import com.kloudtek.systyrant.annotation.Default;
import com.kloudtek.systyrant.annotation.Param;
import com.kloudtek.systyrant.dsl.Parameter;
import com.kloudtek.systyrant.dsl.Parameters;
import com.kloudtek.systyrant.exception.InvalidServiceException;
import com.kloudtek.systyrant.exception.MethodInvocationException;
import com.kloudtek.systyrant.exception.STRuntimeException;
import org.apache.commons.beanutils.ConvertUtils;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MethodInvoker {
    private java.lang.String serviceName;
    private final java.lang.String name;
    private final Method method;
    private final int paramCount;
    private ArrayList<java.lang.String> paramsOrder = new ArrayList<>();
    private LinkedHashMap<java.lang.String, MethodParam> paramsMap = new LinkedHashMap<>();

    public MethodInvoker(java.lang.String serviceName, java.lang.String name, Method method) throws InvalidServiceException {
        this.serviceName = serviceName;
        this.name = name;
        this.method = method;
        Annotation[][] annotations = method.getParameterAnnotations();
        paramCount = annotations.length;
        for (int i = 0; i < annotations.length; i++) {
            Class<?>[] parameterTypes = method.getParameterTypes();
            Annotation[] anlist = annotations[i];
            MethodParam param = new MethodParam(method, parameterTypes[i], anlist);
            paramsMap.put(param.name, param);
            paramsOrder.add(param.name);
        }
    }

    public Object invoke(STContext ctx, @NotNull Parameters params) throws STRuntimeException {
        Object[] plist = new Object[paramCount];
        List<Parameter> parameters = params.getParameters();
        if (parameters.size() > paramCount) {
            throw new MethodInvocationException("Too many arguments provided when calling method " + name);
        }
        for (int i = 0; i < parameters.size(); i++) {
            Parameter p = parameters.get(i);
            String parameterValue = p.eval(ctx, null);
            plist[i] = ConvertUtils.convert(parameterValue, paramsMap.get(paramsOrder.get(i)).type);
        }
        for (Map.Entry<java.lang.String, Parameter> entry : params.getNamedParameters().entrySet()) {
            int idx = paramsOrder.indexOf(entry.getKey());
            if (idx == -1) {
                throw new MethodInvocationException("Invalid argument " + entry.getKey() + " provided when calling method " + name);
            }
            if (plist[idx] != null) {
                throw new MethodInvocationException("Multiple assignments of argument " + entry.getKey() + " when calling method " + name);
            }
            String paramValue = entry.getValue().eval(ctx, null);
            Class<?> type = paramsMap.get(paramsOrder.get(idx)).type;
            plist[idx] = ConvertUtils.convert(paramValue, type);
        }
        Object service = ctx.getServiceManager().getService(serviceName);
        if (service == null) {
            throw new STRuntimeException("BUG: Couldn't find service " + serviceName);
        }
        try {
            return method.invoke(service, plist);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new STRuntimeException(e.getMessage(), e);
        }
    }

    public class MethodParam {
        private java.lang.String name;
        private java.lang.String def;
        private Class<?> type;

        public MethodParam(Method method, Class<?> type, Annotation[] annotations) throws InvalidServiceException {
            this.type = type;
            for (Annotation annotation : annotations) {
                if (annotation instanceof Param) {
                    if (name != null) {
                        throw new InvalidServiceException(toString(method) + " has more than one @Method annotation");
                    }
                    name = ((Param) annotation).value();
                } else if (annotation instanceof Default) {
                    if (def != null) {
                        throw new InvalidServiceException(toString(method) + " has more than one @Default annotation");
                    }
                    def = ((Default) annotation).value();
                }
            }
            if (name == null) {
                throw new InvalidServiceException("No @Method specified for " + toString(method));
            }
        }

        private java.lang.String toString(Method method) {
            return "Method " + method.getClass().getName() + "#" + method.getName();
        }
    }
}
