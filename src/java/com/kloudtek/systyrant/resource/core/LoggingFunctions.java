/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.systyrant.resource.core;

import com.kloudtek.systyrant.annotation.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Various logging functions
 */
public class LoggingFunctions {
    private static final Logger logger = LoggerFactory.getLogger(LoggingFunctions.class);

    /**
     * Log a debug message.
     */
    @Function
    public void logDebug(String message) {
        logger.debug(message);
    }

    /**
     * Log an informational message.
     */
    @Function
    public void logInfo(String message) {
        logger.info(message);
    }

    /**
     * Log a warning message
     */
    @Function
    public void logWarning(String message) {
        logger.warn(message);
    }

    /**
     * Log an error message
     */
    @Function
    public void logError(String message) {
        logger.error(message);
    }

}
