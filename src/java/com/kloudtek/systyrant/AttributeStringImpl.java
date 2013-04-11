/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.systyrant;

public class AttributeStringImpl implements Attribute {
    private String value;

    public AttributeStringImpl(String value) {
        this.value = value;
    }

    @Override
    public <X> X convertTo(Class<X> clazz) {
        return null;
    }
}
