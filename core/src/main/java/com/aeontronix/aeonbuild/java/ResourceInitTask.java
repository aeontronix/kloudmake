/*
 * Copyright (c) 2024 Aeontronix Inc
 */

package com.aeontronix.aeonbuild.java;

import com.aeontronix.aeonbuild.annotation.Dependency;
import com.aeontronix.aeonbuild.exception.InvalidRefException;
import com.aeontronix.aeonbuild.exception.KMRuntimeException;
import com.aeontronix.aeonbuild.*;

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
    public void execute(BuildContextImpl context, Resource resource) throws KMRuntimeException {
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
