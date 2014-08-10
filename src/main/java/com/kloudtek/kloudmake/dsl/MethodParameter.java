/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.kloudmake.dsl;

import com.kloudtek.kloudmake.Parameters;
import com.kloudtek.kloudmake.Resource;
import com.kloudtek.kloudmake.STContext;
import com.kloudtek.kloudmake.exception.STRuntimeException;

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

    public MethodParameter(KloudmakeLangParser.InvokeMethodContext ctx) throws InvalidScriptException {
        raw = ctx.getText();
        name = ctx.methodName.getText();
        List<KloudmakeLangParser.ParameterContext> parameterContexts = ctx.parameter();
        for (KloudmakeLangParser.ParameterContext pCtx : parameterContexts) {
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
    public String eval(STContext ctx, Resource resource) throws STRuntimeException {
        return ctx.invokeMethod(name, params).toString();
    }
}

