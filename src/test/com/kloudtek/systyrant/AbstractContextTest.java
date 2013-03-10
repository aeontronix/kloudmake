/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.systyrant;

import com.kloudtek.systyrant.exception.*;
import com.kloudtek.systyrant.resource.JavaResourceFactory;
import com.kloudtek.systyrant.resource.Resource;
import com.kloudtek.systyrant.resource.ResourceManager;
import org.testng.annotations.BeforeMethod;

import javax.script.ScriptException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

public class AbstractContextTest {
    public static final String TEST = "test:test";
    public static final String UNIQUETEST = "test:uniquetest";
    protected STContext ctx;
    protected ResourceManager resourceManager;

    @BeforeMethod
    public void init() throws STRuntimeException, InvalidResourceDefinitionException {
        ctx = new STContext();
        ctx.setFatalExceptions(Exception.class);
        resourceManager = ctx.getResourceManager();
        resourceManager.registerJavaResource(TestResource.class,"test:test");
    }

    public Resource createTestResource() throws ResourceCreationException {
        return resourceManager.createResource(TEST, null);
    }

    public Resource createTestResource(Resource dependency) throws ResourceCreationException {
        Resource testResource = createTestResource();
        testResource.addDependency(dependency);
        return testResource;
    }

    public Resource createTestResource(String id, Resource dependency) throws ResourceCreationException, InvalidAttributeException {
        Resource testResource = createTestResource(id);
        testResource.addDependency(dependency);
        return testResource;
    }

    public Resource createChildTestResource(String id, Resource parent) throws ResourceCreationException, InvalidAttributeException {
        Resource testResource = resourceManager.createResource(TEST, null, parent);
        testResource.setId(id);
        return testResource;
    }

    public Resource createTestResource(String id) throws ResourceCreationException, InvalidAttributeException {
        return createTestElement("id", id);
    }

    public Resource createTestElement(String attr, String val) throws ResourceCreationException, InvalidAttributeException {
        return createTestResource().set(attr, val);
    }

    public AbstractContextTest register( Class<?> clazz ) throws InvalidResourceDefinitionException {
        resourceManager.registerJavaResource(clazz,"test:"+clazz.getSimpleName().toLowerCase().replace("$",""));
        return this;
    }

    public AbstractContextTest register( Class<?> clazz, String name ) throws InvalidResourceDefinitionException {
        resourceManager.registerJavaResource(clazz,"test:"+name);
        return this;
    }

    public <X> X registerCreateExecuteReturnImpl( Class<X> clazz ) throws Throwable {
        registerAndCreate(clazz).execute();
        return findJavaAction(clazz);
    }

    public AbstractContextTest registerAndCreate( Class<?> clazz ) throws InvalidResourceDefinitionException, ResourceCreationException, InvalidAttributeException {
        return registerAndCreate(clazz,clazz.getSimpleName().replace("$","").toLowerCase());
    }

    public AbstractContextTest registerAndCreate( Class<?> clazz, String name ) throws InvalidResourceDefinitionException, ResourceCreationException, InvalidAttributeException {
        registerAndCreate(clazz,name,null);
        return this;
    }

    public AbstractContextTest registerAndCreate( Class<?> clazz, String name, String id ) throws InvalidResourceDefinitionException, ResourceCreationException, InvalidAttributeException {
        String fqname = "test:" + name;
        resourceManager.registerJavaResource(clazz, fqname);
        Resource resource = resourceManager.createResource(fqname);
        if( id != null ) {
            resource.setId(id);
        }
        return this;
    }

    public Resource create(Class<?> clazz) throws ResourceCreationException {
        return resourceManager.createResource("test:"+clazz.getSimpleName().toLowerCase().replace("$","."));
    }

    @SuppressWarnings("unchecked")
    public <X> X findJavaAction(Class<?> clazz) {
        for (Resource resource : ctx.getResourceManager()) {
            for (STAction action : resource.getActions()) {
                if( action instanceof JavaResourceFactory.JavaImpl ) {
                    Object impl = ((JavaResourceFactory.JavaImpl) action).getImpl();
                    clazz.isAssignableFrom(impl.getClass());
                    return (X) impl;
                }
            }
        }
        fail("Unable to find java action of class "+clazz.getName());
        return null;
    }

    public AbstractContextTest execute() throws Throwable {
        execute(true);
        return this;
    }

    public AbstractContextTest execute(boolean expected) throws Throwable {
        try {
            assertEquals(ctx.execute(), expected);
        } catch (STRuntimeException e) {
            if( e.getCause() != null ) {
                throw e.getCause();
            } else {
                throw e;
            }
        }
        return this;
    }

    public void enableVagrant() throws InvalidAttributeException, ResourceCreationException {
        Resource vagrant = resourceManager.createResource("vagrant:vagrant");
        vagrant.set("dir", "_vagrant");
        vagrant.set("box", "ubuntu-precise64");
        ctx.setDefaultParent(vagrant);
    }

    protected void executeDSLResource(String path) throws Throwable {
        ctx.runScript(getClass().getResource(path).toURI());
        execute();
    }

    protected void executeDSL(String dsl) throws Throwable {
        ctx.runDSLScript(dsl);
        execute();
    }

    protected <X> X registerService(Class<X> clazz) throws IllegalAccessException, InstantiationException, InvalidServiceException {
        return registerService(clazz.getSimpleName().toLowerCase(),clazz);
    }

    protected <X> X registerService(String name, Class<X> clazz) throws IllegalAccessException, InstantiationException, InvalidServiceException {
        X service = clazz.newInstance();
        ctx.getServiceManager().registerService(name, service);
        return service;
    }
}
