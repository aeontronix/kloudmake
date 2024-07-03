/*
 * Copyright (c) 2024 Aeontronix Inc
 */

package com.aeontronix.aeonbuild.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * This annotation can be used to create a new resource using the AeonBuild DSL. It can used on the type, or on a field.
 * If used on a field, that field must be of type {@link com.aeontronix.aeonbuild.Resource}.
 * ie: {@code @New("core.file {path = '/test'}") public Resource fileResource }
 */
@Retention(RUNTIME)
@Target({FIELD, TYPE})
public @interface New {
    String value();
}
