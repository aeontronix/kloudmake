/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.systyrant;

import com.kloudtek.systyrant.exception.InvalidAttributeException;
import com.kloudtek.systyrant.exception.ResourceCreationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * Helper class for use by elements.
 */
public class STHelper {
    public static Resource createElement(@NotNull FQName fqname) throws ResourceCreationException {
        return createElement(fqname, null, (Resource) null);
    }

    public static Resource createElement(@NotNull FQName fqname, String id, Resource parent) throws ResourceCreationException {
        return STContext.get().getResourceManager().createResource(fqname, null, null, null);
    }

    public static Resource createElement(@NotNull FQName fqname, String id, Map<String, Object> attributes) throws ResourceCreationException {
        Resource resource = createElement(fqname, id, (Resource) null);
        try {
            if (attributes != null) {
                resource.setAttributes(attributes);
            }
        } catch (InvalidAttributeException e) {
            throw new ResourceCreationException(e.getMessage(), e);
        }
        return resource;
    }

    public static Resource createChildElement(FQName fqname) throws ResourceCreationException {
        return createElement(fqname, null, STContext.get().currentResource());
    }

    public static Resource createChildElement(FQName fqname, String id) throws ResourceCreationException {
        return createElement(fqname, id, STContext.get().currentResource());
    }

    public static Resource createElement(@Nullable String pkg, @NotNull String name, @Nullable String id) throws ResourceCreationException {
        return createElement(new FQName(pkg, name), id, (Resource) null);
    }

    public static Resource createElement(@Nullable String pkg, @NotNull String name) throws ResourceCreationException {
        return createElement(new FQName(pkg, name));
    }

    public static Resource createElement(@NotNull FQName fqname, String id) throws ResourceCreationException {
        return createElement(fqname, id, (Resource) null);
    }

    public static Resource createChildElement(String fqname) throws ResourceCreationException {
        return createChildElement(new FQName(fqname));
    }

    public static Resource createElement(@NotNull Class<?> elementClass) throws ResourceCreationException {
        return createElement(getCheckFQName(elementClass), null, (Resource) null);
    }

    public static Resource createElement(@NotNull Class<?> elementClass, String id) throws ResourceCreationException {
        return createElement(getCheckFQName(elementClass), id, (Resource) null);
    }

    public static Resource createElement(Class<?> elementClass, Map<String, Object> attributes) throws ResourceCreationException {
        return createElement(getCheckFQName(elementClass), null, attributes);
    }


    public static Resource createElement(Class<?> elementClass, String id, Map<String, Object> attributes) throws ResourceCreationException {
        return createElement(getCheckFQName(elementClass), id, attributes);
    }

    public static Resource createElement(@NotNull String name) throws ResourceCreationException {
        return createElement(new FQName(name));
    }

    public static Resource createChildElement(Class<?> elementClass) throws ResourceCreationException {
        return createChildElement(getCheckFQName(elementClass));
    }

    private static FQName getCheckFQName(Class<?> elementClass) throws ResourceCreationException {
        FQName fqName = new FQName(elementClass);
        if (fqName == null) {
            throw new ResourceCreationException("Class " + elementClass.getName() + " must be annotated with @Resource");
        }
        return fqName;
    }
}
