/*
 * Copyright (c) 2024 Aeontronix Inc
 */

package com.aeontronix.aeonbuild.java;

import com.aeontronix.aeonbuild.annotation.OnlyIf;
import com.aeontronix.aeonbuild.annotation.OnlyIfOperatingSystem;
import com.aeontronix.aeonbuild.exception.InvalidResourceDefinitionException;
import com.aeontronix.aeonbuild.exception.KMRuntimeException;
import com.aeontronix.aeonbuild.BuildContextImpl;
import com.aeontronix.aeonbuild.Resource;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Set;

/**
 * Base class for all OnlyIf annotation enforcement.
 */
public abstract class EnforceOnlyIf {
    public abstract boolean execAllowed(BuildContextImpl context, Resource resource) throws KMRuntimeException;

    public static Set<EnforceOnlyIf> find(Class<?> clazz) throws InvalidResourceDefinitionException {
        HashSet<EnforceOnlyIf> list = new HashSet<>();
        OnlyIfOperatingSystem os = clazz.getAnnotation(OnlyIfOperatingSystem.class);
        if (os != null) {
            list.add(new EnforceOnlyIfOS(os.value()));
        }
        for (java.lang.reflect.Method method : clazz.getDeclaredMethods()) {
            OnlyIf custom = getAnnotation(method, OnlyIf.class);
            if (custom != null) {
                list.add(new EnforceOnlyIfCustom(method, clazz));
            }
        }
        return list;
    }

    public static Set<EnforceOnlyIf> find(java.lang.reflect.Method method) throws InvalidResourceDefinitionException {
        HashSet<EnforceOnlyIf> list = new HashSet<>();
        OnlyIfOperatingSystem os = getAnnotation(method, OnlyIfOperatingSystem.class);
        if (os != null) {
            list.add(new EnforceOnlyIfOS(os.value()));
        }
        return list;
    }

    @SuppressWarnings("unchecked")
    private static <X extends Annotation> X getAnnotation(@NotNull Object obj, Class<X> annotation) {
        if (obj instanceof Class) {
            return (X) ((Class) obj).getAnnotation(annotation);
        } else {
            return ((java.lang.reflect.Method) obj).getAnnotation(annotation);
        }
    }
}
