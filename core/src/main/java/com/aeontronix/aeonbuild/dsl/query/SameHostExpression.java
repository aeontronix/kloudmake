/*
 * Copyright (c) 2024 Aeontronix Inc
 */

package com.aeontronix.aeonbuild.dsl.query;

import com.aeontronix.aeonbuild.exception.InvalidQueryException;
import com.aeontronix.aeonbuild.BuildContextImpl;
import com.aeontronix.aeonbuild.Resource;
import com.aeontronix.aeonbuild.dsl.AeonBuildLangParser;

public class SameHostExpression extends Expression {
    private final Resource baseResource;

    public SameHostExpression(AeonBuildLangParser.QuerySameHostMatchContext sh, Resource baseResource) throws InvalidQueryException {
        if (baseResource == null) {
            throw new InvalidQueryException("samehost must only be used with a resource in scope");
        }
        this.baseResource = baseResource;
    }

    @Override
    public boolean matches(BuildContextImpl context, Resource resource) {
        return resource.getHost() == baseResource.getHost() && baseResource != resource;
    }
}
