/*
 * Copyright (c) 2015. Kelewan Technologies Ltd
 */

package com.kloudtek.kloudmake.dsl;

import com.kloudtek.kloudmake.*;
import com.kloudtek.kloudmake.dsl.statement.Statement;
import com.kloudtek.kloudmake.exception.InvalidResourceDefinitionException;
import com.kloudtek.kloudmake.exception.KMRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.ScriptException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DSLResourceDefinition {
    private static final Logger logger = LoggerFactory.getLogger(DSLResourceDefinition.class);
    private final DSLScript dslScript;
    private String pkg;
    private String name;
    private Map<String, Parameter> defaultAttr = new HashMap<>();
    private Map<Stage, ArrayList<Statement>> statements = new HashMap<>();

    public DSLResourceDefinition(KMContextImpl ctx, DSLScript dslScript, String pkg, KloudmakeLangParser.ResourceDefinitionContext defineElementContext) throws InvalidScriptException {
        this.dslScript = dslScript;
        KloudmakeLangParser.FullyQualifiedIdContext fqid = defineElementContext.fullyQualifiedId();
        KloudmakeLangParser.PackageNameContext pkgCtx = fqid.packageName();
        if (pkgCtx != null) {
            setPkg(pkgCtx.getText());
        } else {
            setPkg(pkg);
        }
        name = fqid.anyId().getText();
        KloudmakeLangParser.ResourceDefinitionParamsContext resourceDefinitionParamsContext = defineElementContext.resourceDefinitionParams();
        if (resourceDefinitionParamsContext != null) {
            defaultAttr.putAll(Parameter.assignmentToMap(resourceDefinitionParamsContext.parameterAssignment()));
        }
        KloudmakeLangParser.ResourceDefinitionStatementsContext resourceDefinitionStatementsContext = defineElementContext.resourceDefinitionStatements();
        if (resourceDefinitionStatementsContext != null) {
            List<KloudmakeLangParser.StatementContext> statements = resourceDefinitionStatementsContext.statement();
            if (statements != null) {
                for (KloudmakeLangParser.StatementContext statement : statements) {
                    addStatement(Statement.create(ctx, statement), Stage.PREPARE);
                }
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

    public ResourceDefinition toResDef(KMContextImpl ctx) throws InvalidResourceDefinitionException {
        ResourceDefinition resourceDefinition = new ResourceDefinition(pkg, name);
        for (Map.Entry<String, Parameter> attrEntry : defaultAttr.entrySet()) {
            Parameter value = attrEntry.getValue();
            // TODO fix dynamic params
            resourceDefinition.addDefaultAttr(attrEntry.getKey(), value.getRawValue());
        }
        for (Stage stage : Stage.values()) {
            List<Statement> statements = getStatementsForStage(stage);
            if (!statements.isEmpty()) {
                resourceDefinition.addAction(new DSLTask(dslScript, statements, stage));
            }
        }
        return resourceDefinition;
    }

    public class DSLTask extends AbstractTask {
        private final List<Statement> actionStatements;
        private final DSLScript dslScript;

        public DSLTask(DSLScript dslScript, List<Statement> actionStatements, Stage stage) {
            this.dslScript = dslScript;
            this.stage = stage;
            this.actionStatements = actionStatements;
        }

        @Override
        public void execute(KMContextImpl context, Resource resource) throws KMRuntimeException {
            logger.debug("Executing all DSL statements for " + resource.toString());
            String old = context.getSourceUrl();
            context.setSourceUrl(dslScript.getSourceUrl());
            try {
                if (actionStatements != null) {
                    for (Statement statement : actionStatements) {
                        try {
                            statement.execute(dslScript, resource);
                        } catch (ScriptException e) {
                            throw new KMRuntimeException(e.getMessage(), e);
                        }
                    }
                }
            } finally {
                if (old != null) {
                    context.setSourceUrl(old);
                } else {
                    context.clearSourceUrl();
                }
            }
        }

        @Override
        public boolean checkExecutionRequired(KMContextImpl context, Resource resource) throws KMRuntimeException {
            return true;
        }
    }
}
