/*
 * Copyright (c) 2024 Aeontronix Inc
 */

package com.aeontronix.aeonbuild.exception;

import org.slf4j.Logger;

/**
 * Created with IntelliJ IDEA.
 * User: ymenager
 * Date: 21/02/2013
 * Time: 00:15
 * To change this template use File | Settings | File Templates.
 */
public class InvalidServiceException extends KMRuntimeException {
    public InvalidServiceException() {
    }

    public InvalidServiceException(String message) {
        super(message);
    }

    public InvalidServiceException(Logger logger, String message) {
        super(logger, message);
    }

    public InvalidServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidServiceException(Logger logger, String message, Throwable cause) {
        super(logger, message, cause);
    }

    public InvalidServiceException(Throwable cause) {
        super(cause);
    }

    public InvalidServiceException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
