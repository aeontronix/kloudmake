/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.kloudmake.dsl.query;

import com.kloudtek.kloudmake.Resource;
import com.kloudtek.kloudmake.STContext;
import com.kloudtek.kloudmake.dsl.KloudmakeLangParser;
import com.kloudtek.kloudmake.exception.InvalidQueryException;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: ymenager
 * Date: 11/03/2013
 * Time: 18:10
 * To change this template use File | Settings | File Templates.
 */
public class ChildOfExpression extends Expression {
    boolean recurse;
    private ArrayList<Resource> parents = new ArrayList<>();

    public ChildOfExpression(KloudmakeLangParser.QueryChildOfMatchContext childOfContext, String query, STContext context, Resource baseResource) throws InvalidQueryException {
        if (childOfContext.exp != null) {
            Expression expression = Expression.create(childOfContext.exp, query, context, baseResource);
            for (Resource resource : context.getResourceManager()) {
                if (expression.matches(context, resource)) {
                    parents.add(resource);
                }
            }
        } else {
            Resource resource = context.currentResource();
            if (resource == null) {
                throw new InvalidQueryException("'childof' has no parameters specified and no resource is in scope: " + query);
            }
            parents.add(resource);
        }
        recurse = childOfContext.s != null;
    }

    @Override
    public boolean matches(STContext context, Resource resource) {
        if (recurse) {
            while (resource != null) {
                boolean match = match(resource);
                if (!match) {
                    resource = resource.getParent();
                } else {
                    return true;
                }
            }
            return false;
        } else {
            return match(resource);
        }
    }

    private boolean match(Resource resource) {
        Resource pres = resource.getParent();
        if (pres != null) {
            return parents.contains(pres);
        }
        return false;
    }
}
