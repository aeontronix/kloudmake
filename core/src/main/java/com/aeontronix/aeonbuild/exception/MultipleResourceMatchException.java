/*
 * Copyright (c) 2024 Aeontronix Inc
 */

package com.aeontronix.aeonbuild.exception;

import org.slf4j.Logger;

public class MultipleResourceMatchException extends ResourceCreationException {
    public MultipleResourceMatchException() {
    }

    public MultipleResourceMatchException(String message) {
        super(message);
    }

    public MultipleResourceMatchException(Logger logger, String message) {
        super(logger, message);
    }

    public MultipleResourceMatchException(String message, Throwable cause) {
        super(message, cause);
    }

    public MultipleResourceMatchException(Logger logger, String message, Throwable cause) {
        super(logger, message, cause);
    }

    public MultipleResourceMatchException(Throwable cause) {
        super(cause);
    }

    public MultipleResourceMatchException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
