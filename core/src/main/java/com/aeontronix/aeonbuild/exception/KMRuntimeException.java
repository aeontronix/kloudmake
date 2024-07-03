/*
 * Copyright (c) 2024 Aeontronix Inc
 */

package com.aeontronix.aeonbuild.exception;

import org.slf4j.Logger;

import java.lang.reflect.InvocationTargetException;

public class KMRuntimeException extends KMException {
    public KMRuntimeException() {
    }

    public KMRuntimeException(String message) {
        super(message);
    }

    public KMRuntimeException(Logger logger, String message) {
        super(logger, message);
    }

    public KMRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

    public KMRuntimeException(Logger logger, String message, Throwable cause) {
        super(logger, message, cause);
    }

    public KMRuntimeException(Throwable cause) {
        super(cause);
    }

    public KMRuntimeException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public static KMRuntimeException getCause(InvocationTargetException e) {
        Throwable cause = e.getCause();
        if (cause != null) {
            if (cause instanceof KMRuntimeException) {
                return (KMRuntimeException) cause;
            } else {
                return new KMRuntimeException(cause.getMessage(), cause);
            }
        } else {
            return new KMRuntimeException(e.getMessage(), e);
        }
    }
}
