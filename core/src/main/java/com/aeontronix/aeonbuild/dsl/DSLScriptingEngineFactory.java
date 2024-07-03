/*
 * Copyright (c) 2024 Aeontronix Inc
 */

package com.aeontronix.aeonbuild.dsl;

import com.aeontronix.aeonbuild.BuildContextImpl;
import org.jetbrains.annotations.NotNull;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: yannick
 * Date: 23/01/13
 * Time: 21:44
 * To change this template use File | Settings | File Templates.
 */
public class DSLScriptingEngineFactory implements ScriptEngineFactory {
    public static final String NAME = "AeonBuildDSL";
    private List<String> exts = Arrays.asList("stl");
    private BuildContextImpl ctx;

    public DSLScriptingEngineFactory(@NotNull BuildContextImpl ctx) {
        this.ctx = ctx;
    }

    @Override
    public String getEngineName() {
        return NAME;
    }

    @Override
    public String getEngineVersion() {
        return "0.9";
    }

    @Override
    public List<String> getExtensions() {
        return exts;
    }

    @Override
    public List<String> getMimeTypes() {
        return Collections.emptyList();
    }

    @Override
    public List<String> getNames() {
        return Arrays.asList(NAME);
    }

    @Override
    public String getLanguageName() {
        return NAME;
    }

    @Override
    public String getLanguageVersion() {
        return "0.9";
    }

    @Override
    public Object getParameter(String key) {
        return null;
    }

    @Override
    public String getMethodCallSyntax(String obj, String m, String... args) {
        return null;
    }

    @Override
    public String getOutputStatement(String toDisplay) {
        return null;
    }

    @Override
    public String getProgram(String... statements) {
        return null;
    }

    @Override
    public ScriptEngine getScriptEngine() {
        return new DSLScriptingEngine(ctx, this);
    }
}
