/*
 * Copyright (c) 2024 Aeontronix Inc
 */

package com.aeontronix.aeonbuild.dsl.query;

import com.aeontronix.aeonbuild.exception.InvalidQueryException;
import com.aeontronix.aeonbuild.BuildContextImpl;
import com.aeontronix.aeonbuild.Resource;
import com.aeontronix.aeonbuild.dsl.AeonBuildLangParser;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: ymenager
 * Date: 11/03/2013
 * Time: 23:36
 * To change this template use File | Settings | File Templates.
 */
public class DependsExpression extends Expression {
    boolean recurse;
    private ArrayList<Resource> resources = new ArrayList<>();

    public DependsExpression(AeonBuildLangParser.QueryDependsMatchContext depCtx, String query, BuildContextImpl context, Resource baseResource) throws InvalidQueryException {
        if (depCtx.exp != null) {
            Expression expression = Expression.create(depCtx.exp, query, context, baseResource);
            for (Resource resource : context.getResourceManager()) {
                if (expression.matches(context, resource)) {
                    resources.add(resource);
                }
            }
        } else {
            Resource resource = context.currentResource();
            if (resource == null) {
                throw new InvalidQueryException("'childof' has no parameters specified and no resource is in scope: " + query);
            }
            resources.add(resource);
        }
        recurse = depCtx.s != null;
    }

    @Override
    public boolean matches(BuildContextImpl context, Resource resource) {
        // todo fail is dependency resolution not done
        if (recurse) {
            return false;
        } else {
            for (Resource r : resources) {
                if (resource.getDependencies().contains(r)) {
                    return true;
                }
            }
            return false;
        }
    }
}
