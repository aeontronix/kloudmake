/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.kloudmake.dsl;

import com.kloudtek.kloudmake.*;
import com.kloudtek.kloudmake.dsl.statement.Statement;
import com.kloudtek.kloudmake.exception.InvalidResourceDefinitionException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.script.ScriptException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.kloudtek.kloudmake.dsl.AntLRUtils.nullToEmpty;

public class DSLScript {
    private KMContextImpl ctx;
    private String source;
    private String defaultPackage;
    private List<ResourceMatcher> imports = new ArrayList<>();
    private List<DSLResourceDefinition> defines = new ArrayList<>();
    private List<Statement> statements = new ArrayList<>();
    private ResourceMatcher defaultPkgMatcher;

    public DSLScript(@NotNull KMContextImpl ctx, @Nullable String defaultPackage, @NotNull KloudmakeLangParser.ScriptContext startContext) throws InvalidScriptException {
        source = ctx.getSourceUrl();
        this.ctx = ctx;
        this.defaultPackage = defaultPackage;
        for (KloudmakeLangParser.StatementContext statementContext : nullToEmpty(startContext.statement())) {
            parseStatements(statementContext);
        }
    }

    public KMContextImpl getCtx() {
        return ctx;
    }

    public String getSource() {
        return source;
    }

    private void parseStatements(KloudmakeLangParser.StatementContext stCtx) throws InvalidScriptException {
        if (stCtx.imp != null) {
            parseImport(stCtx.imp);
        } else if (stCtx.define != null) {
            defines.add(new DSLResourceDefinition(ctx, this, defaultPackage, stCtx.define));
        } else {
            statements.add(Statement.create(ctx, stCtx));
        }
    }

    private void parseImport(KloudmakeLangParser.ImportPkgContext node) {
        String pkg = node.pkg.getText();
        String type = node.type != null ? node.type.getText() : null;
        if (type != null) {
            addImport(new ResourceMatcher(new FQName(pkg, type)));
        } else {
            addImport(new ResourceMatcher(pkg, null));
        }
    }

    public String getSourceUrl() {
        return source;
    }

    public List<ResourceMatcher> getImports() {
        return imports;
    }

    public void addImport(ResourceMatcher importPackage) {
        imports.add(importPackage);
    }

    public List<DSLResourceDefinition> getDefines() {
        return defines;
    }

    public void addDefine(DSLResourceDefinition resourceDefStatement) {
        defines.add(resourceDefStatement);
    }

    public void execute(KMContextImpl ctx) throws InvalidResourceDefinitionException, ScriptException {
        ResourceManager resourceManager = ctx.getResourceManager();
        for (DSLResourceDefinition define : defines) {
            ResourceDefinition resourceDefinition = define.toResDef(ctx);
            resourceManager.registerResourceDefinition(resourceDefinition);
        }
        for (Statement statement : statements) {
            statement.execute(this, null);
        }
    }

    public void setDefaultPackage(String defaultPackage) {
        if (defaultPkgMatcher != null) {
            imports.remove(defaultPkgMatcher);
        }
        if (defaultPackage != null) {
            defaultPkgMatcher = new ResourceMatcher(defaultPackage, null);
            imports.add(defaultPkgMatcher);
        }
        this.defaultPackage = defaultPackage;
    }

    public String getDefaultPackage() {
        return defaultPackage;
    }

    public void addStatement(Statement statement) {
        statements.add(statement);
    }

    public List<Statement> getStatements() {
        return Collections.unmodifiableList(statements);
    }
}
