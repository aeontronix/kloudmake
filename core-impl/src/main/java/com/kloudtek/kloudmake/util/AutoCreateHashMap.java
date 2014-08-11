/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.kloudmake.util;

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
