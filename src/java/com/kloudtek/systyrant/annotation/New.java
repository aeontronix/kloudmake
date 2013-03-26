/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.systyrant.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * This annotation can be used to create a new resource using the Systyrant DSL. It can used on the type, or on a field.
 * If used on a field, that field must be either of type {@link com.kloudtek.systyrant.resource.Resource}, or a collection or array of {@link com.kloudtek.systyrant.resource.Resource}.
 *
 * ie: @New("core:file {path = '/test'}")
 *
 */
@Retention(RUNTIME)
@Target({FIELD, TYPE})
public @interface New {
    String value();
}
