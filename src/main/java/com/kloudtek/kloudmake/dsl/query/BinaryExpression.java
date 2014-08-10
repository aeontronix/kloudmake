/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.kloudmake.dsl.query;

import com.kloudtek.kloudmake.Resource;
import com.kloudtek.kloudmake.STContext;
import com.kloudtek.kloudmake.dsl.BinaryOp;
import com.kloudtek.kloudmake.dsl.KloudmakeLangParser;
import com.kloudtek.kloudmake.exception.InvalidQueryException;

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

    public BinaryExpression(KloudmakeLangParser.BinaryOpContext opCtx,
                            KloudmakeLangParser.QueryExpressionContext leftExprCtx,
                            KloudmakeLangParser.QueryExpressionContext rightExprCtx, String query, STContext context, Resource baseResource) throws InvalidQueryException {
        and = BinaryOp.valueOf(opCtx) == BinaryOp.AND;
        leftExpression = Expression.create(leftExprCtx, query, context, baseResource);
        rightExpression = Expression.create(rightExprCtx, query, context, baseResource);
    }

    @Override
    public boolean matches(STContext context, Resource resource) {
        return and ? leftExpression.matches(context, resource) && rightExpression.matches(context, resource)
                : leftExpression.matches(context, resource) || rightExpression.matches(context, resource);
    }
}
