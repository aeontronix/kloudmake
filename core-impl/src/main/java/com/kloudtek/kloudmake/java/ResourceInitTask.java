/*
 * Copyright (c) 2015. Kelewan Technologies Ltd
 */

package com.kloudtek.kloudmake.java;

import com.kloudtek.kloudmake.*;
import com.kloudtek.kloudmake.annotation.Dependency;
import com.kloudtek.kloudmake.exception.InvalidRefException;
import com.kloudtek.kloudmake.exception.KMRuntimeException;

import java.util.HashSet;

/**
 * Created with IntelliJ IDEA.
 * User: yannick
 * Date: 24/03/13
 * Time: 17:13
 * To change this template use File | Settings | File Templates.
 */
public class ResourceInitTask extends AbstractTask {
    private final HashSet<String> requires;
    private final Dependency dependency;
    private Class<?> clazz;

    public ResourceInitTask(Class<?> clazz, HashSet<String> requires, Dependency dependency) {
        this.requires = requires;
        this.dependency = dependency;
        stage = Stage.INIT;
        this.clazz = clazz;
    }

    @Override
    public void execute(KMContextImpl context, Resource resource) throws KMRuntimeException {
        try {
            ((ResourceImpl) resource).addJavaImpl(clazz.newInstance());
            for (String require : requires) {
                resource.addRequires(require);
            }
            if (dependency != null) {
                resource.addDependency(dependency.value(), dependency.optional());
            }
        } catch (InstantiationException | IllegalAccessException e) {
            throw new KMRuntimeException("Unable to create java resource instance " + clazz.getName() + " : " + e.getMessage(), e);
        } catch (InvalidRefException e) {
            throw new KMRuntimeException("Invalid @Dependency annotation in " + clazz.getName() + " " + e.getMessage(), e);
        }
    }
}
