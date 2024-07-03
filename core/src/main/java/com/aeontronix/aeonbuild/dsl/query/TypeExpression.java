/*
 * Copyright (c) 2024 Aeontronix Inc
 */

package com.aeontronix.aeonbuild.dsl.query;

import com.aeontronix.aeonbuild.FQName;
import com.aeontronix.aeonbuild.BuildContextImpl;
import com.aeontronix.aeonbuild.Resource;
import com.aeontronix.aeonbuild.dsl.AeonBuildLangParser;

/**
 * Created with IntelliJ IDEA.
 * User: ymenager
 * Date: 11/03/2013
 * Time: 19:38
 * To change this template use File | Settings | File Templates.
 */
public class TypeExpression extends Expression {
    private final FQName fqName;

    public TypeExpression(AeonBuildLangParser.QueryTypeMatchContext tm, String query, BuildContextImpl context, Resource baseResource) {
        fqName = new FQName(tm.t.getText());
    }

    @Override
    public boolean matches(BuildContextImpl context, Resource resource) {
        return fqName.equals(resource.getType());
    }
}
