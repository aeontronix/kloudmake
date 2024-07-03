/*
 * Copyright (c) 2024 Aeontronix Inc
 */

package com.aeontronix.aeonbuild.dsl;

import com.aeontronix.aeonbuild.BuildContextImpl;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.ParseCancellationException;

public class AntlrDSLParser implements DSLParser {
    @Override
    public DSLScript parse(BuildContextImpl ctx, String script) throws InvalidScriptException {
        return parse(ctx, null, script);
    }

    @Override
    public DSLScript parse(BuildContextImpl ctx, String pkg, String script) throws InvalidScriptException {
        AeonBuildLangParser parser = createParser(script);
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
        return parse(BuildContextImpl.get(), script);
    }

    @Override
    public DSLScript parse(String pkg, String script) throws InvalidScriptException {
        return parse(BuildContextImpl.get(), script);
    }

    private DSLScript parseRoot(BuildContextImpl ctx, String pkg, AeonBuildLangParser.ScriptContext start) throws InvalidScriptException {
        return new DSLScript(ctx, pkg, start);
    }

    public static AeonBuildLangParser createParser(String script) {
        AeonBuildLangLexer lexer = new AeonBuildLangLexer(new ANTLRInputStream(script));
        AeonBuildLangParser parser = new AeonBuildLangParser(new CommonTokenStream(lexer));
        parser.setErrorHandler(new ErrorHandler());
        return parser;
    }

    public static AeonBuildDSLParser createAltParser(String script) {
        AeonBuildDSLLexer lexer = new AeonBuildDSLLexer(new ANTLRInputStream(script));
        AeonBuildDSLParser parser = new AeonBuildDSLParser(new CommonTokenStream(lexer));
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
