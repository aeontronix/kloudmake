/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.systyrant;

import com.kloudtek.systyrant.annotation.STResource;
import com.kloudtek.systyrant.exception.InvalidAttributeException;
import com.kloudtek.systyrant.exception.ResourceCreationException;
import com.kloudtek.systyrant.resource.Resource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * Helper class for use by elements.
 */
public class STHelper {
    public static Resource createElement(@NotNull FQName fqname) throws ResourceCreationException {
        return STContext.get().getResourceManager().createResource(fqname, null, null);
    }

    public static Resource createElement(@NotNull FQName fqname, String id, Map<String, Object> attributes) throws ResourceCreationException {
        Resource resource = createElement(fqname);
        try {
            if (id != null) {
                resource.setId(id);
            }
            if (attributes != null) {
                resource.setAttributes(attributes);
            }
        } catch (InvalidAttributeException e) {
            throw new ResourceCreationException(e.getMessage(), e);
        }
        return resource;
    }

    public static Resource createChildElement(FQName fqname) throws ResourceCreationException {
        Resource child = createElement(fqname);
        Resource currentResource = STContext.get().currentResource();
        child.setParent(currentResource);
        return child;
    }

    public static Resource createElement(@Nullable String pkg, @NotNull String name, @Nullable String id) throws ResourceCreationException {
        return createElement(new FQName(pkg, name), id, null);
    }

    public static Resource createElement(@Nullable String pkg, @NotNull String name) throws ResourceCreationException {
        return createElement(new FQName(pkg, name));
    }

    public static Resource createElement(@NotNull FQName fqname, String id) throws ResourceCreationException {
        return createElement(fqname, id, null);
    }

    public static Resource createChildElement(String fqname) throws ResourceCreationException {
        return createChildElement(new FQName(fqname));
    }

    public static Resource createElement(@NotNull Class<?> elementClass) throws ResourceCreationException {
        return createElement(getCheckFQName(elementClass), null, null);
    }

    public static Resource createElement(@NotNull Class<?> elementClass, String id) throws ResourceCreationException {
        return createElement(getCheckFQName(elementClass), id, null);
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

    public static FQName getFQName(Class<?> elementClass) {
        STResource annotation = elementClass.getAnnotation(STResource.class);
        if (annotation != null) {
            return new FQName(annotation.value());
        } else {
            return null;
        }
    }

    private static FQName getCheckFQName(Class<?> elementClass) throws ResourceCreationException {
        FQName fqName = getFQName(elementClass);
        if (fqName == null) {
            throw new ResourceCreationException("Class " + elementClass.getName() + " must be annotated with @Resource");
        }
        return fqName;
    }
}
