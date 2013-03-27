/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.systyrant.exception;

import com.kloudtek.systyrant.resource.Resource;
import org.slf4j.Logger;

/**
 * Thrown when multiple unique resources are found in their scope
 */
public class MultipleUniqueResourcesFoundException extends STRuntimeException {
    public MultipleUniqueResourcesFoundException() {
    }

    public MultipleUniqueResourcesFoundException(Resource resource) {
        super("Multiple instances of unique resource " + resource.getType()+" found");
    }

    public MultipleUniqueResourcesFoundException(String message) {
        super(message);
    }

    public MultipleUniqueResourcesFoundException(Logger logger, String message) {
        super(logger, message);
    }

    public MultipleUniqueResourcesFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public MultipleUniqueResourcesFoundException(Logger logger, String message, Throwable cause) {
        super(logger, message, cause);
    }

    public MultipleUniqueResourcesFoundException(Throwable cause) {
        super(cause);
    }

    public MultipleUniqueResourcesFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
