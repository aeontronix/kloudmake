/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.kloudmake.dsl.query;

import com.kloudtek.kloudmake.FQName;
import com.kloudtek.kloudmake.Resource;
import com.kloudtek.kloudmake.STContext;
import com.kloudtek.kloudmake.dsl.KloudmakeLangParser;

/**
 * Created with IntelliJ IDEA.
 * User: ymenager
 * Date: 11/03/2013
 * Time: 19:38
 * To change this template use File | Settings | File Templates.
 */
public class TypeExpression extends Expression {
    private final FQName fqName;

    public TypeExpression(KloudmakeLangParser.QueryTypeMatchContext tm, String query, STContext context, Resource baseResource) {
        fqName = new FQName(tm.t.getText());
    }

    @Override
    public boolean matches(STContext context, Resource resource) {
        return fqName.equals(resource.getType());
    }
}
