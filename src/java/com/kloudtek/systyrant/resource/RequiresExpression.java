/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.systyrant.resource;

import com.kloudtek.systyrant.FQName;
import com.kloudtek.systyrant.Resource;
import com.kloudtek.systyrant.STContext;
import com.kloudtek.systyrant.dsl.AntLRUtils;
import com.kloudtek.systyrant.dsl.AntlrDSLParser;
import com.kloudtek.systyrant.dsl.Parameter;
import com.kloudtek.systyrant.dsl.SystyrantLangParser;
import com.kloudtek.systyrant.exception.InvalidAttributeException;
import com.kloudtek.systyrant.exception.InvalidDependencyException;
import com.kloudtek.systyrant.exception.InvalidVariableException;
import com.kloudtek.systyrant.exception.ResourceCreationException;
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
        SystyrantLangParser.RequiresContext reqCtx = null;
        try {
            reqCtx = AntlrDSLParser.createParser(expression).requires();
        } catch (RecognitionException e) {
            throw new InvalidDependencyException("Invalid requires expression: " + expression);
        }
        for (SystyrantLangParser.RequiresTypeContext rtctx : AntLRUtils.nullToEmpty(reqCtx.requiresType())) {
            requiredDependencies.add(new RequiredDependency(rtctx));
        }
    }

    public ArrayList<Resource> resolveRequires(STContext ctx) throws InvalidVariableException, ResourceCreationException, InvalidAttributeException {
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
            for (Resource match : matches) {
                resource.addDependency(match);
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

        public RequiredDependency(SystyrantLangParser.RequiresTypeContext reqCtx) {
            name = new FQName(reqCtx.id.getText());
            if (reqCtx.attrs != null) {
                for (SystyrantLangParser.ParameterAssignmentContext parameterAssignmentContext : reqCtx.attrs.attr.parameterAssignment()) {
                    attrs.put(parameterAssignmentContext.anyId().getText(), Parameter.create(parameterAssignmentContext.staticOrDynamicValue()));
                }
            }
        }

        private List<Resource> findMatches(STContext ctx) throws InvalidVariableException {
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

        public void resolveAttrs(STContext ctx) throws InvalidVariableException {
            for (Map.Entry<String, Parameter> entry : attrs.entrySet()) {
                attrsResolved.put(entry.getKey(), entry.getValue().eval(ctx, resource));
            }
        }
    }
}
