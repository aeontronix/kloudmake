/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.systyrant.dsl;

import com.kloudtek.systyrant.Resource;
import com.kloudtek.systyrant.STContext;
import com.kloudtek.systyrant.Stage;
import com.kloudtek.systyrant.dsl.statement.Statement;
import com.kloudtek.systyrant.exception.InvalidResourceDefinitionException;
import com.kloudtek.systyrant.exception.STRuntimeException;
import com.kloudtek.systyrant.resource.AbstractAction;
import com.kloudtek.systyrant.resource.ResourceDefinition;
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

    public DSLResourceDefinition(STContext ctx, DSLScript dslScript, String pkg, SystyrantLangParser.ResourceDefinitionContext defineElementContext) throws InvalidScriptException {
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
            defaultAttr.putAll(Parameter.assignmentToMap(resourceDefinitionParamsContext.parameterAssignment()));
        }
        SystyrantLangParser.ResourceDefinitionStatementsContext resourceDefinitionStatementsContext = defineElementContext.resourceDefinitionStatements();
        if (resourceDefinitionStatementsContext != null) {
            List<SystyrantLangParser.StatementContext> statements = resourceDefinitionStatementsContext.statement();
            if (statements != null) {
                for (SystyrantLangParser.StatementContext statement : statements) {
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

    public ResourceDefinition toResDef(STContext ctx) throws InvalidResourceDefinitionException {
        ResourceDefinition resourceDefinition = new ResourceDefinition(pkg, name);
        for (Map.Entry<String, Parameter> attrEntry : defaultAttr.entrySet()) {
            Parameter value = attrEntry.getValue();
            // TODO fix dynamic params
            resourceDefinition.addDefaultAttr(attrEntry.getKey(), value.getRawValue());
        }
        for (Stage stage : Stage.values()) {
            List<Statement> statements = getStatementsForStage(stage);
            if (!statements.isEmpty()) {
                resourceDefinition.addAction(new DSLAction(dslScript, statements, stage));
            }
        }
        return resourceDefinition;
    }

    public class DSLAction extends AbstractAction {
        private final List<Statement> actionStatements;
        private final DSLScript dslScript;

        public DSLAction(DSLScript dslScript, List<Statement> actionStatements, Stage stage) {
            this.dslScript = dslScript;
            switch (stage) {
                case PREPARE:
                    type = Type.PREPARE;
                    break;
                case EXECUTE:
                    type = Type.EXECUTE;
                    break;
                case CLEANUP:
                    type = Type.CLEANUP;
                    break;
            }
            this.actionStatements = actionStatements;
        }

        @Override
        public void execute(STContext context, Resource resource) throws STRuntimeException {
            logger.debug("Executing all DSL statements for " + resource.toString());
            if (actionStatements != null) {
                for (Statement statement : actionStatements) {
                    try {
                        statement.execute(dslScript, resource);
                    } catch (ScriptException e) {
                        throw new STRuntimeException(e.getMessage(), e);
                    }
                }
            }
        }

        @Override
        public boolean checkExecutionRequired(STContext context, Resource resource) throws STRuntimeException {
            return true;
        }
    }
}
