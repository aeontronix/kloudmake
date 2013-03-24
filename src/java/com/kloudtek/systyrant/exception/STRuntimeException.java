/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.systyrant.exception;

import org.slf4j.Logger;

import java.lang.reflect.InvocationTargetException;

public class STRuntimeException extends STException {
    public STRuntimeException() {
    }

    public STRuntimeException(String message) {
        super(message);
    }

    public STRuntimeException(Logger logger, String message) {
        super(logger, message);
    }

    public STRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

    public STRuntimeException(Logger logger, String message, Throwable cause) {
        super(logger, message, cause);
    }

    public STRuntimeException(Throwable cause) {
        super(cause);
    }

    public STRuntimeException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public static STRuntimeException getCause(InvocationTargetException e) {
        Throwable cause = e.getCause();
        if( cause != null ) {
            if( cause instanceof STRuntimeException ) {
                return (STRuntimeException)cause;
            } else {
                return new STRuntimeException(cause.getMessage(), cause);
            }
        } else {
            return new STRuntimeException(e.getMessage(),e);
        }
    }
}
