/*
 * Copyright (c) 2024 Aeontronix Inc
 */

package com.aeontronix.aeonbuild.dsl.query;

import com.aeontronix.aeonbuild.BuildContextImpl;
import com.aeontronix.aeonbuild.Resource;
import com.aeontronix.aeonbuild.dsl.AeonBuildLangParser;

/**
 * Expression used to match by id.
 */
public class UidExpression extends Expression {
    private String uid;

    public UidExpression(AeonBuildLangParser.QueryUidMatchContext idCtx, Resource baseResource) {
        if (baseResource == null) {
            uid = idCtx.getText();
        } else {
            uid = baseResource.getUid() + "." + idCtx.getText();
        }
    }

    @Override
    public boolean matches(BuildContextImpl context, Resource resource) {
        return uid.equalsIgnoreCase(resource.getUid());
    }
}
