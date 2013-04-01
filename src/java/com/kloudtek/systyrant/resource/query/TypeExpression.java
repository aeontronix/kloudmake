/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.systyrant.resource.query;

import com.kloudtek.systyrant.FQName;
import com.kloudtek.systyrant.Resource;
import com.kloudtek.systyrant.STContext;
import com.kloudtek.systyrant.dsl.SystyrantLangParser;

/**
 * Created with IntelliJ IDEA.
 * User: ymenager
 * Date: 11/03/2013
 * Time: 19:38
 * To change this template use File | Settings | File Templates.
 */
public class TypeExpression extends Expression {
    private final FQName fqName;

    public TypeExpression(SystyrantLangParser.QueryTypeMatchContext tm, String query, STContext context, Resource baseResource) {
        fqName = new FQName(tm.t.getText());
    }

    @Override
    public boolean matches(STContext context, Resource resource) {
        return fqName.equals(resource.getType());
    }
}
