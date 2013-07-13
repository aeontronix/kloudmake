/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.systyrant.annotation;

import com.kloudtek.systyrant.Stage;

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
