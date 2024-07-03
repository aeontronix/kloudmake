/*
 * Copyright (c) 2024 Aeontronix Inc
 */

package com.aeontronix.aeonbuild.annotation;

import com.aeontronix.aeonbuild.BuildContextImpl;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * <p>This annotation is used to inject objects into a field of a java class annotated with {@link KMResource}. The injected
 * object depends on the type of the annotated field:</p>
 * <ul>
 * <li>{@link BuildContextImpl}: The owning context will be injected</li>
 * <li>{@link Resources}: The resource corresponding to the java class will be injected</li>
 * <li>In any other case, it will attempt to inject a service of the specified class type using
 * {@link com.aeontronix.aeonbuild.ServiceManager#getService(Class)}</li>
 * </ul>
 */
@Retention(RUNTIME)
@Target({FIELD})
public @interface Inject {
}
