/*
 * Copyright (c) 2024 Aeontronix Inc
 */

package com.aeontronix.aeonbuild.exception;

import org.slf4j.Logger;

public class InvalidAttributeException extends KMRuntimeException {
    public InvalidAttributeException() {
    }

    public InvalidAttributeException(String message) {
        super(message);
    }

    public InvalidAttributeException(Logger logger, String message) {
        super(logger, message);
    }

    public InvalidAttributeException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidAttributeException(Logger logger, String message, Throwable cause) {
        super(logger, message, cause);
    }

    public InvalidAttributeException(Throwable cause) {
        super(cause);
    }

    public InvalidAttributeException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
