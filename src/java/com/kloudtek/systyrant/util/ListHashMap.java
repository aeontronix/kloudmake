/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.systyrant.util;

import java.util.ArrayList;

public class ListHashMap<X, Y> extends AutoCreateHashMap<X, ArrayList<Y>> {
    @Override
    protected ArrayList<Y> create() {
        return new ArrayList<Y>();
    }
}
