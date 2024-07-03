/*
 * Copyright (c) 2024 Aeontronix Inc
 */

package com.aeontronix.aeonbuild.exception;

import org.antlr.v4.runtime.Token;

/**
 * Created with IntelliJ IDEA.
 * User: yannick
 * Date: 10/03/13
 * Time: 20:20
 * To change this template use File | Settings | File Templates.
 */
public class InvalidQueryException extends KMException {
    public InvalidQueryException(int line, int charPositionInLine, String query) {
        super("(" + line + ":" + charPositionInLine + "): Invalid query " + query);
    }

    public InvalidQueryException(Token token, String query) {
        this(token.getLine(), token.getCharPositionInLine(), query);
    }

    public InvalidQueryException() {
    }

    public InvalidQueryException(String message) {
        super(message);
    }
}
