/*
 * Copyright (c) 2024 Aeontronix Inc
 */

package com.aeontronix.aeonbuild.dsl;

import com.aeontronix.aeonbuild.exception.InvalidVariableException;
import com.aeontronix.aeonbuild.BuildContextImpl;
import com.aeontronix.aeonbuild.Resource;

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
    public String eval(BuildContextImpl ctx, Resource resource) throws InvalidVariableException {
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
