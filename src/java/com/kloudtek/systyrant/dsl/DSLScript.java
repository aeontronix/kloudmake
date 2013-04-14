/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.systyrant.dsl;

import com.kloudtek.systyrant.FQName;
import com.kloudtek.systyrant.ResourceManager;
import com.kloudtek.systyrant.STContext;
import com.kloudtek.systyrant.context.ResourceDefinition;
import com.kloudtek.systyrant.context.ResourceMatcher;
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

    public DSLScript(@NotNull STContext ctx, @Nullable String defaultPackage, @NotNull SystyrantLangParser.StartContext startContext) throws InvalidScriptException {
        source = ctx.getSourceUrl();
        this.ctx = ctx;
        this.defaultPackage = defaultPackage;
        for (SystyrantLangParser.TopLvlFunctionsContext topLvlFunctionsContext : nullToEmpty(startContext.topLvlFunctions())) {
            parseTopLvlFunction(topLvlFunctionsContext);
        }
    }

    public STContext getCtx() {
        return ctx;
    }

    public String getSource() {
        return source;
    }

    private void parseTopLvlFunction(SystyrantLangParser.TopLvlFunctionsContext topLvlFunctionsContext) throws InvalidScriptException {
        SystyrantLangParser.ImportPkgContext importPkgContext = topLvlFunctionsContext.importPkg();
        if (importPkgContext != null) {
            parseImport(importPkgContext);
        }
        SystyrantLangParser.StatementContext statementContext = topLvlFunctionsContext.statement();
        if (statementContext != null) {
            SystyrantLangParser.ResourceDefinitionContext resourceDefinitionContext = statementContext.resourceDefinition();
            if (resourceDefinitionContext != null) {
                defines.add(new DSLResourceDefinition(ctx, this, defaultPackage, resourceDefinitionContext));
            } else {
                statements.add(Statement.create(ctx, statementContext));
            }
        }
    }

    private void parseImport(SystyrantLangParser.ImportPkgContext node) {
        SystyrantLangParser.FullyQualifiedIdContext fqId = node.fullyQualifiedId();
        SystyrantLangParser.PackageNameContext pkgName = node.packageName();
        String importDec = fqId != null ? fqId.getText() : pkgName.getText();
        if (importDec.contains(":")) {
            addImport(new ResourceMatcher(new FQName(importDec)));
        } else {
            addImport(new ResourceMatcher(importDec, null));
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
