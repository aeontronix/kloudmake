/*
 * Copyright (c) 2024 Aeontronix Inc
 */

package com.aeontronix.aeonbuild.exception;

public class AlreadyExecutedException extends RuntimeException {
    public AlreadyExecutedException() {
    }

    public AlreadyExecutedException(String message) {
        super(message);
    }

    public AlreadyExecutedException(String message, Throwable cause) {
        super(message, cause);
    }

    public AlreadyExecutedException(Throwable cause) {
        super(cause);
    }

    public AlreadyExecutedException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
