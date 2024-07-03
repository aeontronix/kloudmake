/*
 * Copyright (c) 2024 Aeontronix Inc
 */

package com.aeontronix.aeonbuild.exception;

public class DepencyNotFoundException extends InvalidDependencyException {
    public DepencyNotFoundException() {
    }

    public DepencyNotFoundException(String message) {
        super(message);
    }

    public DepencyNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public DepencyNotFoundException(Throwable cause) {
        super(cause);
    }

    public DepencyNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
