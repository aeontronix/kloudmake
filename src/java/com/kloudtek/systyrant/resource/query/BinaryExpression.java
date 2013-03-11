/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.systyrant.resource.query;

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
                            SystyrantLangParser.QueryExpressionContext rightExprCtx, String query) throws InvalidQueryException {
        and = BinaryOp.valueOf(opCtx) == BinaryOp.AND;
        leftExpression = Expression.create(leftExprCtx,query);
        rightExpression = Expression.create(rightExprCtx,query);
    }

    @Override
    public boolean matches(Resource resource) {
        return and ? leftExpression.matches(resource) && rightExpression.matches(resource)
                : leftExpression.matches(resource) || rightExpression.matches(resource);
    }
}
