/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.systyrant.resource.query;

import com.kloudtek.systyrant.dsl.SystyrantLangLexer;
import com.kloudtek.systyrant.dsl.SystyrantLangParser;
import com.kloudtek.systyrant.exception.InvalidQueryException;
import com.kloudtek.systyrant.resource.Resource;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.BailErrorStrategy;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.misc.ParseCancellationException;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>A resource query is a query language that allows to retrieve resources from a context based on various parameters.</p>
 * The query string is in the format
 */
public class ResourceQuery {
    private final Expression expression;

    public ResourceQuery(String query) throws InvalidQueryException {
        SystyrantLangParser parser = new SystyrantLangParser(new CommonTokenStream(new SystyrantLangLexer(new ANTLRInputStream(query))));
        parser.setErrorHandler(new BailErrorStrategy());
        try {
            expression = Expression.create(parser.query().queryExpression(), query);
        } catch (ParseCancellationException e) {
            RecognitionException cause = (RecognitionException) e.getCause();
            throw new InvalidQueryException(cause.getOffendingToken(), query);
        }
    }

    public List<Resource> find(List<Resource> resources) {
        ArrayList<Resource> matches = new ArrayList<>();
        for (Resource resource : resources) {
            if( expression.matches(resource)) {
                matches.add(resource);
            }
        }
        return matches;
    }
}
