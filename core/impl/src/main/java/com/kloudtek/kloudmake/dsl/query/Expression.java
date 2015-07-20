/*
 * Copyright (c) 2015. Kelewan Technologies Ltd
 */

package com.kloudtek.kloudmake.dsl.query;

import com.kloudtek.kloudmake.KMContextImpl;
import com.kloudtek.kloudmake.Resource;
import com.kloudtek.kloudmake.dsl.KloudmakeLangParser;
import com.kloudtek.kloudmake.exception.InvalidQueryException;
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

    public static Expression create(KloudmakeLangParser.QueryExpressionContext expr, String query, KMContextImpl context, Resource baseResource) throws InvalidQueryException {
        if (expr.attrMatch != null) {
            return new AttrMatchExpression(expr.attrMatch, query, context);
        } else if (expr.bOp != null) {
            List<KloudmakeLangParser.QueryExpressionContext> exprs = expr.queryExpression();
            assert exprs.size() == 2;
            return new BinaryExpression(expr.bOp, exprs.get(0), exprs.get(1), query, context, baseResource);
        } else if (expr.co != null) {
            return new ChildOfExpression(expr.co, query, context, baseResource);
        } else if (expr.tm != null) {
            return new TypeExpression(expr.tm, query, context, baseResource);
        } else if (expr.id != null) {
            return new IdExpression(expr.id, baseResource);
        } else if (expr.uid != null) {
            return new UidExpression(expr.uid, baseResource);
        } else if (expr.sh != null) {
            return new SameHostExpression(expr.sh, context.currentResource());
        } else if (expr.bracketExpr != null) {
            return create(expr.bracketExpr, query, context, baseResource);
        } else {
            throw new InvalidQueryException(expr.getStart().getLine(), expr.getStart().getCharPositionInLine(), query);
        }
    }

    public abstract boolean matches(KMContextImpl context, Resource resource);
}
