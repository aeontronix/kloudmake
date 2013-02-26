/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.systyrant.dsl;

import com.kloudtek.systyrant.FQName;
import com.kloudtek.systyrant.STContext;
import com.kloudtek.systyrant.dsl.statement.Statement;
import com.kloudtek.systyrant.exception.InvalidResourceDefinitionException;
import com.kloudtek.systyrant.resource.ResourceMatcher;

import javax.script.ScriptException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.kloudtek.systyrant.dsl.AntlrDSLParser.nullToEmpty;

public class DSLScript {
    private STContext ctx;
    private String defaultPackage;
    private List<ResourceMatcher> imports = new ArrayList<>();
    private List<ResourceDefinition> defines = new ArrayList<>();
    private List<Statement> statements = new ArrayList<>();
    private ResourceMatcher defaultPkgMatcher;

    public DSLScript(STContext ctx, String defaultPackage, SystyrantLangParser.StartContext startContext) throws InvalidScriptException {
        this.ctx = ctx;
        this.defaultPackage = defaultPackage;
        for (SystyrantLangParser.TopLvlFunctionsContext topLvlFunctionsContext : nullToEmpty(startContext.topLvlFunctions())) {
            parseTopLvlFunction(topLvlFunctionsContext);
        }
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
                defines.add(new ResourceDefinition(ctx, this, defaultPackage, resourceDefinitionContext));
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

    public List<ResourceMatcher> getImports() {
        return imports;
    }

    public void addImport(ResourceMatcher importPackage) {
        imports.add(importPackage);
    }

    public List<ResourceDefinition> getDefines() {
        return defines;
    }

    public void addDefine(ResourceDefinition resourceDefinition) {
        defines.add(resourceDefinition);
    }

    public void execute(STContext ctx) throws InvalidResourceDefinitionException, ScriptException {
        for (ResourceDefinition define : defines) {
            ctx.getResourceManager().registerResources(define.createFactory(defaultPackage));
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
