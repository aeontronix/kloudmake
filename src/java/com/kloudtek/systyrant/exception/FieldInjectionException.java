/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.systyrant.exception;

import java.lang.reflect.Field;

/**
 * Created with IntelliJ IDEA.
 * User: yannick
 * Date: 10/03/13
 * Time: 14:10
 * To change this template use File | Settings | File Templates.
 */
public class FieldInjectionException extends STRuntimeException {
    Field field;

    public FieldInjectionException(Field field) {
        super(genMsg(field));
        this.field = field;
    }

    public FieldInjectionException(Field field, Throwable cause) {
        super(genMsg(field, cause), cause);
        this.field = field;
    }

    public FieldInjectionException(Field field, String message) {
        super(genMsg(field) + ": " + message);
        this.field = field;
    }

    public FieldInjectionException(Field field, String message, Throwable cause) {
        super(genMsg(field) + ": " + message, cause);
        this.field = field;
    }

    private static String genMsg(Field field) {
        return "Unable inject " + field.getDeclaringClass().getName() + "#" + field.getName();
    }

    private static String genMsg(Field field, Throwable cause) {
        StringBuilder tmp = new StringBuilder(genMsg(field)).append(": ");
        if (cause instanceof IllegalAccessException) {
            tmp.append("Field access if not not allowed");
        } else {
            tmp.append(cause.getMessage());
        }
        return tmp.toString();
    }
}
