/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.systyrant.dsl;

import com.kloudtek.systyrant.STContext;
import com.kloudtek.systyrant.exception.InvalidVariableException;
import com.kloudtek.systyrant.resource.Resource;

import static com.kloudtek.systyrant.dsl.VariableParameter.resolveVariable;

/**
 * Created with IntelliJ IDEA.
 * User: yinkaf
 * Date: 25/02/2013
 * Time: 01:29
 * To change this template use File | Settings | File Templates.
 */
public class StringWithVariablesParameter extends Parameter {
    private String txt;

    public StringWithVariablesParameter() {
    }

    public StringWithVariablesParameter(String txt) {
        this.txt = txt;
    }

    public String getTxt() {
        return txt;
    }

    public void setTxt(String param) {
        this.txt = param;
    }

    @Override
    public String getRawValue() {
        return txt;
    }

    @Override
    public String eval(STContext ctx, Resource resource) throws InvalidVariableException {
        boolean escaping = false;
        StringBuilder buf = new StringBuilder();
        StringBuilder var = null;
        char[] chars = txt.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            if (var != null) {
                if (c == '}') {
                    buf.append(resolveVariable(resource, var.toString()));
                    var = null;
                } else {
                    var.append(c);
                }
            } else {
                if (c == '$' && i + 1 < chars.length && chars[i + 1] == '{' && !escaping) {
                    var = new StringBuilder();
                    i++;
                } else {
                    if (!escaping && c == '\\') {
                        escaping = true;
                    } else {
                        if (escaping) {
                            escaping = false;
                        }
                        buf.append(c);
                    }
                }
            }
        }
        if (var != null) {
            throw new InvalidVariableException("Variable substitution missing closing bracket: " + txt);
        }
        return buf.toString();
    }
}

