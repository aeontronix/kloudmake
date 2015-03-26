/*
 * Copyright (c) 2015. Kelewan Technologies Ltd
 */

package com.kloudtek.kloudmake.annotation;

import com.kloudtek.kloudmake.ServiceManager;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * <p>This annotation can be used to indicate classes to be registered as services {@link ServiceManager#registerService(String, Object)},
 * or to inject a service into a java resource implementation (a class annotated with {@link STResource})</p>
 * <ul>
 * <li>
 * When used to annotate a class type, it will create an object of the annotated class type (or of the class type
 * specified in {@link #def()} if specified), and register it using {@link ServiceManager#registerService(String, Object)}.
 * The name will be {@link #value()} if specified, otherwise it will use the annotated class name converted to lower case.
 * </li>
 * <li>
 * When used to annotate a field in a java resource implementation, it will find the relevant service and inject it.
 * {@link ServiceManager#getService(String)} will be used if {@link #value()} is set, or otherwise it will use
 * {@link ServiceManager#getService(Class)} with the field's name as parameter.
 * </li>
 * </ul>
 */
@Retention(RUNTIME)
@Target({FIELD, PARAMETER, TYPE})
public @interface Service {
    /**
     * Service name (if not set the class name lower cased will be used)
     *
     * @return Service name
     */
    String value() default "";

    /**
     * Service implementation class (if left as default the class of annotated type will be used). Only valid on
     * annotated class types, otherwise has no effect.
     *
     * @return Default service implementation class (if class {@link ServiceManager} then it means the class of annotated type should be used)
     */
    Class def() default ServiceManager.class;
}
