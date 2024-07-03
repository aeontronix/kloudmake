/*
 * Copyright (c) 2024 Aeontronix Inc
 */

package com.aeontronix.aeonbuild.dsl;

import com.aeontronix.aeonbuild.exception.KMRuntimeException;
import com.aeontronix.aeonbuild.BuildContextImpl;
import com.aeontronix.aeonbuild.Parameters;
import com.aeontronix.aeonbuild.Resource;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: yinkaf
 * Date: 25/02/2013
 * Time: 01:29
 * To change this template use File | Settings | File Templates.
 */
public class MethodParameter extends Parameter {
    private String raw;
    private final String name;
    private Parameters params = new Parameters();

    public MethodParameter(AeonBuildLangParser.InvokeMethodContext ctx) throws InvalidScriptException {
        raw = ctx.getText();
        name = ctx.methodName.getText();
        List<AeonBuildLangParser.ParameterContext> parameterContexts = ctx.parameter();
        for (AeonBuildLangParser.ParameterContext pCtx : parameterContexts) {
            Parameter parameter = Parameter.create(pCtx.staticOrDynamicValue());
            if (pCtx.id != null) {
                params.addNamedParameter(pCtx.getText(), parameter);
            } else {
                params.addParameter(pCtx.getStart(), parameter);
            }
        }
    }

    @Override
    public String getRawValue() {
        return raw;
    }

    @Override
    public String eval(BuildContextImpl ctx, Resource resource) throws KMRuntimeException {
        return ctx.invokeMethod(name, params).toString();
    }
}

