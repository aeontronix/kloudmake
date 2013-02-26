/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.systyrant.dsl.statement;

import com.kloudtek.systyrant.STContext;
import com.kloudtek.systyrant.dsl.DSLScript;
import com.kloudtek.systyrant.dsl.InvalidScriptException;
import com.kloudtek.systyrant.dsl.SystyrantLangParser;
import com.kloudtek.systyrant.resource.Resource;

import javax.script.ScriptException;

public abstract class Statement {
    public abstract void execute(DSLScript dslScript, Resource resource) throws ScriptException;

    public static Statement create(STContext ctx, SystyrantLangParser.StatementContext statementContext) throws InvalidScriptException {
        SystyrantLangParser.CreateResourceContext createResourceContext = statementContext.createResource();
        if (createResourceContext != null) {
            return new CreateElementsStatement(ctx, createResourceContext);
        }
        SystyrantLangParser.InvokeMethodContext invokeMethodContext = statementContext.invokeMethod();
        if (invokeMethodContext != null) {
            return new InvokeMethodStatement(ctx, invokeMethodContext);
        }
        throw new RuntimeException("Statement not recognized");
    }
}
