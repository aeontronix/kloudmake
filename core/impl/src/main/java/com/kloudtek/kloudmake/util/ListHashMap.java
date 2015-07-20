/*
 * Copyright (c) 2015. Kelewan Technologies Ltd
 */

package com.kloudtek.kloudmake.util;

import java.util.ArrayList;

public class ListHashMap<X, Y> extends AutoCreateHashMap<X, ArrayList<Y>> {
    @Override
    protected ArrayList<Y> create() {
        return new ArrayList<Y>();
    }
}
