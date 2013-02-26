/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.systyrant.exception;

import org.slf4j.Logger;

public class STRuntimeException extends STException {
    public STRuntimeException() {
    }

    public STRuntimeException(String message) {
        super(message);
    }

    public STRuntimeException(Logger logger, String message) {
        super(logger, message);
    }

    public STRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

    public STRuntimeException(Logger logger, String message, Throwable cause) {
        super(logger, message, cause);
    }

    public STRuntimeException(Throwable cause) {
        super(cause);
    }

    public STRuntimeException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
