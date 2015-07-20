/*
 * Copyright (c) 2015. Kelewan Technologies Ltd
 */

package com.kloudtek.kloudmake.dsl;

/**
 * Created with IntelliJ IDEA.
 * User: yannick
 * Date: 10/03/13
 * Time: 20:33
 * To change this template use File | Settings | File Templates.
 */
public enum BinaryOp {
    AND, OR;

    public static BinaryOp valueOf(KloudmakeLangParser.BinaryOpContext op) {
        if (op.a != null) {
            return AND;
        } else if (op.o != null) {
            return OR;
        } else {
            throw new RuntimeException("BUG! Invalid binary operator " + op.getText());
        }
    }

}
