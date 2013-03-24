/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.systyrant.annotation;

import com.kloudtek.systyrant.host.OperatingSystem;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME)
@Target({METHOD})
public @interface OnlyIfHostMetaData {
    String key();
    String value();
    boolean regex() default false;
}
