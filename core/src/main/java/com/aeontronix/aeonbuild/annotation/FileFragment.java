/*
 * Copyright (c) 2024 Aeontronix Inc
 */

package com.aeontronix.aeonbuild.annotation;

import com.aeontronix.aeonbuild.resource.core.FileContent;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Resources that implement new types of file fragments should have this annotation (in addition to {@link KMResource})
 */
@Retention(RUNTIME)
@Target({TYPE})
public @interface FileFragment {
    Class<? extends FileContent> fileContentClass();
}
