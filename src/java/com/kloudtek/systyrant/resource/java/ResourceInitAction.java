/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.systyrant.resource.java;

import com.kloudtek.systyrant.STContext;
import com.kloudtek.systyrant.exception.STRuntimeException;
import com.kloudtek.systyrant.resource.AbstractAction;
import com.kloudtek.systyrant.resource.Resource;

import static com.kloudtek.systyrant.resource.Action.Type.INIT;

/**
* Created with IntelliJ IDEA.
* User: yannick
* Date: 24/03/13
* Time: 17:13
* To change this template use File | Settings | File Templates.
*/
public class ResourceInitAction extends AbstractAction {
    private Class<?> clazz;

    public ResourceInitAction(Class<?> clazz) {
        type = INIT;
        this.clazz = clazz;
    }

    @Override
    public void execute(STContext context, Resource resource) throws STRuntimeException {
        try {
            resource.addJavaImpl(clazz.newInstance());
        } catch (InstantiationException | IllegalAccessException e) {
            throw new STRuntimeException("Unable to create java resource instance " + e.getMessage(), e);
        }
    }
}
