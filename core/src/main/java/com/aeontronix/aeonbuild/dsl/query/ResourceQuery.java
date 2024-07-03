/*
 * Copyright (c) 2024 Aeontronix Inc
 */

package com.aeontronix.aeonbuild.dsl.query;

import com.aeontronix.aeonbuild.exception.InvalidQueryException;
import com.aeontronix.aeonbuild.BuildContextImpl;
import com.aeontronix.aeonbuild.Resource;
import com.aeontronix.aeonbuild.dsl.AeonBuildLangLexer;
import com.aeontronix.aeonbuild.dsl.AeonBuildLangParser;
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
    private final BuildContextImpl context;

    public ResourceQuery(BuildContextImpl context, String query, Resource baseResource) throws InvalidQueryException {
        this.context = context;
        AeonBuildLangParser parser = new AeonBuildLangParser(new CommonTokenStream(new AeonBuildLangLexer(new ANTLRInputStream(query))));
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
