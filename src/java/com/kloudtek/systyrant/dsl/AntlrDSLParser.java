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
        SystyrantLangParser parser = createParser(script);
        try {
            return parseRoot(ctx, pkg, parser.script());
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

    private DSLScript parseRoot(STContext ctx, String pkg, SystyrantLangParser.ScriptContext start) throws InvalidScriptException {
        return new DSLScript(ctx, pkg, start);
    }

    public static SystyrantLangParser createParser(String script) {
        SystyrantLangLexer lexer = new SystyrantLangLexer(new ANTLRInputStream(script));
        SystyrantLangParser parser = new SystyrantLangParser(new CommonTokenStream(lexer));
        parser.setErrorHandler(new ErrorHandler());
        return parser;
    }

    public static SysTyrantDSLParser createAltParser(String script) {
        SysTyrantDSLLexer lexer = new SysTyrantDSLLexer(new ANTLRInputStream(script));
        SysTyrantDSLParser parser = new SysTyrantDSLParser(new CommonTokenStream(lexer));
        parser.setErrorHandler(new ErrorHandler());
        return parser;
    }

    public static class ErrorHandler extends DefaultErrorStrategy {
        @Override
        public void recover(Parser recognizer, RecognitionException e) {
            for (ParserRuleContext context = recognizer.getContext(); context != null; context = context.getParent()) {
                context.exception = e;
            }
            throw new InvalidScriptException(e.getOffendingToken());
        }

        @Override
        public Token recoverInline(Parser recognizer) throws RecognitionException {
            for (ParserRuleContext context = recognizer.getContext(); context != null; context = context.getParent()) {
                context.exception = new InputMismatchException(recognizer);
            }
            throw new InvalidScriptException(recognizer.getCurrentToken());
        }

        @Override
        public void sync(Parser recognizer) {
        }
    }
}
