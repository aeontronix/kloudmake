/*
 * Copyright (c) 2015. Kelewan Technologies Ltd
 */

package com.kloudtek.kloudmake.util;

import com.kloudtek.kloudmake.exception.InvalidAttributeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Pattern;

public class ValidateUtils {
    private static final Logger logger = LoggerFactory.getLogger(ValidateUtils.class);
    private static Pattern IDREGEX = Pattern.compile("^[a-z0-9\\-_]*$");
    private static Pattern PKGREGEX = Pattern.compile("^[a-z0-9\\-_\\.]*$");

    public static boolean isValidId(String id) {
        return IDREGEX.matcher(id).matches();
    }

    public static void validateId(String fqn, String attr, String value) throws InvalidAttributeException {
        if (value != null && !isValidId(value)) {
            logger.error("{}'s attribute {} isn't a valid value: {}", fqn, attr, value);
            throw new InvalidAttributeException();
        }
    }

    public static boolean isValidPkg(String pkg) {
        return PKGREGEX.matcher(pkg).matches();
    }
}
