/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.systyrant.dsl;

import com.kloudtek.systyrant.exception.InvalidQueryException;

/**
 * Created with IntelliJ IDEA.
 * User: yannick
 * Date: 10/03/13
 * Time: 20:33
 * To change this template use File | Settings | File Templates.
 */
public enum LogOp {
    EQ, LIKE, REGEX, ISNULL, EMPTY, CONTAINS, CHILDOF;

    public static LogOp valueOf(SystyrantLangParser.QueryAttrMatchOpContext op, String query) throws InvalidQueryException {
        if (op.eq != null) {
            return EQ;
        } else if (op.lk != null) {
            return LIKE;
        } else if (op.rgx != null) {
            return REGEX;
        } else {
            throw new RuntimeException("BUG! Invalid LogOp: " + op.getText());
        }
    }
}
