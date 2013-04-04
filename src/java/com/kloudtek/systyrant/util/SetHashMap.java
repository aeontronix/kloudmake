/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.systyrant.util;

import java.util.HashSet;

public class SetHashMap<X, Y> extends AutoCreateHashMap<X, HashSet<Y>> {
    @Override
    protected HashSet<Y> create() {
        return new HashSet<>();
    }
}
