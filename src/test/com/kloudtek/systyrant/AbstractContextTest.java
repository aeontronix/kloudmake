/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.systyrant;

import com.kloudtek.systyrant.exception.*;
import com.kloudtek.systyrant.resource.AbstractAction;
import com.kloudtek.systyrant.resource.Resource;
import com.kloudtek.systyrant.resource.ResourceManager;
import org.testng.annotations.BeforeMethod;

import javax.script.ScriptException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.testng.Assert.*;

public class AbstractContextTest {
    public static final String JTEST = "test:jtest";
    public static final String TEST = "test:test";
    public static final String UNIQUETEST = "test:uniquetest";
    protected STContext ctx;
    protected ResourceManager resourceManager;

    @SuppressWarnings("unchecked")
    @BeforeMethod
    public void init() throws STRuntimeException, InvalidResourceDefinitionException, IOException, ScriptException {
        ctx = new STContext();
        ctx.setFatalExceptions(Exception.class);
        resourceManager = ctx.getResourceManager();
        resourceManager.registerJavaResource(TestResource.class, JTEST);
        ctx.runDSLScript("def test:test {}");
    }

    public Resource createTestResource() throws ResourceCreationException {
        return resourceManager.createResource(TEST);
    }

    public Resource createTestResource(String id) throws ResourceCreationException, InvalidAttributeException {
        Resource testResource = createTestResource();
        testResource.setId(id);
        return testResource;
    }

    public Resource createTestResource(String id, Resource dependency) throws ResourceCreationException, InvalidAttributeException {
        Resource testResource = createTestResource();
        testResource.setId(id);
        testResource.addDependency(dependency);
        return testResource;
    }

    public Resource createTestResource(String attrName, String attrValue) throws ResourceCreationException, InvalidAttributeException {
        Resource testResource = createTestResource();
        testResource.set(attrName,attrValue);
        return testResource;
    }

    public Resource createJavaTestResource() throws ResourceCreationException {
        return resourceManager.createResource(JTEST);
    }

    public Resource createJavaTestResource(String id, Resource dependency) throws ResourceCreationException, InvalidAttributeException {
        Resource testResource = createJavaTestResource(id);
        testResource.addDependency(dependency);
        return testResource;
    }

    public Resource createChildTestResource(String id, Resource parent) throws ResourceCreationException, InvalidAttributeException {
        Resource testResource = resourceManager.createResource(JTEST, null, parent);
        testResource.setId(id);
        return testResource;
    }

    public Resource createJavaTestResource(String id) throws ResourceCreationException, InvalidAttributeException {
        return createJavaTestElement("id", id);
    }

    public Resource createJavaTestElement(String attr, String val) throws ResourceCreationException, InvalidAttributeException {
        return createJavaTestResource().set(attr, val);
    }

    public AbstractContextTest register(Class<?> clazz) throws InvalidResourceDefinitionException {
        resourceManager.registerJavaResource(clazz, "test:" + clazz.getSimpleName().toLowerCase().replace("$", ""));
        return this;
    }

    public AbstractContextTest register(Class<?> clazz, String name) throws InvalidResourceDefinitionException {
        resourceManager.registerJavaResource(clazz, "test:" + name);
        return this;
    }

    public AbstractContextTest registerAndCreate(Class<?> clazz) throws InvalidResourceDefinitionException, ResourceCreationException, InvalidAttributeException {
        return registerAndCreate(clazz, clazz.getSimpleName().replace("$", "").toLowerCase());
    }

    public AbstractContextTest registerAndCreate(Class<?> clazz, String name) throws InvalidResourceDefinitionException, ResourceCreationException, InvalidAttributeException {
        registerAndCreate(clazz, name, null);
        return this;
    }

    public AbstractContextTest registerAndCreate(Class<?> clazz, String name, String id) throws InvalidResourceDefinitionException, ResourceCreationException, InvalidAttributeException {
        String fqname = "test:" + name;
        resourceManager.registerJavaResource(clazz, fqname);
        Resource resource = resourceManager.createResource(fqname);
        if (id != null) {
            resource.setId(id);
        }
        return this;
    }

    public Resource create(Class<?> clazz) throws ResourceCreationException {
        return resourceManager.createResource("test:" + clazz.getSimpleName().toLowerCase().replace("$", "."));
    }

    @SuppressWarnings("unchecked")
    public <X> X findJavaAction(Class<X> clazz) {
        for (Resource resource : ctx.getResourceManager()) {
            X impl = resource.getJavaImpl(clazz);
            if (impl != null) {
                return impl;
            }
        }
        fail("Unable to find java action of class " + clazz.getName());
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
            if (e.getCause() != null) {
                throw e.getCause();
            } else {
                throw e;
            }
        }
        return this;
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
        return registerService(clazz.getSimpleName().toLowerCase(), clazz);
    }

    protected <X> X registerService(String name, Class<X> clazz) throws IllegalAccessException, InstantiationException, InvalidServiceException {
        X service = clazz.newInstance();
        ctx.getServiceManager().registerService(name, service);
        return service;
    }

    protected void assertContainsSame(Collection<Resource> actual, Resource... expected) {
        assertNotNull(actual);
        assertEquals(actual.size(), expected.length);
        ArrayList<Resource> list = new ArrayList<>(actual);
        for (Resource resource : expected) {
            boolean found = false;
            for (Resource rs : list) {
                if (rs == resource) {
                    list.remove(rs);
                    found = true;
                    break;
                }
            }
            if (!found) {
                fail("Failed to find " + resource);
            }
        }
        if (!list.isEmpty()) {
            fail("Unexpected resources " + list);
        }
    }

    protected void assertBefore(Resource before, Resource... after) {
        List<Resource> aftRes = Arrays.asList(after);
        for (Resource r : ctx.getResourceManager()) {
            if (r == before) {
                return;
            } else if (aftRes.contains(r)) {
                fail("Resource " + r + " is before " + before);
            }
        }
    }

    public class FailAction extends AbstractAction {
        public FailAction() {
            type = Type.EXECUTE;
        }

        public FailAction(Type type) {
            this.type = type;
        }

        @Override
        public void execute(STContext context, Resource resource) throws STRuntimeException {
            throw new STRuntimeException();
        }

        @Override
        public boolean checkExecutionRequired(STContext context, Resource resource) throws STRuntimeException {
            return true;
        }
    }

}
