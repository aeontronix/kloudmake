/*
 * Copyright (c) 2015. Kelewan Technologies Ltd
 */

package com.kloudtek.kloudmake.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * <p>This annotation is used to inject objects into a field of a java class annotated with {@link KMResource}. The injected
 * object depends on the type of the annotated field:</p>
 * <ul>
 * <li>{@link com.kloudtek.kloudmake.KMContextImpl}: The owning context will be injected</li>
 * <li>{@link com.kloudtek.kloudmake.context.Resource}: The resource corresponding to the java class will be injected</li>
 * <li>In any other case, it will attempt to inject a service of the specified class type using
 * {@link com.kloudtek.kloudmake.ServiceManager#getService(Class)}</li>
 * </ul>
 * TODO FIX JAVADOCS NO LONGER CORRECT
 */
@Retention(RUNTIME)
@Target({FIELD})
public @interface Inject {
}
