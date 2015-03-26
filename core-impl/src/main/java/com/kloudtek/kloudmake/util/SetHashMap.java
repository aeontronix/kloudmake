/*
 * Copyright (c) 2015. Kelewan Technologies Ltd
 */

package com.kloudtek.kloudmake.util;

import java.util.HashSet;

public class SetHashMap<X, Y> extends AutoCreateHashMap<X, HashSet<Y>> {
    @Override
    protected HashSet<Y> create() {
        return new HashSet<>();
    }
}
