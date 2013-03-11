/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.systyrant.exception;

import org.antlr.v4.runtime.Token;
import org.slf4j.Logger;

/**
 * Created with IntelliJ IDEA.
 * User: yannick
 * Date: 10/03/13
 * Time: 20:20
 * To change this template use File | Settings | File Templates.
 */
public class InvalidQueryException extends STException {
    public InvalidQueryException(int line, int charPositionInLine,String query) {
        super("(" + line + ":" + charPositionInLine + "): Invalid query " + query);
    }

    public InvalidQueryException(Token token, String query) {
        this(token.getLine(),token.getCharPositionInLine(),query);
    }
}
