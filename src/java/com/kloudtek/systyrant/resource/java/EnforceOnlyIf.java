/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.systyrant.resource.java;

import com.kloudtek.systyrant.STContext;
import com.kloudtek.systyrant.annotation.Method;
import com.kloudtek.systyrant.annotation.OnlyIfOperatingSystem;
import com.kloudtek.systyrant.resource.Resource;
import org.jetbrains.annotations.NotNull;
import sun.invoke.anon.AnonymousClassLoader;

import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
* Created with IntelliJ IDEA.
* User: yannick
* Date: 24/03/13
* Time: 17:12
* To change this template use File | Settings | File Templates.
*/
public abstract class EnforceOnlyIf {
    public abstract boolean execAllowed(STContext context, Resource resource);

    public static Set<EnforceOnlyIf> find( @NotNull Object methodOrClass ) {
        HashSet<EnforceOnlyIf> list = new HashSet<>();
        OnlyIfOperatingSystem os = getAnnotation(methodOrClass, OnlyIfOperatingSystem.class);
        if( os != null ) {
            list.add(new EnforceOnlyIfOS(os.value()));
        }
        return list;
    }

    @SuppressWarnings("unchecked")
    private static <X extends Annotation> X getAnnotation(@NotNull Object obj, Class<X> annotation) {
        if( obj instanceof Class ) {
            return (X) ((Class) obj).getAnnotation(annotation);
        } else {
            return ((java.lang.reflect.Method) obj).getAnnotation(annotation);
        }
    }
}
