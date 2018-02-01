/*
 * Copyright (c) 2015. Kelewan Technologies Ltd
 */

package com.kloudtek.kloudmake.dsl;

import com.kloudtek.kloudmake.exception.InvalidQueryException;

/**
 * Created with IntelliJ IDEA.
 * User: yannick
 * Date: 10/03/13
 * Time: 20:33
 * To change this template use File | Settings | File Templates.
 */
public enum LogOp {
    EQ, LIKE, REGEX, ISNULL, EMPTY, CONTAINS, CHILDOF, GT, LT;

    public static LogOp valueOf(KloudmakeLangParser.QueryAttrMatchOpContext op, String query) throws InvalidQueryException {
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
