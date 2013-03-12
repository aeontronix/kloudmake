/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.systyrant.resource.query;

import com.kloudtek.systyrant.STContext;
import com.kloudtek.systyrant.dsl.SystyrantLangParser;
import com.kloudtek.systyrant.exception.InvalidQueryException;
import com.kloudtek.systyrant.resource.Resource;

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

    public DependsExpression(SystyrantLangParser.QueryDependsMatchContext depCtx, String query, STContext context) throws InvalidQueryException {
        if (depCtx.exp != null) {
            Expression expression = Expression.create(depCtx.exp, query, context);
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
    public boolean matches(STContext context, Resource resource) {
        // todo fail is dependency resolution not done
        if(recurse) {
            return false;
        } else {
            for (Resource r : resources) {
                if( resource.getDependencies().contains(r) ) {
                    return true;
                }
            }
            return false;
        }
    }
}
