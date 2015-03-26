/*
 * Copyright (c) 2015. Kelewan Technologies Ltd
 */

package com.kloudtek.kloudmake.exception;

import org.slf4j.Logger;

public class InvalidRefException extends STException {
    public InvalidRefException() {
    }

    public InvalidRefException(String message) {
        super(message);
    }

    public InvalidRefException(Logger logger, String message) {
        super(logger, message);
    }

    public InvalidRefException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidRefException(Logger logger, String message, Throwable cause) {
        super(logger, message, cause);
    }

    public InvalidRefException(Throwable cause) {
        super(cause);
    }

    public InvalidRefException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
