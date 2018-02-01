/*
 * Copyright (c) 2015. Kelewan Technologies Ltd
 */

package com.kloudtek.kloudmake.dsl.query;

import com.kloudtek.kloudmake.KMContextImpl;
import com.kloudtek.kloudmake.Resource;
import com.kloudtek.kloudmake.dsl.KloudmakeLangLexer;
import com.kloudtek.kloudmake.dsl.KloudmakeLangParser;
import com.kloudtek.kloudmake.exception.InvalidQueryException;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.BailErrorStrategy;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>A resource query is a query language that allows to retrieve resources from a context based on various parameters.</p>
 * The query string is in the format
 */
public class ResourceQuery {
    private final Expression expression;
    private final KMContextImpl context;

    public ResourceQuery(KMContextImpl context, String query, Resource baseResource) throws InvalidQueryException {
        this.context = context;
        KloudmakeLangParser parser = new KloudmakeLangParser(new CommonTokenStream(new KloudmakeLangLexer(new ANTLRInputStream(query))));
        parser.setErrorHandler(new BailErrorStrategy());
        try {
            expression = Expression.create(parser.query().queryExpression(), query, context, baseResource);
        } catch (ParseCancellationException e) {
            RecognitionException cause = (RecognitionException) e.getCause();
            throw new InvalidQueryException(cause.getOffendingToken(), query);
        }
    }

    @NotNull
    public List<Resource> find(List<Resource> resources) {
        ArrayList<Resource> matches = new ArrayList<>();
        for (Resource resource : resources) {
            if (expression.matches(context, resource)) {
                matches.add(resource);
            }
        }
        return matches;
    }
}
