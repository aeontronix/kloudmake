/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.systyrant.dsl;

import org.antlr.v4.runtime.Token;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class InvalidScriptException extends RuntimeException {
    private String location;
    private String token;

    public InvalidScriptException() {
    }

    public InvalidScriptException(String message, Exception cause) {
        super(message, cause);
    }

    public InvalidScriptException(@NotNull String message, @NotNull String location, @Nullable String token, @Nullable Exception cause) {
        super("[" + location + "]: " + message, cause);
        this.location = location;
        this.token = token;
    }


    public InvalidScriptException(String location, String token, Exception cause) {
        this("Invalid text found: " + token, location, token, cause);
    }

    public InvalidScriptException(String message, Token token) {
        super(message);
        location = token.getLine() + ":" + token.getCharPositionInLine();
    }

    public InvalidScriptException(String message) {
        super(message);
    }

    public InvalidScriptException(Token offendingToken) {
        super("[" + location(offendingToken) + "] unexpected token: " + offendingToken.getText());
        location = location(offendingToken);
    }

    public String getLocation() {
        return location;
    }

    public String getToken() {
        return token;
    }

    public static String location(Token token) {
        return token.getLine() + ":" + token.getCharPositionInLine();
    }
}
