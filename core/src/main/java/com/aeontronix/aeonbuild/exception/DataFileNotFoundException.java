/*
 * Copyright (c) 2024 Aeontronix Inc
 */

package com.aeontronix.aeonbuild.exception;

import java.io.IOException;

public class DataFileNotFoundException extends IOException {
    public DataFileNotFoundException() {
    }

    public DataFileNotFoundException(String message) {
        super(message);
    }

    public DataFileNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public DataFileNotFoundException(Throwable cause) {
        super(cause);
    }
}
