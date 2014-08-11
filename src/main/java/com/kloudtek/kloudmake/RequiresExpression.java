/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.kloudmake;

import com.kloudtek.kloudmake.dsl.*;
import com.kloudtek.kloudmake.exception.*;
import org.antlr.v4.runtime.RecognitionException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: yannick
 * Date: 26/03/13
 * Time: 22:49
 * To change this template use File | Settings | File Templates.
 */
public class RequiresExpression {
    private final ArrayList<RequiredDependency> requiredDependencies = new ArrayList<>();
    private final Resource resource;

    public RequiresExpression(Resource resource, String expression) throws InvalidDependencyException {
        this.resource = resource;
        KloudmakeLangParser.RequiresContext reqCtx = null;
        try {
            reqCtx = AntlrDSLParser.createParser(expression).requires();
        } catch (RecognitionException e) {
            throw new InvalidDependencyException("Invalid requires expression: " + expression);
        }
        for (KloudmakeLangParser.RequiresTypeContext rtctx : AntLRUtils.nullToEmpty(reqCtx.requiresType())) {
            try {
                requiredDependencies.add(new RequiredDependency(rtctx));
            } catch (InvalidScriptException e) {
                throw new InvalidDependencyException(e.getMessage(), e);
            }
        }
    }

    public ArrayList<Resource> resolveRequires(KMContextImpl ctx) throws InvalidVariableException, ResourceCreationException, InvalidAttributeException {
        ArrayList<Resource> allMatches = new ArrayList<>();
        for (RequiredDependency requiredDependency : requiredDependencies) {
            requiredDependency.resolveAttrs(ctx);
            List<Resource> matches = requiredDependency.findMatches(ctx);
            if (matches.isEmpty()) {
                String id = requiredDependency.attrsResolved.remove("id");
                Resource newRes = ctx.getResourceManager().createResource(requiredDependency.name, id, null, ResourceMatcher.convert(ctx.getImports()));
                for (Map.Entry<String, String> entry : requiredDependency.attrsResolved.entrySet()) {
                    newRes.set(entry.getKey(), entry.getValue());
                }
                matches.add(newRes);
            }
            allMatches.addAll(matches);
        }
        return allMatches;
    }

    public class RequiredDependency {
        private final HashMap<String, Parameter> attrs = new HashMap<>();
        private final HashMap<String, String> attrsResolved = new HashMap<>();
        private FQName name;

        public HashMap<String, Parameter> getAttrs() {
            return attrs;
        }

        public FQName getName() {
            return name;
        }

        public RequiredDependency(KloudmakeLangParser.RequiresTypeContext reqCtx) throws InvalidScriptException {
            name = new FQName(reqCtx.id.getText());
            if (reqCtx.attrs != null) {
                for (KloudmakeLangParser.ParameterAssignmentContext parameterAssignmentContext : reqCtx.attrs.attr.parameterAssignment()) {
                    attrs.put(parameterAssignmentContext.anyId().getText(), Parameter.create(parameterAssignmentContext.staticOrDynamicValue()));
                }
            }
        }

        private List<Resource> findMatches(KMContextImpl ctx) throws InvalidVariableException {
            final ArrayList<Resource> matches = new ArrayList<>();
            for (Resource candidate : ctx.getResourceManager()) {
                if (name.matches(candidate.getType(), ctx)) {
                    if (attrMatch(candidate)) {
                        matches.add(candidate);
                    }
                }
            }
            return matches;
        }

        private boolean attrMatch(Resource candidate) {
            if (attrs.isEmpty()) {
                return true;
            } else {
                for (Map.Entry<String, String> entry : attrsResolved.entrySet()) {
                    String val = candidate.get(entry.getKey());
                    if (!entry.getValue().equalsIgnoreCase(val)) {
                        return false;
                    }
                }
                return true;
            }
        }

        public void resolveAttrs(KMContextImpl ctx) throws InvalidVariableException {
            for (Map.Entry<String, Parameter> entry : attrs.entrySet()) {
                try {
                    attrsResolved.put(entry.getKey(), entry.getValue().eval(ctx, resource));
                } catch (STRuntimeException e) {
                    throw new InvalidVariableException(e.getMessage(), e);
                }
            }
        }
    }
}
