/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.systyrant.dsl;

import com.kloudtek.systyrant.Resource;
import com.kloudtek.systyrant.STContext;
import com.kloudtek.systyrant.exception.STRuntimeException;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.kloudtek.systyrant.dsl.AntLRUtils.nullToEmpty;

public abstract class Parameter {
    public static Parameter create(SystyrantLangParser.StaticOrDynamicValueContext vctx) throws InvalidScriptException {
        if (vctx.st != null) {
            return new StaticParameter(AntLRUtils.toString(vctx.st));
        }
        if (vctx.dyn != null) {
            SystyrantLangParser.VariableLookupValueContext variableLookupValueContext = vctx.dyn.variableLookupValue();
            if (variableLookupValueContext != null) {
                return new VariableParameter(variableLookupValueContext.getText().substring(1));
            }
            TerminalNode stringWithVars = vctx.dyn.ASTRING();
            if (stringWithVars != null) {
                String text = stringWithVars.getText();
                return new StringWithVariablesParameter(text.substring(1, text.length() - 1));
            }
        }
        if (vctx.iv != null) {
            return new MethodParameter(vctx.iv);
        }
        throw new RuntimeException("Bug: staticOrDynamicValueContext has no valid data");
    }

    public static Map<String, Parameter> assignmentToMap(List<SystyrantLangParser.ParameterAssignmentContext> parameterAssignmentContexts) throws InvalidScriptException {
        HashMap<String, Parameter> map = new HashMap<>();
        for (SystyrantLangParser.ParameterAssignmentContext parameterAssignmentContext : nullToEmpty(parameterAssignmentContexts)) {
            String attrName = parameterAssignmentContext.anyId().getText();
            SystyrantLangParser.StaticOrDynamicValueContext staticOrDynamicValueContext = parameterAssignmentContext.staticOrDynamicValue();
            map.put(attrName, create(staticOrDynamicValueContext));
        }
        return map;
    }

    public abstract String getRawValue();

    public abstract String eval(STContext ctx, Resource resource) throws STRuntimeException;
}
