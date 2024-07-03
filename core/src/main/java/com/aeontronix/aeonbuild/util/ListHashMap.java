/*
 * Copyright (c) 2024 Aeontronix Inc
 */

package com.aeontronix.aeonbuild.util;

import java.util.ArrayList;

public class ListHashMap<X, Y> extends AutoCreateHashMap<X, ArrayList<Y>> {
    @Override
    protected ArrayList<Y> create() {
        return new ArrayList<Y>();
    }
}
