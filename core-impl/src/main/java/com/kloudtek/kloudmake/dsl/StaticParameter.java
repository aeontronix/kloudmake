/*
 * Copyright (c) 2015. Kelewan Technologies Ltd
 */

package com.kloudtek.kloudmake.dsl;

import com.kloudtek.kloudmake.KMContextImpl;
import com.kloudtek.kloudmake.Resource;

public class StaticParameter extends Parameter {
    private String value;

    public StaticParameter() {
    }

    public StaticParameter(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String getRawValue() {
        return value;
    }

    @Override
    public String eval(KMContextImpl ctx, Resource resource) {
        return value;
    }

    @Override
    public String toString() {
        return "staticparam{" + value + "}";

    }
}
