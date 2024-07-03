/*
 * Copyright (c) 2024 Aeontronix Inc
 */

package com.aeontronix.aeonbuild.dsl.statement;

import com.aeontronix.aeonbuild.BuildContextImpl;
import com.aeontronix.aeonbuild.Resource;
import com.aeontronix.aeonbuild.dsl.DSLScript;
import com.aeontronix.aeonbuild.dsl.InvalidScriptException;
import com.aeontronix.aeonbuild.dsl.AeonBuildLangParser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.script.ScriptException;
import java.util.List;

public abstract class Statement {
    @Nullable
    public abstract List<Resource> execute(@NotNull DSLScript dslScript, @NotNull Resource resource) throws ScriptException;

    public static Statement create(BuildContextImpl ctx, AeonBuildLangParser.StatementContext statementContext) throws InvalidScriptException {
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
