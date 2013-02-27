/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.systyrant;

import com.kloudtek.systyrant.exception.*;
import com.kloudtek.systyrant.resource.Resource;
import com.kloudtek.systyrant.resource.ResourceManager;
import com.kloudtek.systyrant.resource.builtin.virt.VagrantResource;
import com.kloudtek.systyrant.service.host.Host;
import com.kloudtek.systyrant.service.host.LocalHost;
import com.kloudtek.systyrant.service.host.SshHost;
import org.testng.annotations.BeforeMethod;

import static org.testng.Assert.assertEquals;

public class AbstractVagrantTest {
    public static final String TEST = "test:test";
    public static final String UNIQUETEST = "test:uniquetest";
    public static final String VAGRANTDIR = "_vagrant";
    protected STContext ctx;
    protected ResourceManager resourceManager;
    protected Host host;

    @BeforeMethod
    public void init() throws STRuntimeException, InvalidResourceDefinitionException, InvalidServiceException, ResourceCreationException {
        ctx = new STContext();
        host = (Host) ctx.getServiceManager().getService("host");
        resourceManager = ctx.getResourceManager();
        resourceManager.registerJavaResource(TestResource.class, TEST);
        resourceManager.registerJavaResource(UniqueTestResource.class, UNIQUETEST);
        Resource vagrant = resourceManager.createResource("virt:vagrant");
        vagrant.set("dir", VAGRANTDIR);
        vagrant.set("box", "ubuntu-precise64");
        ctx.setDefaultParent(vagrant);
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

    public SshHost execute() throws STRuntimeException {
        return execute(true);
    }

    public SshHost execute(boolean expected) throws STRuntimeException {
        assertEquals(ctx.execute(), expected);
        SshHost sshHost = VagrantResource.createSshHost(new LocalHost(), VAGRANTDIR);
        sshHost.start();
        return sshHost;
    }
}
