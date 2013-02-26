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

//    public static HashMap<String, Parameter> namedToParams(List<SystyrantLangParser.NamedParamContext> namedParamContexts) throws InvalidScriptException {
//        HashMap<String, Parameter> params = new HashMap<>();
//        if (namedParamContexts != null) {
//            for (SystyrantLangParser.NamedParamContext paramContext : namedParamContexts) {
//                SystyrantLangParser.ParamValueContext value = paramContext.paramValue();
//                TerminalNode nbValue = value.NB();
//                TerminalNode stValue = value.STRING();
//                Parameter parameter;
//                if (nbValue != null) {
//                    parameter = new StaticParameter(nbValue.getText());
//                } else if (stValue != null) {
//                    parameter = new StaticParameter(unescapeStr(stValue));
//                } else {
//                    throw new RuntimeException("Bug: no param found in " + paramContext.getText());
//                }
//                TerminalNode idVal = paramContext.ID();
//                params.put(idVal.getText(), parameter);
//            }
//        }
//        return params;
//    }

    public static Parameters toParams(List<SystyrantLangParser.ParameterContext> paramListContext) throws InvalidScriptException {
        Parameters params = new Parameters();
        if (paramListContext != null) {
            for (SystyrantLangParser.ParameterContext paramContext : paramListContext) {
                Parameter parameter;
                SystyrantLangParser.StaticOrDynamicValueContext staticOrDynamicValueContext = paramContext.staticOrDynamicValue();
                SystyrantLangParser.StaticValueContext staticValueContext = staticOrDynamicValueContext.staticValue();
                SystyrantLangParser.InvokeMethodContext invokeMethodContext = staticOrDynamicValueContext.invokeMethod();
                SystyrantLangParser.DynamicValueContext dynamicValueContext = staticOrDynamicValueContext.dynamicValue();
                if (staticValueContext != null) {
                    parameter = new StaticParameter(toString(staticValueContext));
                } else if (invokeMethodContext != null) {
                    throw new RuntimeException("Not implemented");
                } else {
                    throw new RuntimeException("Bug: no param found in " + paramContext.getText());
                }
                SystyrantLangParser.AnyIdContext idVal = paramContext.anyId();
                if (idVal != null) {
                    params.addNamedParameter(idVal.getText(), parameter);
                } else {
                    params.addParameter(paramContext.start, parameter);
                }
            }
        }
        return params;
    }

//    public static String unescapeStr(SystyrantLangParser.ParamValueContext assignCtx) {
//        TerminalNode st = assignCtx.STRING();
//        if (st != null) {
//            return unescapeStr(st);
//        }
//        TerminalNode nb = assignCtx.NB();
//        if (nb != null) {
//            return nb.getText();
//        }
//        return null;
//    }

    public static String unescapeStr(TerminalNode stringNode) {
        return unescapeStr(stringNode.getText());
    }

    public static String unescapeStr(Token token) {
        return unescapeStr(token.getText());
    }

    public static String unescapeStr(String txt) {
        if (txt.length() == 0) {
            return null;
        }
        char f = txt.charAt(0);
        char l = txt.charAt(txt.length() - 1);
        int b = (f == '\"' || f == '\'') ? 1 : 0;
        int e = txt.length() - ((l == '\"' || l == '\'') ? 1 : 0);
        txt = txt.substring(b, e);
        return txt.replace("\\\\", "\\").replace("\\'", "'").replace("\\\"", "\"");
    }

    public static <X> List<X> nullToEmpty(List<X> list) {
        if (list == null) {
            return Collections.emptyList();
        } else {
            return list;
        }
    }

    public static SystyrantLangParser createParser(String script) {
        SystyrantLangLexer lexer = new SystyrantLangLexer(new ANTLRInputStream(script));
        SystyrantLangParser parser = new SystyrantLangParser(new CommonTokenStream(lexer));
        parser.setErrorHandler(new BailErrorStrategy());
        return parser;
    }

    public static String toString(SystyrantLangParser.StaticValueContext staticValueContext) {
        if (staticValueContext == null) {
            return null;
        }
        TerminalNode nb = staticValueContext.NB();
        if (nb != null) {
            return nb.getText();
        }
        TerminalNode str = staticValueContext.STRING();
        if (str != null) {
            return unescapeStr(str.getText());
        }
        TerminalNode ustr = staticValueContext.UQSTRING();
        if (ustr != null) {
            return ustr.getText();
        }
        SystyrantLangParser.AnyIdContext anyIdContext = staticValueContext.anyId();
        if (anyIdContext != null) {
            return anyIdContext.getText();
        }
        throw new RuntimeException("BUG: staticValue has no data");
    }
}
