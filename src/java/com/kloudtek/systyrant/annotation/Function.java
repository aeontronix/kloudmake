/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.systyrant.annotation;

import com.kloudtek.systyrant.Stage;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface Function {
    String value() default "";

    Stage[] stage() default Stage.PREPARE;
}
