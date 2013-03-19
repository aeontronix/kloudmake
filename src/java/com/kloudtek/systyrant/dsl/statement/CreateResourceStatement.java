/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.systyrant.dsl.statement;

import com.kloudtek.systyrant.FQName;
import com.kloudtek.systyrant.STContext;
import com.kloudtek.systyrant.dsl.*;
import com.kloudtek.systyrant.exception.InvalidAttributeException;
import com.kloudtek.systyrant.exception.InvalidVariableException;
import com.kloudtek.systyrant.exception.ResourceCreationException;
import com.kloudtek.systyrant.resource.Resource;

import javax.script.ScriptException;
import java.util.*;

public class CreateResourceStatement extends Statement {
    private FQName elementName;
    private List<Instance> instances = new ArrayList<>();
    private STContext ctx;

    public CreateResourceStatement(STContext ctx, SystyrantLangParser.CreateResourceContext createElementsContext) throws InvalidScriptException {
        this.ctx = ctx;
        elementName = new FQName(createElementsContext.elname.getText());
        Map<String, Parameter> params = new LinkedHashMap<>();
        SystyrantLangParser.CreateResourceParamsContext paramsCtx = createElementsContext.params;
        if (paramsCtx != null) {
            for (SystyrantLangParser.ParameterAssignmentContext pctx : AntLRUtils.nullToEmpty(paramsCtx.parameterAssignment())) {
                if (pctx != null) {
                    String paramName = pctx.paramName.getText();
                    Parameter parameter = Parameter.create(pctx.staticOrDynamicValue());
                    params.put(paramName, parameter);
                }
            }
        }
        SystyrantLangParser.CreateResourceStatementsContext resourceStatements = createElementsContext.createResourceStatements();
        if (resourceStatements != null) {
            SystyrantLangParser.CreateResourceSingleInstanceContext singleResource = resourceStatements.createResourceSingleInstance();
            if (singleResource != null) {
                parseResource(singleResource.createResourceInstanceId(), singleResource.createResourceInstanceElements(), params);
            } else {
                List<SystyrantLangParser.CreateResourceMultipleInstanceContext> multipleInstances = AntLRUtils.nullToEmpty(resourceStatements.createResourceMultipleInstance());
                for (SystyrantLangParser.CreateResourceMultipleInstanceContext instance : multipleInstances) {
                    parseResource(instance.createResourceInstanceId(), instance.createResourceInstanceElements(), params);
                }
            }
        }
    }

    private void parseResource(SystyrantLangParser.CreateResourceInstanceIdContext resourceInstanceId,
                               List<SystyrantLangParser.CreateResourceInstanceElementsContext> resourceInstanceElements, Map<String, Parameter> params) throws InvalidScriptException {
        Parameter id = resourceInstanceId != null ? Parameter.create(resourceInstanceId.staticOrDynamicValue()) : null;
        instances.add(new Instance(id, resourceInstanceElements, params));
    }

    public List<Instance> getInstances() {
        return Collections.unmodifiableList(instances);
    }

    @Override
    public List<Resource> execute(DSLScript dslScript, Resource parent) throws ScriptException {
        try {
            ArrayList<Resource> resources = new ArrayList<>();
            for (Instance instance : instances) {
                Resource resource = ctx.getResourceManager().createResource(elementName, dslScript.getImports(), parent);
                if (instance.id != null) {
                    resource.setId(instance.id.eval(ctx, resource));
                }
                for (Map.Entry<String, Parameter> pe : instance.parameters.entrySet()) {
                    resource.set(pe.getKey(), pe.getValue().eval(ctx, resource));
                }
                for (CreateResourceStatement children : instance.childrens) {
                    children.execute(dslScript, resource);
                }
                resources.add(resource);
            }
            return resources;
        } catch (ResourceCreationException | InvalidAttributeException | InvalidVariableException e) {
            throw new ScriptException(e);
        }
    }

    public FQName getElementName() {
        return elementName;
    }

    @Override
    public String toString() {
        return "createres{"+elementName+" : "+ instances+"}";
    }

    public class Instance {
        private Parameter id;
        private Map<String, Parameter> parameters = new LinkedHashMap<>();
        private List<CreateResourceStatement> childrens = new ArrayList<>();

        public Instance(Parameter id, List<SystyrantLangParser.CreateResourceInstanceElementsContext> resourceInstanceElements, Map<String, Parameter> params) throws InvalidScriptException {
            this.id = id;
            parameters.putAll(params);
            for (SystyrantLangParser.CreateResourceInstanceElementsContext elCtx : AntLRUtils.nullToEmpty(resourceInstanceElements)) {
                SystyrantLangParser.ParameterAssignmentContext param = elCtx.parameterAssignment();
                if (param != null) {
                    String paramName = param.paramName.getText();
                    Parameter parameter = Parameter.create(param.staticOrDynamicValue());
                    parameters.put(paramName, parameter);
                }
                SystyrantLangParser.CreateResourceInstanceChildContext createChildCtx = elCtx.createResourceInstanceChild();
                if (createChildCtx != null) {
                    childrens.add(new CreateResourceStatement(ctx, createChildCtx.createResource()));
                }
            }
        }

        public Parameter getId() {
            return id;
        }

        public Map<String, Parameter> getParameters() {
            return parameters;
        }

        public List<CreateResourceStatement> getChildrens() {
            return childrens;
        }

        @Override
        public String toString() {
            return "instance{"+id+":"+parameters+","+childrens+"}";
        }
    }
}
