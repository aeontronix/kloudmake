/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.systyrant.dsl;

import com.kloudtek.systyrant.STContext;
import com.kloudtek.systyrant.Stage;
import com.kloudtek.systyrant.dsl.statement.Statement;
import com.kloudtek.systyrant.resource.ResourceFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ResourceDefinition {
    private final DSLScript dslScript;
    private String pkg;
    private String name;
    private Map<String, Parameter> defaultAttr = new HashMap<>();
    private Map<Stage, ArrayList<Statement>> statements = new HashMap<>();

    public ResourceDefinition(STContext ctx, DSLScript dslScript, String pkg, SystyrantLangParser.ResourceDefinitionContext defineElementContext) throws InvalidScriptException {
        this.dslScript = dslScript;
        SystyrantLangParser.FullyQualifiedIdContext fqid = defineElementContext.fullyQualifiedId();
        SystyrantLangParser.PackageNameContext pkgCtx = fqid.packageName();
        if (pkgCtx != null) {
            setPkg(pkgCtx.getText());
        } else {
            setPkg(pkg);
        }
        name = fqid.anyId().getText();
        SystyrantLangParser.ResourceDefinitionParamsContext resourceDefinitionParamsContext = defineElementContext.resourceDefinitionParams();
        if (resourceDefinitionParamsContext != null) {
            defaultAttr.putAll(Parameters.assignmentToMap(resourceDefinitionParamsContext.parameterAssignment()));
        }
        List<SystyrantLangParser.StatementContext> statements = defineElementContext.statement();
        if (statements != null) {
            for (SystyrantLangParser.StatementContext statement : statements) {
                addStatement(Statement.create(ctx, statement), Stage.PREPARE);
            }
        }
    }

    public String getPkg() {
        return pkg;
    }

    public void setPkg(String pkg) {
        this.pkg = pkg;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public synchronized void addStatement(Statement statement, Stage stage) {
        getStatementsForStage(stage).add(statement);
    }

    public synchronized List<Statement> getStatementsForStage(Stage stage) {
        ArrayList<Statement> list = statements.get(stage);
        if (list == null) {
            list = new ArrayList<>();
            statements.put(stage, list);
        }
        return list;
    }

    public ResourceFactory createFactory(String defaultPackage) {
        return new DSLResourceFactory(dslScript, pkg != null ? pkg : defaultPackage, name, statements, defaultAttr);
    }
}
