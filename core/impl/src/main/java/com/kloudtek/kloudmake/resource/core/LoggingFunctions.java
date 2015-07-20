/*
 * Copyright (c) 2015. Kelewan Technologies Ltd
 */

package com.kloudtek.kloudmake.resource.core;

import com.kloudtek.kloudmake.annotation.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Various logging functions
 */
public class LoggingFunctions {
    private static final Logger logger = LoggerFactory.getLogger(LoggingFunctions.class);

    /**
     * Log a debug message.
     *
     * @param message Message to log
     */
    @Function
    public void logDebug(String message) {
        logger.debug(message);
    }

    /**
     * Log an informational message.
     *
     * @param message Message to log
     */
    @Function
    public void logInfo(String message) {
        logger.info(message);
    }

    /**
     * Log a warning message
     *
     * @param message Message to log
     */
    @Function
    public void logWarning(String message) {
        logger.warn(message);
    }

    /**
     * Log an error message
     *
     * @param message Message to log
     */
    @Function
    public void logError(String message) {
        logger.error(message);
    }

}
