/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.systyrant.dsl.statement;

import com.kloudtek.systyrant.Resource;
import com.kloudtek.systyrant.STContext;
import com.kloudtek.systyrant.dsl.DSLScript;
import com.kloudtek.systyrant.dsl.InvalidScriptException;
import com.kloudtek.systyrant.dsl.Parameter;
import com.kloudtek.systyrant.dsl.SystyrantLangParser;
import com.kloudtek.systyrant.exception.STRuntimeException;

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
    private final STContext ctx;
    private String name;
    private Parameter param;

    public AssignVariableStatement(STContext ctx, SystyrantLangParser.AssignVariableStatementContext vctx) throws InvalidScriptException {
        this.ctx = ctx;
        name = vctx.var.getText();
        param = Parameter.create(vctx.val);
    }

    @Override
    public List<Resource> execute(DSLScript dslScript, Resource resource) throws ScriptException {
        try {
            resource.setVar(name, param.eval(ctx, resource));
            return null;
        } catch (STRuntimeException e) {
            throw new ScriptException(e);
        }
    }
}
