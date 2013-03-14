/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.systyrant.resource.query;

import com.kloudtek.systyrant.STContext;
import com.kloudtek.systyrant.dsl.BinaryOp;
import com.kloudtek.systyrant.dsl.SystyrantLangParser;
import com.kloudtek.systyrant.exception.InvalidQueryException;
import com.kloudtek.systyrant.resource.Resource;

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

    public BinaryExpression(SystyrantLangParser.BinaryOpContext opCtx,
                            SystyrantLangParser.QueryExpressionContext leftExprCtx,
                            SystyrantLangParser.QueryExpressionContext rightExprCtx, String query, STContext context) throws InvalidQueryException {
        and = BinaryOp.valueOf(opCtx) == BinaryOp.AND;
        leftExpression = Expression.create(leftExprCtx, query, context);
        rightExpression = Expression.create(rightExprCtx, query, context);
    }

    @Override
    public boolean matches(STContext context, Resource resource) {
        return and ? leftExpression.matches(context, resource) && rightExpression.matches(context, resource)
                : leftExpression.matches(context, resource) || rightExpression.matches(context, resource);
    }
}
