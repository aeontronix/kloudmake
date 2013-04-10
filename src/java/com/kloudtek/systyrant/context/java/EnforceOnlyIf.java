/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.systyrant.context.java;

import com.kloudtek.systyrant.Resource;
import com.kloudtek.systyrant.STContext;
import com.kloudtek.systyrant.annotation.OnlyIf;
import com.kloudtek.systyrant.annotation.OnlyIfOperatingSystem;
import com.kloudtek.systyrant.exception.InvalidResourceDefinitionException;
import com.kloudtek.systyrant.exception.STRuntimeException;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Set;

/**
 * Base class for all OnlyIf annotation enforcement.
 */
public abstract class EnforceOnlyIf {
    public abstract boolean execAllowed(STContext context, Resource resource) throws STRuntimeException;

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
