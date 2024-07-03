/*
 * Copyright (c) 2024 Aeontronix Inc
 */

package com.aeontronix.aeonbuild.dsl.query;

import com.aeontronix.aeonbuild.exception.InvalidQueryException;
import com.aeontronix.aeonbuild.BuildContextImpl;
import com.aeontronix.aeonbuild.Resource;
import com.aeontronix.aeonbuild.dsl.BinaryOp;
import com.aeontronix.aeonbuild.dsl.AeonBuildLangParser;

/**
 * Created with IntelliJ IDEA.
 * User: yannick
 * Date: 10/03/13
 * Time: 21:30
 * To change this template use File | Settings | File Templates.
 */
public class BinaryExpression extends Expression {
    private boolean and;
    private Expression leftExpression;
    private Expression rightExpression;

    public BinaryExpression(AeonBuildLangParser.BinaryOpContext opCtx,
                            AeonBuildLangParser.QueryExpressionContext leftExprCtx,
                            AeonBuildLangParser.QueryExpressionContext rightExprCtx, String query, BuildContextImpl context, Resource baseResource) throws InvalidQueryException {
        and = BinaryOp.valueOf(opCtx) == BinaryOp.AND;
        leftExpression = create(leftExprCtx, query, context, baseResource);
        rightExpression = create(rightExprCtx, query, context, baseResource);
    }

    @Override
    public boolean matches(BuildContextImpl context, Resource resource) {
        return and ? leftExpression.matches(context, resource) && rightExpression.matches(context, resource)
                : leftExpression.matches(context, resource) || rightExpression.matches(context, resource);
    }
}
