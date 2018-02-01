/*
 * Copyright (c) 2015. Kelewan Technologies Ltd
 */

package com.kloudtek.kloudmake.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * <p>This annotation is used to inject other resources (retrieving using the query specified in {@link #value()})
 * from the context into a Resource's field.</p>
 * <p>The annotated field must be in a {@link KMResource} annotated class.</p>
 * <p>The annotated field's class type must be either of:</p>
 * <ul>
 * <li>a {@link KMResource} object (in which case it will expect only one valid resource to be found, and will fail if that is not the case)</li>
 * <li>an array of {@link KMResource} objects</li>
 * <li>a {@link java.util.Collection} of {@link KMResource} objects</li>
 * <li>a {@link java.util.List} of {@link KMResource} objects</li>
 * <li>a {@link java.util.Set} of {@link KMResource} objects</li>
 * </ul>
 */
@Retention(RUNTIME)
@Target({FIELD})
public @interface Resources {
    /**
     * Query used to retrieve resources to inject.
     *
     * @return Query string.
     * @see com.kloudtek.kloudmake.dsl.query.ResourceQuery
     */
    String value();
}
