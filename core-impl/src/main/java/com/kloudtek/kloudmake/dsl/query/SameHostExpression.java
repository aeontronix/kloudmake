/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.kloudmake.dsl.query;

import com.kloudtek.kloudmake.KMContextImpl;
import com.kloudtek.kloudmake.Resource;
import com.kloudtek.kloudmake.dsl.KloudmakeLangParser;
import com.kloudtek.kloudmake.exception.InvalidQueryException;

public class SameHostExpression extends Expression {
    private final Resource baseResource;

    public SameHostExpression(KloudmakeLangParser.QuerySameHostMatchContext sh, Resource baseResource) throws InvalidQueryException {
        if (baseResource == null) {
            throw new InvalidQueryException("samehost must only be used with a resource in scope");
        }
        this.baseResource = baseResource;
    }

    @Override
    public boolean matches(KMContextImpl context, Resource resource) {
        return resource.getHost() == baseResource.getHost() && baseResource != resource;
    }
}
