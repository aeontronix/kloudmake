/*
 * Copyright (c) 2024 Aeontronix Inc
 */

package com.aeontronix.aeonbuild.dsl;

import com.aeontronix.aeonbuild.BuildContextImpl;

/**
 * Interface for DSL parsers
 */
public interface DSLParser {
    DSLScript parse(BuildContextImpl ctx, String script) throws InvalidScriptException;

    DSLScript parse(BuildContextImpl ctx, String pkg, String script) throws InvalidScriptException;

    DSLScript parse(String script) throws InvalidScriptException;

    DSLScript parse(String pkg, String script) throws InvalidScriptException;
}
