/*
 * Copyright (c) 2024 Aeontronix Inc
 */

package com.aeontronix.aeonbuild.service.filestore;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.TYPE;

@Retention(RetentionPolicy.RUNTIME)
@Target({FIELD, TYPE})
public @interface DataFileDef {
    String path() default "";

    String url() default "";

    String sha1() default "";

    boolean retrievable() default true;
}
