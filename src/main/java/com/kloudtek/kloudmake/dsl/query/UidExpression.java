/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.kloudmake.dsl.query;

import com.kloudtek.kloudmake.KMContextImpl;
import com.kloudtek.kloudmake.Resource;
import com.kloudtek.kloudmake.dsl.KloudmakeLangParser;

/**
 * Expression used to match by id.
 */
public class UidExpression extends Expression {
    private String uid;

    public UidExpression(KloudmakeLangParser.QueryUidMatchContext idCtx, Resource baseResource) {
        if (baseResource == null) {
            uid = idCtx.getText();
        } else {
            uid = baseResource.getUid() + "." + idCtx.getText();
        }
    }

    @Override
    public boolean matches(KMContextImpl context, Resource resource) {
        return uid.equalsIgnoreCase(resource.getUid());
    }
}
