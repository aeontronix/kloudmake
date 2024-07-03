/*
 * Copyright (c) 2024 Aeontronix Inc
 */

package com.aeontronix.aeonbuild.dsl;

import com.aeontronix.aeonbuild.exception.InvalidQueryException;

/**
 * Created with IntelliJ IDEA.
 * User: yannick
 * Date: 10/03/13
 * Time: 20:33
 * To change this template use File | Settings | File Templates.
 */
public enum LogOp {
    EQ, LIKE, REGEX, ISNULL, EMPTY, CONTAINS, CHILDOF, GT, LT;

    public static LogOp valueOf(AeonBuildLangParser.QueryAttrMatchOpContext op, String query) throws InvalidQueryException {
        if (op.eq != null) {
            return EQ;
        } else if (op.lk != null) {
            return LIKE;
        } else if (op.rgx != null) {
            return REGEX;
        } else if (op.l != null) {
            return LT;
        } else if (op.m != null) {
            return GT;
        } else {
            throw new RuntimeException("BUG! Invalid LogOp: " + op.getText());
        }
    }
}
