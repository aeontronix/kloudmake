/*
 * Copyright (c) 2015. Kelewan Technologies Ltd
 */

package com.kloudtek.kloudmake.exception;

import org.slf4j.Logger;

public class InvalidResourceDefinitionException extends STException {
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
