/*
 * Copyright (c) 2024 Aeontronix Inc
 */

package com.aeontronix.aeonbuild.dsl;

/**
 * Created with IntelliJ IDEA.
 * User: yannick
 * Date: 10/03/13
 * Time: 20:33
 * To change this template use File | Settings | File Templates.
 */
public enum BinaryOp {
    AND, OR;

    public static BinaryOp valueOf(AeonBuildLangParser.BinaryOpContext op) {
        if (op.a != null) {
            return AND;
        } else if (op.o != null) {
            return OR;
        } else {
            throw new RuntimeException("BUG! Invalid binary operator " + op.getText());
        }
    }

}
