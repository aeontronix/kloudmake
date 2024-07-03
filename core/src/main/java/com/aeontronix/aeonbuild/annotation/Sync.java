/*
 * Copyright (c) 2024 Aeontronix Inc
 */

package com.aeontronix.aeonbuild.annotation;

import com.aeontronix.aeonbuild.Stage;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME)
@Target({METHOD})
public @interface Sync {
    String value() default "";

    int order() default 0;

    Stage stage() default Stage.EXECUTE;

    boolean postChildren() default false;
}
