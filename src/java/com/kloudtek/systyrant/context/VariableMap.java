/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.systyrant.context;

import com.kloudtek.systyrant.Resource;

import java.util.HashMap;

public class VariableMap extends HashMap<String, Object> {
    private Resource resource;

    public VariableMap(Resource resource) {
        this.resource = resource;
    }

    @Override
    public Object get(Object key) {
        String name = key.toString();
        Object value = super.get(name);
        if (value == null) {
            Resource r = resource;
            while (r != null && value == null) {
                value = r.get(name);
                if (value == null) {
                    value = r.getVar(name.toString(), true);
                }
                r = r.getParent();
            }
        }
        return value;
    }
}
