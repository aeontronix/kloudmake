/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.systyrant.resource.core;

import com.kloudtek.systyrant.annotation.Function;
import org.joda.time.format.ISODateTimeFormat;

/**
 * Various utility functions
 */
public class UtilFunctions {
    /**
     * Return the current date in ISO8601 format (yyyy-MM-dd)
     *
     * @return Date in ISO8601 format.
     */
    @Function
    public String currentDate() {
        return ISODateTimeFormat.date().print(System.currentTimeMillis());
    }

    /**
     * Return the current date and time in ISO8601 format (yyyy-MM-dd'T'HH:mm:ssZZ)
     *
     * @return Date and time in ISO8601 format.
     */
    @Function
    public String currentDateTime() {
        return ISODateTimeFormat.dateTimeNoMillis().print(System.currentTimeMillis());
    }
}
