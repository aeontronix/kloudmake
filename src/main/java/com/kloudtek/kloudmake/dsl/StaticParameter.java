/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.kloudmake.dsl;

import com.kloudtek.kloudmake.Resource;
import com.kloudtek.kloudmake.STContext;

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
    public String eval(STContext ctx, Resource resource) {
        return value;
    }

    @Override
    public String toString() {
        return "staticparam{" + value + "}";

    }
}