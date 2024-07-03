/*
 * Copyright (c) 2024 Aeontronix Inc
 */

package com.aeontronix.aeonbuild.util;

import java.util.HashMap;

public class MapHashMap<X, Y, Z> extends AutoCreateHashMap<X, HashMap<Y, Z>> {
    @Override
    protected HashMap<Y, Z> create() {
        return new HashMap<>();
    }
}
