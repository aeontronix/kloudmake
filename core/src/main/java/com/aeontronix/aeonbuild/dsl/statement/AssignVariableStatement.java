/*
 * Copyright (c) 2024 Aeontronix Inc
 */

package com.aeontronix.aeonbuild.dsl.statement;

import com.aeontronix.aeonbuild.exception.KMRuntimeException;
import com.aeontronix.aeonbuild.BuildContextImpl;
import com.aeontronix.aeonbuild.Resource;
import com.aeontronix.aeonbuild.dsl.DSLScript;
import com.aeontronix.aeonbuild.dsl.InvalidScriptException;
import com.aeontronix.aeonbuild.dsl.AeonBuildLangParser;
import com.aeontronix.aeonbuild.dsl.Parameter;

import javax.script.ScriptException;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: yannick
 * Date: 13/04/13
 * Time: 23:48
 * To change this template use File | Settings | File Templates.
 */
public class AssignVariableStatement extends Statement {
    private final BuildContextImpl ctx;
    private String name;
    private Parameter param;

    public AssignVariableStatement(BuildContextImpl ctx, AeonBuildLangParser.AssignVariableStatementContext vctx) throws InvalidScriptException {
        this.ctx = ctx;
        name = vctx.var.getText();
        param = Parameter.create(vctx.val);
    }

    @Override
    public List<Resource> execute(DSLScript dslScript, Resource resource) throws ScriptException {
        try {
            resource.setVar(name, param.eval(ctx, resource));
            return null;
        } catch (KMRuntimeException e) {
            throw new ScriptException(e);
        }
    }
}
