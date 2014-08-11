/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.kloudmake.dsl;

import com.kloudtek.kloudmake.KMContextImpl;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.ParseCancellationException;

public class AntlrDSLParser implements DSLParser {
    @Override
    public DSLScript parse(KMContextImpl ctx, String script) throws InvalidScriptException {
        return parse(ctx, null, script);
    }

    @Override
    public DSLScript parse(KMContextImpl ctx, String pkg, String script) throws InvalidScriptException {
        KloudmakeLangParser parser = createParser(script);
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
        return parse(KMContextImpl.get(), script);
    }

    @Override
    public DSLScript parse(String pkg, String script) throws InvalidScriptException {
        return parse(KMContextImpl.get(), script);
    }

    private DSLScript parseRoot(KMContextImpl ctx, String pkg, KloudmakeLangParser.ScriptContext start) throws InvalidScriptException {
        return new DSLScript(ctx, pkg, start);
    }

    public static KloudmakeLangParser createParser(String script) {
        KloudmakeLangLexer lexer = new KloudmakeLangLexer(new ANTLRInputStream(script));
        KloudmakeLangParser parser = new KloudmakeLangParser(new CommonTokenStream(lexer));
        parser.setErrorHandler(new ErrorHandler());
        return parser;
    }

    public static KloudmakeDSLParser createAltParser(String script) {
        KloudmakeDSLLexer lexer = new KloudmakeDSLLexer(new ANTLRInputStream(script));
        KloudmakeDSLParser parser = new KloudmakeDSLParser(new CommonTokenStream(lexer));
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
