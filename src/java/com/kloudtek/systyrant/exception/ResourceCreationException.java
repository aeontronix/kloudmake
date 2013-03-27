/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.systyrant.exception;

import org.slf4j.Logger;

/** Thrown when a resource creation fails. */
public class ResourceCreationException extends STRuntimeException {
    public ResourceCreationException() {
    }

    public ResourceCreationException(String message) {
        super(message);
    }

    public ResourceCreationException(Logger logger, String message) {
        super(logger, message);
    }

    public ResourceCreationException(String message, Throwable cause) {
        super(message, cause);
    }

    public ResourceCreationException(Logger logger, String message, Throwable cause) {
        super(logger, message, cause);
    }

    public ResourceCreationException(Throwable cause) {
        super(cause);
    }

    public ResourceCreationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
