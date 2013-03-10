/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.systyrant.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * <p>This annotation is used to inject other resources from the context into a Resource's field.</p>
 * <p/>
 * <p>The annotated field must be in a {@link STResource} annotated class.</p>
 * <p>The annotated field's class type must be either of:</p>
 * <ul>
 * <li>a {@link STResource} object (in which case it will expect only one valid resource to be found, and will fail if that is not the case)</li>
 * <li>an array of {@link STResource} objects</li>
 * <li>a collection of {@link STResource} objects</li>
 * </ul>
 */
@Retention(RUNTIME)
@Target({FIELD})
public @interface Resources {
    /**
     * If set to true, only elements that are childrens of the annotated resource will be returned.
     *
     * @return childrens flag (default is false).
     */
    boolean childrens() default false;
}
