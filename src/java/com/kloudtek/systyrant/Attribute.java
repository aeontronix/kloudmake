/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.systyrant;

public interface Attribute {
    public <X> X convertTo(Class<X> clazz);
}
