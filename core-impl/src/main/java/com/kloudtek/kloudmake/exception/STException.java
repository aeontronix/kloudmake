/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.kloudmake.exception;

import org.slf4j.Logger;

/**
 * Base class for all kloudmake exceptions.
 */
public class STException extends Exception {
    private boolean logged;

    public STException() {
    }

    public STException(String message) {
        super(message);
    }

    public STException(Logger logger, String message) {
        super(message);
        logger.error(message);
        logged = true;
    }

    public STException(String message, Throwable cause) {
        super(message, cause);
    }

    public STException(Logger logger, String message, Throwable cause) {
        super(message, cause);
        logger.error(message, cause);
        logged = true;
    }

    public STException(Throwable cause) {
        super(cause);
    }

    public STException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public boolean isLogged() {
        return logged;
    }

    public void setLogged(boolean logged) {
        this.logged = logged;
    }
}
