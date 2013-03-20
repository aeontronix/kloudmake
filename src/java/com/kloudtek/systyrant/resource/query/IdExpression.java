/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.systyrant.resource.query;

import com.kloudtek.systyrant.STContext;
import com.kloudtek.systyrant.dsl.SystyrantLangParser;
import com.kloudtek.systyrant.resource.Resource;

/**
 * Created with IntelliJ IDEA.
 * User: ymenager
 * Date: 20/03/2013
 * Time: 22:07
 * To change this template use File | Settings | File Templates.
 */
public class IdExpression extends Expression {
    private String id;
    private boolean uidMatch;
    private Resource parent;

    public IdExpression(SystyrantLangParser.QueryIdMatchContext idCtx, Resource baseResource) {
        if( baseResource != null ) {
            parent = baseResource.getParent();
        } else {
            uidMatch = true;
        }
        id = idCtx.getText();
    }

    @Override
    public boolean matches(STContext context, Resource resource) {
        if( uidMatch ) {
            return id.equalsIgnoreCase(resource.getUid());
        } else {
            return resource.getParent() == parent && id.equalsIgnoreCase(resource.getId());
        }
    }
}
