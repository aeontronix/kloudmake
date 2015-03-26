/*
 * Copyright (c) 2015. Kelewan Technologies Ltd
 */

package com.kloudtek.kloudmake.dsl;

import com.kloudtek.kloudmake.KMContextImpl;

/**
 * Interface for DSL parsers
 */
public interface DSLParser {
    DSLScript parse(KMContextImpl ctx, String script) throws InvalidScriptException;

    DSLScript parse(KMContextImpl ctx, String pkg, String script) throws InvalidScriptException;

    DSLScript parse(String script) throws InvalidScriptException;

    DSLScript parse(String pkg, String script) throws InvalidScriptException;
}
