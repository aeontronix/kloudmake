/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.systyrant.dsl;

import com.kloudtek.systyrant.STContext;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.Collections;
import java.util.List;

public class AntlrDSLParser implements DSLParser {
    @Override
    public DSLScript parse(STContext ctx, String script) throws InvalidScriptException {
        return parse(ctx, null, script);
    }

    @Override
    public DSLScript parse(STContext ctx, String pkg, String script) throws InvalidScriptException {
        SystyrantLangParser parser = createParser(script);
        try {
            return parseRoot(ctx, pkg, parser.start());
        } catch (ParseCancellationException e) {
            if (e.getCause() instanceof InputMismatchException) {
                InputMismatchException cause = (InputMismatchException) e.getCause();
                Token offendingToken = cause.getOffendingToken();
                throw new InvalidScriptException(offendingToken.getLine() + ":" + offendingToken.getCharPositionInLine(), offendingToken.getText(), cause);
            } else {
                throw new InvalidScriptException(e.getMessage(), e);
            }
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

    public static SystyrantLangParser createParser(String script) {
        SystyrantLangLexer lexer = new SystyrantLangLexer(new ANTLRInputStream(script));
        SystyrantLangParser parser = new SystyrantLangParser(new CommonTokenStream(lexer));
        parser.setErrorHandler(new BailErrorStrategy());
        return parser;
    }
}
