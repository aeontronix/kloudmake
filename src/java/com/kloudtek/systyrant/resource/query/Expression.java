/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.systyrant.resource.query;

import com.kloudtek.systyrant.STContext;
import com.kloudtek.systyrant.dsl.SystyrantLangParser;
import com.kloudtek.systyrant.exception.InvalidQueryException;
import com.kloudtek.systyrant.resource.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: yannick
 * Date: 10/03/13
 * Time: 20:14
 * To change this template use File | Settings | File Templates.
 */
public abstract class Expression {
    private static final Logger logger = LoggerFactory.getLogger(Expression.class);

    public static Expression create(SystyrantLangParser.QueryExpressionContext expr, String query, STContext context) throws InvalidQueryException {
        if (expr.attrMatch != null) {
            return new AttrMatchExpression(expr.attrMatch, query, context);
        } else if (expr.bOp != null) {
            List<SystyrantLangParser.QueryExpressionContext> exprs = expr.queryExpression();
            assert exprs.size() == 2;
            return new BinaryExpression(expr.bOp, exprs.get(0), exprs.get(1), query, context);
        } else if (expr.co != null) {
            return new ChildOfExpression(expr.co, query, context);
        } else if (expr.dep != null) {
            return new DependsExpression(expr.dep, query, context);
        } else if (expr.tm != null) {
            return new TypeExpression(expr.tm, query, context);
        } else {
            throw new InvalidQueryException(expr.getStart().getLine(), expr.getStart().getCharPositionInLine(), query);
        }
    }

    public abstract boolean matches(STContext context, Resource resource);
}
