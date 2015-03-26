/*
 * Copyright (c) 2015. Kelewan Technologies Ltd
 */

package com.kloudtek.kloudmake.exception;

import org.slf4j.Logger;

/**
 * Created with IntelliJ IDEA.
 * User: yannick
 * Date: 15/02/13
 * Time: 19:38
 * To change this template use File | Settings | File Templates.
 */
public class ResourceValidationException extends KMRuntimeException {
    public ResourceValidationException() {
    }

    public ResourceValidationException(String message) {
        super(message);
    }

    public ResourceValidationException(Logger logger, String message) {
        super(logger, message);
    }

    public ResourceValidationException(String message, Throwable cause) {
        super(message, cause);
    }

    public ResourceValidationException(Logger logger, String message, Throwable cause) {
        super(logger, message, cause);
    }

    public ResourceValidationException(Throwable cause) {
        super(cause);
    }

    public ResourceValidationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
