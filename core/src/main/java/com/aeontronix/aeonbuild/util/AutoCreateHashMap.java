/*
 * Copyright (c) 2024 Aeontronix Inc
 */

package com.aeontronix.aeonbuild.util;

import java.util.HashMap;

public abstract class AutoCreateHashMap<X, Y> extends HashMap<X, Y> {
    @Override
    @SuppressWarnings("unchecked")
    public synchronized Y get(Object key) {
        Y val = super.get(key);
        if (val == null) {
            val = create();
            put((X) key, val);
        }
        return val;
    }

    protected abstract Y create();
}
