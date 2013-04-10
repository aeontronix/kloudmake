/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.systyrant.exception;

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
