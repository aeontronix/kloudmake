/*
 * Copyright (c) 2024 Aeontronix Inc
 */

package com.aeontronix.aeonbuild.util;

import java.util.HashSet;

public class SetHashMap<X, Y> extends AutoCreateHashMap<X, HashSet<Y>> {
    @Override
    protected HashSet<Y> create() {
        return new HashSet<>();
    }
}
