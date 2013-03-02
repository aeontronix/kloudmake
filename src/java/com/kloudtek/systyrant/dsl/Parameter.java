/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.systyrant.dsl;

import com.kloudtek.systyrant.STContext;
import com.kloudtek.systyrant.exception.InvalidVariableException;
import com.kloudtek.systyrant.resource.Resource;
import org.antlr.v4.runtime.tree.TerminalNode;

public abstract class Parameter {
    public static Parameter create(SystyrantLangParser.StaticOrDynamicValueContext assignmentValueContext) {
        SystyrantLangParser.StaticValueContext staticValueContext = assignmentValueContext.staticValue();
        if (staticValueContext != null) {
            return new StaticParameter(AntlrDSLParser.toString(staticValueContext));
        }
        SystyrantLangParser.DynamicValueContext dynamicValueContext = assignmentValueContext.dynamicValue();
        if (dynamicValueContext != null) {
            SystyrantLangParser.VariableLookupValueContext variableLookupValueContext = dynamicValueContext.variableLookupValue();
            if (variableLookupValueContext != null) {
                return new VariableParameter(variableLookupValueContext.getText().substring(1));
            }
            TerminalNode stringWithVars = dynamicValueContext.ASTRING();
            if (stringWithVars != null) {
                String text = stringWithVars.getText();
                return new StringWithVariablesParameter(text.substring(1, text.length() - 1));
            }
        }
        throw new RuntimeException("Bug: staticOrDynamicValueContext has no valid data");
    }

    public abstract String getRawValue();

    public abstract String eval(STContext ctx, Resource resource) throws InvalidVariableException;
}
