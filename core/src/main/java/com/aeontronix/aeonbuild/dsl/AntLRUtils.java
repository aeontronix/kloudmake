/*
 * Copyright (c) 2024 Aeontronix Inc
 */

package com.aeontronix.aeonbuild.dsl;

import com.aeontronix.aeonbuild.Parameters;

import java.io.StringWriter;
import java.util.Collections;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: yannick
 * Date: 10/03/13
 * Time: 19:17
 * To change this template use File | Settings | File Templates.
 */
public class AntLRUtils {
    public static String escapeStr(String txt) {
        if (txt.isEmpty()) {
            return null;
        }
        StringWriter buf = new StringWriter();
        boolean escape = false;
        for (char c : txt.toCharArray()) {
            if (c == '\\') {
                if (escape) {
                    buf.write(c);
                    escape = false;
                } else {
                    escape = true;
                }
            } else {
                buf.write(c);
                if (escape) {
                    escape = false;
                }
            }
        }
        return buf.toString();
    }

    public static Parameters toParams(List<AeonBuildLangParser.ParameterContext> paramListContext) throws InvalidScriptException {
        Parameters params = new Parameters();
        if (paramListContext != null) {
            for (AeonBuildLangParser.ParameterContext paramContext : paramListContext) {
                Parameter parameter = Parameter.create(paramContext.staticOrDynamicValue());
                AeonBuildLangParser.AnyIdContext idVal = paramContext.anyId();
                if (idVal != null) {
                    params.addNamedParameter(idVal.getText(), parameter);
                } else {
                    params.addParameter(paramContext.start, parameter);
                }
            }
        }
        return params;
    }

    public static <X> List<X> nullToEmpty(List<X> list) {
        if (list == null) {
            return Collections.emptyList();
        } else {
            return list;
        }
    }

    public static String toString(AeonBuildLangParser.StaticValueContext ctx) {
        if (ctx == null) {
            return null;
        } else if (ctx.nb != null) {
            return ctx.nb.getText();
        } else if (ctx.qstr != null) {
            String text = ctx.qstr.getText();
            return escapeStr(text.substring(1, text.length() - 1));
        } else if (ctx.uqstr != null) {
            return ctx.uqstr.getText();
        } else if (ctx.id != null) {
            return ctx.id.getText();
        } else {
            throw new RuntimeException("BUG: staticValue has no data");
        }
    }

    public static String toString(AeonBuildLangParser.StringContext ctx) {
        if (ctx == null) {
            return null;
        } else if (ctx.astr != null) {
            String text = ctx.astr.getText();
            return escapeStr(text.substring(1, text.length() - 1));
        } else {
            return toString(ctx.sval);
        }
    }
}
