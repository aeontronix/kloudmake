/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.systyrant.context.java;

import com.kloudtek.systyrant.Resource;
import com.kloudtek.systyrant.STContext;
import com.kloudtek.systyrant.annotation.Dependency;
import com.kloudtek.systyrant.context.AbstractAction;
import com.kloudtek.systyrant.context.ResourceImpl;
import com.kloudtek.systyrant.exception.InvalidRefException;
import com.kloudtek.systyrant.exception.STRuntimeException;

import java.util.HashSet;

import static com.kloudtek.systyrant.Action.Type.INIT;

/**
 * Created with IntelliJ IDEA.
 * User: yannick
 * Date: 24/03/13
 * Time: 17:13
 * To change this template use File | Settings | File Templates.
 */
public class ResourceInitAction extends AbstractAction {
    private final HashSet<String> requires;
    private final Dependency dependency;
    private Class<?> clazz;

    public ResourceInitAction(Class<?> clazz, HashSet<String> requires, Dependency dependency) {
        this.requires = requires;
        this.dependency = dependency;
        type = INIT;
        this.clazz = clazz;
    }

    @Override
    public void execute(STContext context, Resource resource) throws STRuntimeException {
        try {
            ((ResourceImpl) resource).addJavaImpl(clazz.newInstance());
            for (String require : requires) {
                resource.addRequires(require);
            }
            if (dependency != null) {
                resource.addDependency(dependency.value(), dependency.optional());
            }
        } catch (InstantiationException | IllegalAccessException e) {
            throw new STRuntimeException("Unable to create java resource instance " + clazz.getName() + " : " + e.getMessage(), e);
        } catch (InvalidRefException e) {
            throw new STRuntimeException("Invalid @Dependency annotation in " + clazz.getName() + " " + e.getMessage(), e);
        }
    }
}
