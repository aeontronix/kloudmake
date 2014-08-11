/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.kloudmake.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * This annotation can be used to create a new resource using the Kloudmake DSL. It can used on the type, or on a field.
 * If used on a field, that field must be of type {@link com.kloudtek.kloudmake.context.Resource}.
 * ie: <code>
 *
 * @New("core.file {path = '/test'}")
 * public Resource fileResource
 * </code>
 */
@Retention(RUNTIME)
@Target({FIELD, TYPE})
public @interface New {
    String value();
}
