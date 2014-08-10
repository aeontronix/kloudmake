/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.kloudmake.dsl.query;

import com.kloudtek.kloudmake.Resource;
import com.kloudtek.kloudmake.STContext;
import com.kloudtek.kloudmake.dsl.KloudmakeLangParser;

/**
 * Expression used to match by id.
 */
public class IdExpression extends Expression {
    private String id;
    private Resource base;

    public IdExpression(KloudmakeLangParser.QueryIdMatchContext idCtx, Resource baseResource) {
        base = baseResource;
        id = idCtx.getText();
    }

    @Override
    public boolean matches(STContext context, Resource resource) {
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
