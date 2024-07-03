/*
 * Copyright (c) 2024 Aeontronix Inc
 */

package com.aeontronix.aeonbuild.exception;

import org.slf4j.Logger;

public class InvalidStageException extends KMRuntimeException {
    public InvalidStageException() {
    }

    public InvalidStageException(String message) {
        super(message);
    }

    public InvalidStageException(Logger logger, String message) {
        super(logger, message);
    }

    public InvalidStageException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidStageException(Logger logger, String message, Throwable cause) {
        super(logger, message, cause);
    }

    public InvalidStageException(Throwable cause) {
        super(cause);
    }

    public InvalidStageException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
