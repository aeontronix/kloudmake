/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.systyrant;

import com.kloudtek.systyrant.exception.*;
import com.kloudtek.systyrant.resource.Resource;
import com.kloudtek.systyrant.resource.ResourceManager;
import org.testng.annotations.BeforeMethod;

import static org.testng.Assert.assertEquals;

public class AbstractContextTest {
    public static final String TEST = "test:test";
    public static final String UNIQUETEST = "test:uniquetest";
    protected STContext ctx;
    protected ResourceManager resourceManager;

    @BeforeMethod
    public void init() throws STRuntimeException, InvalidResourceDefinitionException, InvalidServiceException {
        ctx = new STContext();
        resourceManager = ctx.getResourceManager();
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

    public void execute() throws STRuntimeException {
        execute(true);
    }

    public void execute(boolean expected) throws STRuntimeException {
        assertEquals(ctx.execute(), expected);
    }

    public void enableVagrant() throws InvalidAttributeException, ResourceCreationException {
        Resource vagrant = resourceManager.createResource("virt:vagrant");
        vagrant.set("dir", "_vagrant");
        vagrant.set("box", "ubuntu-precise64");
        ctx.setDefaultParent(vagrant);
    }
}
