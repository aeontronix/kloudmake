/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.kloudmake.dsl;

import com.kloudtek.kloudmake.STContext;
import com.kloudtek.kloudmake.exception.InvalidResourceDefinitionException;
import com.kloudtek.util.io.IOUtils;
import org.jetbrains.annotations.NotNull;

import javax.script.*;
import java.io.IOException;
import java.io.Reader;

import static javax.script.ScriptContext.ENGINE_SCOPE;

public class DSLScriptingEngine extends AbstractScriptEngine {
    private DSLParser parser = new AntlrDSLParser();
    private final STContext ctx;
    private ScriptEngineFactory factory;

    public DSLScriptingEngine(@NotNull STContext ctx, @NotNull DSLScriptingEngineFactory factory) {
        this.ctx = ctx;
        this.factory = factory;
    }

    @Override
    public Object eval(String script, ScriptContext context) throws ScriptException {
        try {
            String pkg = (String) context.getBindings(ENGINE_SCOPE).get("package");
            if (pkg == null) {
                pkg = "default";
            }
            DSLScript dslScript = parser.parse(ctx, pkg, script);
            dslScript.execute(ctx);
        } catch (InvalidScriptException | InvalidResourceDefinitionException e) {
            throw new ScriptException(e);
        }
        return null;
    }

    @Override
    public Object eval(Reader reader, ScriptContext context) throws ScriptException {
        try {
            return eval(IOUtils.toString(reader), context);
        } catch (IOException e) {
            throw new ScriptException(e);
        }
    }

    @Override
    public Bindings createBindings() {
        return null;
    }

    @Override
    public ScriptEngineFactory getFactory() {
        return factory;
    }
}
