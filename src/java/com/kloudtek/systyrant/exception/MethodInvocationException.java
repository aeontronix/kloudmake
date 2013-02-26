/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.systyrant.exception;

import org.slf4j.Logger;

/**
 * Created with IntelliJ IDEA.
 * User: ymenager
 * Date: 22/02/2013
 * Time: 02:05
 * To change this template use File | Settings | File Templates.
 */
public class MethodInvocationException extends STRuntimeException {
    public MethodInvocationException() {
    }

    public MethodInvocationException(String message) {
        super(message);
    }

    public MethodInvocationException(Logger logger, String message) {
        super(logger, message);
    }

    public MethodInvocationException(String message, Throwable cause) {
        super(message, cause);
    }

    public MethodInvocationException(Logger logger, String message, Throwable cause) {
        super(logger, message, cause);
    }

    public MethodInvocationException(Throwable cause) {
        super(cause);
    }

    public MethodInvocationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
