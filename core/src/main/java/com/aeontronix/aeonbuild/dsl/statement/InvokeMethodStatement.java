/*
 * Copyright (c) 2024 Aeontronix Inc
 */

package com.aeontronix.aeonbuild.dsl.statement;

import com.aeontronix.aeonbuild.exception.KMRuntimeException;
import com.aeontronix.aeonbuild.BuildContextImpl;
import com.aeontronix.aeonbuild.Parameters;
import com.aeontronix.aeonbuild.Resource;
import com.aeontronix.aeonbuild.dsl.AntLRUtils;
import com.aeontronix.aeonbuild.dsl.DSLScript;
import com.aeontronix.aeonbuild.dsl.InvalidScriptException;
import com.aeontronix.aeonbuild.dsl.AeonBuildLangParser;
import org.antlr.v4.runtime.Token;

import javax.script.ScriptException;
import java.util.List;

public class InvokeMethodStatement extends Statement {
    private final Token token;
    private String methodName;
    private Parameters parameters;
    private BuildContextImpl ctx;

    public InvokeMethodStatement(BuildContextImpl ctx, AeonBuildLangParser.InvokeMethodContext invokeMethodContext) throws InvalidScriptException {
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
