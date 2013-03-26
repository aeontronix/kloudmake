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
public class UidExpression extends Expression {
    private String uid;

    public UidExpression(SystyrantLangParser.QueryUidMatchContext idCtx, Resource baseResource) {
        if( baseResource == null ) {
            uid = idCtx.getText();
        } else {
            uid = baseResource.getUid()+"."+idCtx.getText();
        }
    }

    @Override
    public boolean matches(STContext context, Resource resource) {
        return uid.equalsIgnoreCase(resource.getUid());
    }
}
