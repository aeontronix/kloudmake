/*
 * Copyright (c) 2015. Kelewan Technologies Ltd
 */

package com.kloudtek.kloudmake.dsl.statement;

import com.kloudtek.kloudmake.KMContextImpl;
import com.kloudtek.kloudmake.Resource;
import com.kloudtek.kloudmake.dsl.DSLScript;
import com.kloudtek.kloudmake.dsl.InvalidScriptException;
import com.kloudtek.kloudmake.dsl.KloudmakeLangParser;

import javax.script.ScriptException;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: yannick
 * Date: 18/03/13
 * Time: 23:05
 * To change this template use File | Settings | File Templates.
 */
public class DepLinkedCreateResourceStatement extends Statement {
    private Statement leftStatement;
    private Statement rightStatement;
    private boolean forwardDep;

    public DepLinkedCreateResourceStatement(KMContextImpl ctx, KloudmakeLangParser.CreateResourceContext createResourceContext) throws InvalidScriptException {
        forwardDep = createResourceContext.rlk != null;
        leftStatement = createSubStatement(ctx, createResourceContext.ldep);
        rightStatement = createSubStatement(ctx, createResourceContext.rdep);
    }

    private Statement createSubStatement(KMContextImpl ctx, KloudmakeLangParser.CreateResourceContext createResourceContext) throws InvalidScriptException {
        if (createResourceContext.rlk != null || createResourceContext.llk != null) {
            return new DepLinkedCreateResourceStatement(ctx, createResourceContext);
        } else {
            return new CreateResourceStatement(ctx, createResourceContext);
        }
    }

    @Override
    public List<Resource> execute(DSLScript dslScript, Resource resource) throws ScriptException {
        List<Resource> leftResources = leftStatement.execute(dslScript, resource);
        List<Resource> rightResources = rightStatement.execute(dslScript, resource);
        if (forwardDep) {
            makeDependent(leftResources, rightResources);
        } else {
            makeDependent(rightResources, leftResources);
        }
        return rightResources;
    }

    private void makeDependent(List<Resource> leftResources, List<Resource> rightResources) {
        for (Resource resource : rightResources) {
            resource.addDependencies(leftResources);
        }
    }

    @Override
    public String toString() {
        return "linkeddeps{" + leftStatement + " -> " + rightStatement + "}";
    }
}
