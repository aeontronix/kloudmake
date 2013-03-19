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
import java.util.List;

public abstract class Statement {
    public abstract List<Resource> execute(DSLScript dslScript, Resource resource) throws ScriptException;

    public static Statement create(STContext ctx, SystyrantLangParser.StatementContext statementContext) throws InvalidScriptException {
        if (statementContext.create != null) {
            if (statementContext.create.llk != null || statementContext.create.rlk != null ) {
                return new DepLinkedCreateResourceStatement(ctx, statementContext.create);
            } else {
                return new CreateResourceStatement(ctx, statementContext.create);
            }
        }
        if (statementContext.invoke != null) {
            return new InvokeMethodStatement(ctx, statementContext.invoke);
        }
        throw new RuntimeException("Statement not recognized");
    }
}
