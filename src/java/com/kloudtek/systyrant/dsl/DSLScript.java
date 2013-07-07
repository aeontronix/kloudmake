/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.systyrant.dsl;

import com.kloudtek.systyrant.*;
import com.kloudtek.systyrant.dsl.statement.Statement;
import com.kloudtek.systyrant.exception.InvalidResourceDefinitionException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.script.ScriptException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.kloudtek.systyrant.dsl.AntLRUtils.nullToEmpty;

public class DSLScript {
    private STContext ctx;
    private String source;
    private String defaultPackage;
    private List<ResourceMatcher> imports = new ArrayList<>();
    private List<DSLResourceDefinition> defines = new ArrayList<>();
    private List<Statement> statements = new ArrayList<>();
    private ResourceMatcher defaultPkgMatcher;

    public DSLScript(@NotNull STContext ctx, @Nullable String defaultPackage, @NotNull SystyrantLangParser.ScriptContext startContext) throws InvalidScriptException {
        source = ctx.getSourceUrl();
        this.ctx = ctx;
        this.defaultPackage = defaultPackage;
        for (SystyrantLangParser.StatementContext statementContext : nullToEmpty(startContext.statement())) {
            parseStatements(statementContext);
        }
    }

    public STContext getCtx() {
        return ctx;
    }

    public String getSource() {
        return source;
    }

    private void parseStatements(SystyrantLangParser.StatementContext stCtx) throws InvalidScriptException {
        if (stCtx.imp != null) {
            parseImport(stCtx.imp);
        } else if (stCtx.define != null) {
            defines.add(new DSLResourceDefinition(ctx, this, defaultPackage, stCtx.define));
        } else {
            statements.add(Statement.create(ctx, stCtx));
        }
    }

    private void parseImport(SystyrantLangParser.ImportPkgContext node) {
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

    public void execute(STContext ctx) throws InvalidResourceDefinitionException, ScriptException {
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
