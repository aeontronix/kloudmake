/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.systyrant.dsl.statement;

import com.kloudtek.systyrant.Parameters;
import com.kloudtek.systyrant.Resource;
import com.kloudtek.systyrant.STContext;
import com.kloudtek.systyrant.dsl.AntLRUtils;
import com.kloudtek.systyrant.dsl.DSLScript;
import com.kloudtek.systyrant.dsl.InvalidScriptException;
import com.kloudtek.systyrant.dsl.SystyrantLangParser;
import com.kloudtek.systyrant.exception.STRuntimeException;
import org.antlr.v4.runtime.Token;

import javax.script.ScriptException;
import java.util.List;

public class InvokeMethodStatement extends Statement {
    private final Token token;
    private String methodName;
    private Parameters parameters;
    private STContext ctx;

    public InvokeMethodStatement(STContext ctx, SystyrantLangParser.InvokeMethodContext invokeMethodContext) throws InvalidScriptException {
        this.ctx = ctx;
        token = invokeMethodContext.getStart();
        methodName = invokeMethodContext.methodName.getText();
        parameters = AntLRUtils.toParams(invokeMethodContext.parameter());
    }

    public String getMethodName() {
        return methodName;
    }

    public Parameters getParameters() {
        return parameters;
    }

    @Override
    public List<Resource> execute(DSLScript dslScript, Resource resource) throws ScriptException {
        try {
            ctx.getServiceManager().invokeMethod(methodName, parameters);
        } catch (STRuntimeException e) {
            ScriptException scriptException = new ScriptException(e.getMessage(), null, token.getLine(), token.getCharPositionInLine());
            scriptException.initCause(e);
            throw scriptException;
        }
        return null;
    }
}
