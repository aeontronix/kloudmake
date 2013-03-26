/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.systyrant.resource.query;

import com.kloudtek.systyrant.STContext;
import com.kloudtek.systyrant.dsl.SystyrantLangParser;
import com.kloudtek.systyrant.resource.Resource;

/**
 * Expression used to match by id.
 */
public class IdExpression extends Expression {
    private String id;
    private Resource parent;

    public IdExpression(SystyrantLangParser.QueryIdMatchContext idCtx, Resource baseResource) {
        parent = baseResource;
        id = idCtx.getText();
    }

    @Override
    public boolean matches(STContext context, Resource resource) {
        Resource rp = resource;
        while( rp != null ) {
            if( rp.getParent() == parent && id.equalsIgnoreCase(resource.getId()) ) {
                return true;
            }
            rp = rp.getParent();
        }
        return false;
    }
}
