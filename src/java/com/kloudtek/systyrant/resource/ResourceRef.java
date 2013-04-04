/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.systyrant.resource;

import com.kloudtek.systyrant.exception.InvalidRefException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represent a reference to one of more elements, in a text format.
 */
public class ResourceRef {
    private static final Logger logger = LoggerFactory.getLogger(ResourceRef.class);
    private String ref;

    public ResourceRef() {
    }

    public ResourceRef(String ref) throws InvalidRefException {
        setRef(ref);
    }

    public String getRef() {
        return ref;
    }

    public void setRef(String ref) throws InvalidRefException {
        this.ref = ref;
    }

    @Override
    public String toString() {
        return ref;
    }
}
