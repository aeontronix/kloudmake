/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.kloudmake.dsl;

import com.kloudtek.kloudmake.Resource;
import com.kloudtek.kloudmake.STContext;
import com.kloudtek.kloudmake.exception.InvalidVariableException;

/**
 * Created with IntelliJ IDEA.
 * User: yinkaf
 * Date: 25/02/2013
 * Time: 01:29
 * To change this template use File | Settings | File Templates.
 */
public class VariableParameter extends Parameter {
    String parameterId;

    public VariableParameter() {
    }

    public VariableParameter(String parameterId) {
        this.parameterId = parameterId;
    }

    public String getParameterId() {
        return parameterId;
    }

    public void setParameterId(String param) {
        this.parameterId = param;
    }

    @Override
    public String getRawValue() {
        return parameterId;
    }

    @Override
    public String eval(STContext ctx, Resource resource) throws InvalidVariableException {
        return resolveVariable(resource, parameterId);
    }

    public static String resolveVariable(Resource resource, String attr) throws InvalidVariableException {
        Object var = resource.getVar(attr);
        if (var != null) {
            return var.toString();
        } else {
            throw new InvalidVariableException("Unable to find variable " + attr);
        }
    }
}
