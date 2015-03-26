/*
 * Copyright (c) 2015. Kelewan Technologies Ltd
 */

package com.kloudtek.kloudmake.exception;

/**
 * Thrown when invalid dependencies are found (not existent references or circular dependencies)
 */
public class InvalidDependencyException extends KMRuntimeException {
    public InvalidDependencyException() {
    }

    public InvalidDependencyException(String message) {
        super(message);
    }

    public InvalidDependencyException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidDependencyException(Throwable cause) {
        super(cause);
    }

    public InvalidDependencyException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
