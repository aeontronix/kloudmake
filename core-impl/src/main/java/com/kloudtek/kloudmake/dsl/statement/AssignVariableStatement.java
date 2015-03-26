/*
 * Copyright (c) 2015. Kelewan Technologies Ltd
 */

package com.kloudtek.kloudmake.dsl.statement;

import com.kloudtek.kloudmake.KMContextImpl;
import com.kloudtek.kloudmake.Resource;
import com.kloudtek.kloudmake.dsl.DSLScript;
import com.kloudtek.kloudmake.dsl.InvalidScriptException;
import com.kloudtek.kloudmake.dsl.KloudmakeLangParser;
import com.kloudtek.kloudmake.dsl.Parameter;
import com.kloudtek.kloudmake.exception.KMRuntimeException;

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
    private final KMContextImpl ctx;
    private String name;
    private Parameter param;

    public AssignVariableStatement(KMContextImpl ctx, KloudmakeLangParser.AssignVariableStatementContext vctx) throws InvalidScriptException {
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
