/*
 * Copyright (c) 2024 Aeontronix Inc
 */

package com.aeontronix.aeonbuild.exception;

import org.slf4j.Logger;

/**
 * Base class for all kloudmake exceptions.
 */
public class KMException extends Exception {
    private boolean logged;

    public KMException() {
    }

    public KMException(String message) {
        super(message);
    }

    public KMException(Logger logger, String message) {
        super(message);
        logger.error(message);
        logged = true;
    }

    public KMException(String message, Throwable cause) {
        super(message, cause);
    }

    public KMException(Logger logger, String message, Throwable cause) {
        super(message, cause);
        logger.error(message, cause);
        logged = true;
    }

    public KMException(Throwable cause) {
        super(cause);
    }

    public KMException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public boolean isLogged() {
        return logged;
    }

    public void setLogged(boolean logged) {
        this.logged = logged;
    }
}
