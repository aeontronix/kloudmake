/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.systyrant.dsl;

import com.kloudtek.systyrant.STContext;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.ParseCancellationException;

public class AntlrDSLParser implements DSLParser {


    @Override
    public DSLScript parse(STContext ctx, String script) throws InvalidScriptException {
        return parse(ctx, null, script);
    }

    @Override
    public DSLScript parse(STContext ctx, String pkg, String script) throws InvalidScriptException {
        SystyrantLangParser parser = createOldParser(script);
        try {
            return parseRoot(ctx, pkg, parser.start());
        } catch (ParseCancellationException e) {
            return handleException(e);
        }
    }

    private DSLScript handleException(ParseCancellationException e) throws InvalidScriptException {
        if (e.getCause() instanceof InputMismatchException) {
            InputMismatchException cause = (InputMismatchException) e.getCause();
            Token offendingToken = cause.getOffendingToken();
            throw new InvalidScriptException(offendingToken.getLine() + ":" + offendingToken.getCharPositionInLine(), offendingToken.getText(), cause);
        } else {
            throw new InvalidScriptException(e.getMessage(), e);
        }
    }

    @Override
    public DSLScript parse(String script) throws InvalidScriptException {
        return parse(STContext.get(), script);
    }

    @Override
    public DSLScript parse(String pkg, String script) throws InvalidScriptException {
        return parse(STContext.get(), script);
    }

    private DSLScript parseRoot(STContext ctx, String pkg, SystyrantLangParser.StartContext start) throws InvalidScriptException {
        return new DSLScript(ctx, pkg, start);
    }

    public static SystyrantLangParser createOldParser(String script) {
        SystyrantLangLexer lexer = new SystyrantLangLexer(new ANTLRInputStream(script));
        SystyrantLangParser parser = new SystyrantLangParser(new CommonTokenStream(lexer));
        parser.setErrorHandler(new BailErrorStrategy());
        return parser;
    }

    public static SysTyrantDSLParser createParser(String script) {
        SysTyrantDSLLexer lexer = new SysTyrantDSLLexer(new ANTLRInputStream(script));
        SysTyrantDSLParser parser = new SysTyrantDSLParser(new CommonTokenStream(lexer));
        parser.setErrorHandler(new BailErrorStrategy());
        return parser;
    }
}
