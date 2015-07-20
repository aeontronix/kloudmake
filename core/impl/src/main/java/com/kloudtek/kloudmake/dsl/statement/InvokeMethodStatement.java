/*
 * Copyright (c) 2015. Kelewan Technologies Ltd
 */

package com.kloudtek.kloudmake.dsl.statement;

import com.kloudtek.kloudmake.KMContextImpl;
import com.kloudtek.kloudmake.Parameters;
import com.kloudtek.kloudmake.Resource;
import com.kloudtek.kloudmake.dsl.AntLRUtils;
import com.kloudtek.kloudmake.dsl.DSLScript;
import com.kloudtek.kloudmake.dsl.InvalidScriptException;
import com.kloudtek.kloudmake.dsl.KloudmakeLangParser;
import com.kloudtek.kloudmake.exception.KMRuntimeException;
import org.antlr.v4.runtime.Token;

import javax.script.ScriptException;
import java.util.List;

public class InvokeMethodStatement extends Statement {
    private final Token token;
    private String methodName;
    private Parameters parameters;
    private KMContextImpl ctx;

    public InvokeMethodStatement(KMContextImpl ctx, KloudmakeLangParser.InvokeMethodContext invokeMethodContext) throws InvalidScriptException {
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
        } catch (KMRuntimeException e) {
            ScriptException scriptException = new ScriptException(e.getMessage(), null, token.getLine(), token.getCharPositionInLine());
            scriptException.initCause(e);
            throw scriptException;
        }
        return null;
    }
}
