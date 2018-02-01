/*
 * Copyright (c) 2015. Kelewan Technologies Ltd
 */

package com.kloudtek.kloudmake.dsl.statement;

import com.kloudtek.kloudmake.KMContextImpl;
import com.kloudtek.kloudmake.Resource;
import com.kloudtek.kloudmake.dsl.DSLScript;
import com.kloudtek.kloudmake.dsl.InvalidScriptException;
import com.kloudtek.kloudmake.dsl.KloudmakeLangParser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.script.ScriptException;
import java.util.List;

public abstract class Statement {
    @Nullable
    public abstract List<Resource> execute(@NotNull DSLScript dslScript, @NotNull Resource resource) throws ScriptException;

    public static Statement create(KMContextImpl ctx, KloudmakeLangParser.StatementContext statementContext) throws InvalidScriptException {
        if (statementContext.create != null) {
            if (statementContext.create.llk != null || statementContext.create.rlk != null) {
                return new DepLinkedCreateResourceStatement(ctx, statementContext.create);
            } else {
                return new CreateResourceStatement(ctx, statementContext.create);
            }
        } else if (statementContext.invoke != null) {
            return new InvokeMethodStatement(ctx, statementContext.invoke);
        } else if (statementContext.asvar != null) {
            return new AssignVariableStatement(ctx, statementContext.asvar);
        }
        throw new RuntimeException("Statement not recognized");
    }
}
