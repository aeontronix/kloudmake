/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.systyrant.exception;

import org.slf4j.Logger;

/**
 * Created with IntelliJ IDEA.
 * User: ymenager
 * Date: 03/03/2013
 * Time: 22:37
 * To change this template use File | Settings | File Templates.
 */
public class NoProviderFoundException extends STRuntimeException {
    public NoProviderFoundException() {
    }

    public NoProviderFoundException(String message) {
        super(message);
    }

    public NoProviderFoundException(Logger logger, String message) {
        super(logger, message);
    }

    public NoProviderFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public NoProviderFoundException(Logger logger, String message, Throwable cause) {
        super(logger, message, cause);
    }

    public NoProviderFoundException(Throwable cause) {
        super(cause);
    }

    public NoProviderFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
