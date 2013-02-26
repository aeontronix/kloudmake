/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.systyrant.dsl;

import com.kloudtek.systyrant.STAction;
import com.kloudtek.systyrant.STContext;
import com.kloudtek.systyrant.Stage;
import com.kloudtek.systyrant.dsl.statement.Statement;
import com.kloudtek.systyrant.exception.InvalidAttributeException;
import com.kloudtek.systyrant.exception.ResourceCreationException;
import com.kloudtek.systyrant.exception.STRuntimeException;
import com.kloudtek.systyrant.resource.Resource;
import com.kloudtek.systyrant.resource.ResourceFactory;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.ScriptException;
import java.util.ArrayList;
import java.util.Map;

public class DSLResourceFactory extends ResourceFactory {
    private static final Logger logger = LoggerFactory.getLogger(DSLResourceFactory.class);
    private final DSLScript dslScript;
    private final Map<Stage, ArrayList<Statement>> statements;
    private Map<String, Parameter> defaultAttr;

    public DSLResourceFactory(DSLScript dslScript, @NotNull String pkg, @NotNull String name,
                              Map<Stage, ArrayList<Statement>> statements, Map<String, Parameter> defaultAttr) {
        super(pkg, name);
        this.dslScript = dslScript;
        this.statements = statements;
        this.defaultAttr = defaultAttr;
    }

    @NotNull
    @Override
    public Resource create(STContext context) throws ResourceCreationException {
        Resource resource = new Resource(context, this);
        resource.addAction(new DSLAction());
        for (Map.Entry<String, Parameter> attrEntry : defaultAttr.entrySet()) {
            try {
                Parameter value = attrEntry.getValue();
                resource.set(attrEntry.getKey(), value.eval(STContext.get(), resource));
            } catch (InvalidAttributeException e) {
                throw new ResourceCreationException(e.getMessage(), e);
            }
        }
        return resource;
    }

    public class DSLAction implements STAction {
        @Override
        public void execute(Resource resource, Stage stage, boolean postChildren) throws STRuntimeException {
            ArrayList<Statement> list = statements.get(stage);
            logger.debug("Executing all DSL statements for " + resource.toString());
            if (list != null) {
                for (Statement statement : list) {
                    try {
                        statement.execute(dslScript, resource);
                    } catch (ScriptException e) {
                        throw new STRuntimeException(e.getMessage(), e);
                    }
                }
            }
        }
    }
}
