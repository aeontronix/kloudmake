/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.systyrant.dsl.query;

import com.kloudtek.systyrant.Resource;
import com.kloudtek.systyrant.STContext;
import com.kloudtek.systyrant.dsl.SystyrantLangParser;
import com.kloudtek.systyrant.exception.InvalidQueryException;

public class SameHostExpression extends Expression {
    private final Resource baseResource;

    public SameHostExpression(SystyrantLangParser.QuerySameHostMatchContext sh, Resource baseResource) throws InvalidQueryException {
        if (baseResource == null) {
            throw new InvalidQueryException("samehost must only be used with a resource in scope");
        }
        this.baseResource = baseResource;
    }

    @Override
    public boolean matches(STContext context, Resource resource) {
        return resource.getHost() == baseResource.getHost() && baseResource != resource;
    }
}
