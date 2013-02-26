/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.systyrant.dsl;

import com.kloudtek.systyrant.STContext;
import com.kloudtek.systyrant.resource.Resource;

public abstract class Parameter {
    public static Parameter create(SystyrantLangParser.StaticOrDynamicValueContext assignmentValueContext) {
        SystyrantLangParser.StaticValueContext staticValueContext = assignmentValueContext.staticValue();
        SystyrantLangParser.DynamicValueContext dynamicValueContext = assignmentValueContext.dynamicValue();
        if (staticValueContext != null) {
            return new StaticParameter(AntlrDSLParser.toString(staticValueContext));
        }

        if (dynamicValueContext != null) {
            SystyrantLangParser.AnyIdContext anyIdContext = dynamicValueContext.anyId();
            if (anyIdContext != null) {
                return new DynamicParameter(anyIdContext.getText());
            }
        }
        throw new RuntimeException("Bug: staticOrDynamicValueContext has no valid data");
    }

    public abstract String eval(STContext ctx, Resource resource);
}
