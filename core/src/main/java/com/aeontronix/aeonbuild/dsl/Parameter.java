/*
 * Copyright (c) 2024 Aeontronix Inc
 */

package com.aeontronix.aeonbuild.dsl;

import com.aeontronix.aeonbuild.exception.KMRuntimeException;
import com.aeontronix.aeonbuild.BuildContextImpl;
import com.aeontronix.aeonbuild.Resource;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.aeontronix.aeonbuild.dsl.AntLRUtils.nullToEmpty;

public abstract class Parameter {
    public static Parameter create(AeonBuildLangParser.StaticOrDynamicValueContext vctx) throws InvalidScriptException {
        if (vctx.st != null) {
            return new StaticParameter(AntLRUtils.toString(vctx.st));
        }
        if (vctx.dyn != null) {
            AeonBuildLangParser.VariableLookupValueContext variableLookupValueContext = vctx.dyn.variableLookupValue();
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

    public static Map<String, Parameter> assignmentToMap(List<AeonBuildLangParser.ParameterAssignmentContext> parameterAssignmentContexts) throws InvalidScriptException {
        HashMap<String, Parameter> map = new HashMap<>();
        for (AeonBuildLangParser.ParameterAssignmentContext parameterAssignmentContext : nullToEmpty(parameterAssignmentContexts)) {
            String attrName = parameterAssignmentContext.anyId().getText();
            AeonBuildLangParser.StaticOrDynamicValueContext staticOrDynamicValueContext = parameterAssignmentContext.staticOrDynamicValue();
            map.put(attrName, create(staticOrDynamicValueContext));
        }
        return map;
    }

    public abstract String getRawValue();

    public abstract String eval(BuildContextImpl ctx, Resource resource) throws KMRuntimeException;
}
