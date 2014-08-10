/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.kloudmake.dsl;

import com.kloudtek.kloudmake.Resource;
import com.kloudtek.kloudmake.STContext;
import com.kloudtek.kloudmake.exception.STRuntimeException;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.kloudtek.kloudmake.dsl.AntLRUtils.nullToEmpty;

public abstract class Parameter {
    public static Parameter create(KloudmakeLangParser.StaticOrDynamicValueContext vctx) throws InvalidScriptException {
        if (vctx.st != null) {
            return new StaticParameter(AntLRUtils.toString(vctx.st));
        }
        if (vctx.dyn != null) {
            KloudmakeLangParser.VariableLookupValueContext variableLookupValueContext = vctx.dyn.variableLookupValue();
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

    public static Map<String, Parameter> assignmentToMap(List<KloudmakeLangParser.ParameterAssignmentContext> parameterAssignmentContexts) throws InvalidScriptException {
        HashMap<String, Parameter> map = new HashMap<>();
        for (KloudmakeLangParser.ParameterAssignmentContext parameterAssignmentContext : nullToEmpty(parameterAssignmentContexts)) {
            String attrName = parameterAssignmentContext.anyId().getText();
            KloudmakeLangParser.StaticOrDynamicValueContext staticOrDynamicValueContext = parameterAssignmentContext.staticOrDynamicValue();
            map.put(attrName, create(staticOrDynamicValueContext));
        }
        return map;
    }

    public abstract String getRawValue();

    public abstract String eval(STContext ctx, Resource resource) throws STRuntimeException;
}
