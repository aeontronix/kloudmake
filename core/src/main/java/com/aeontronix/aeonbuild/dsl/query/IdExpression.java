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
public class IdExpression extends Expression {
    private String id;
    private Resource base;

    public IdExpression(AeonBuildLangParser.QueryIdMatchContext idCtx, Resource baseResource) {
        base = baseResource;
        id = idCtx.getText();
    }

    @Override
    public boolean matches(BuildContextImpl context, Resource resource) {
        return isWithinScope(resource) && id.equalsIgnoreCase(resource.getId());
    }

    private boolean isWithinScope(Resource resource) {
        Resource rp = resource.getParent();
        do {
            if (rp == base) {
                return true;
            } else if (rp != null) {
                rp = rp.getParent();
                if (rp == null && base == null) {
                    return true;
                }
            }
        } while (rp != null);
        return false;
    }
}
