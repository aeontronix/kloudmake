/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.systyrant.dsl;

import com.kloudtek.systyrant.STContext;
import com.kloudtek.systyrant.resource.Resource;

/**
 * Created with IntelliJ IDEA.
 * User: yinkaf
 * Date: 25/02/2013
 * Time: 01:29
 * To change this template use File | Settings | File Templates.
 */
public class DynamicParameter extends Parameter {
    String parameterId;

    public DynamicParameter() {
    }

    public DynamicParameter(String parameterId) {
        this.parameterId = parameterId;
    }

    public String getParameterId() {
        return parameterId;
    }

    public void setParameterId(String param) {
        this.parameterId = param;
    }

    @Override
    public String eval(STContext ctx, Resource resource) {
        return getAttrValue(resource, parameterId);
    }

    private String getAttrValue(Resource resource, String attr) {
        if (resource == null)
            return null;
        if (resource.getAttributes().containsKey(attr)) {
            return resource.get(attr);
        } else {
            return getAttrValue(resource.getParent(), attr);
        }
    }
}

