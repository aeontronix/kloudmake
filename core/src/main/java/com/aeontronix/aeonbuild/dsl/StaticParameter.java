/*
 * Copyright (c) 2024 Aeontronix Inc
 */

package com.aeontronix.aeonbuild.dsl;

import com.aeontronix.aeonbuild.BuildContextImpl;
import com.aeontronix.aeonbuild.Resource;

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
    public String eval(BuildContextImpl ctx, Resource resource) {
        return value;
    }

    @Override
    public String toString() {
        return "staticparam{" + value + "}";

    }
}
