/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.systyrant.resource;

import com.kloudtek.systyrant.FQName;
import com.kloudtek.systyrant.STContext;
import com.kloudtek.systyrant.exception.InvalidAttributeException;
import com.kloudtek.systyrant.exception.InvalidRefException;
import com.kloudtek.systyrant.exception.STException;
import org.apache.commons.jexl2.Expression;
import org.apache.commons.jexl2.JexlEngine;
import org.apache.commons.jexl2.MapContext;
import org.apache.commons.lang3.text.translate.LookupTranslator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

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
