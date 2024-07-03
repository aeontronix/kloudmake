/*
 * Copyright (c) 2024 Aeontronix Inc
 */

package com.aeontronix.aeonbuild.exception;

import org.slf4j.Logger;

public class InvalidResourceDefinitionException extends KMException {
    public InvalidResourceDefinitionException() {
    }

    public InvalidResourceDefinitionException(String message) {
        super(message);
    }

    public InvalidResourceDefinitionException(Logger logger, String message) {
        super(logger, message);
    }

    public InvalidResourceDefinitionException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidResourceDefinitionException(Logger logger, String message, Throwable cause) {
        super(logger, message, cause);
    }

    public InvalidResourceDefinitionException(Throwable cause) {
        super(cause);
    }

    public InvalidResourceDefinitionException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
