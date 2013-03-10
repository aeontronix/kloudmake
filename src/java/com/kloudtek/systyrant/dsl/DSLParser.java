/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.systyrant.dsl;

import com.kloudtek.systyrant.STContext;

/** Interface for DSL parsers */
public interface DSLParser {
    DSLScript parse(STContext ctx, String script) throws InvalidScriptException;

    DSLScript parse(STContext ctx, String pkg, String script) throws InvalidScriptException;

    DSLScript parse(String script) throws InvalidScriptException;

    DSLScript parse(String pkg, String script) throws InvalidScriptException;
}
