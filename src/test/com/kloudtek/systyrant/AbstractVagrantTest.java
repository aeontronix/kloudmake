/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.systyrant;

import com.kloudtek.systyrant.exception.*;
import com.kloudtek.systyrant.host.Host;
import com.kloudtek.systyrant.host.LocalHost;
import com.kloudtek.systyrant.host.SshHost;
import com.kloudtek.systyrant.resource.builtin.vagrant.SharedFolder;
import com.kloudtek.systyrant.resource.builtin.vagrant.VagrantResource;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.Executor;
import org.apache.commons.exec.PumpStreamHandler;

import java.io.File;
import java.io.IOException;

import static org.testng.Assert.assertEquals;

public class AbstractVagrantTest {
    public static final String TEST = "test:test";
    public static final String UNIQUETEST = "test:uniquetest";
    public static final String VAGRANTDIR = "_vagrant";
    public static final String TESTDIR = VAGRANTDIR + File.separator + "_vagrant";
    protected STContext ctx;
    protected ResourceManager resourceManager;
    protected Host host;
    protected SshHost sshHost;

    public void init() throws STRuntimeException, InvalidResourceDefinitionException, InvalidServiceException, ResourceCreationException, InjectException, IOException {
        ctx = new STContext();
        host = ctx.getHost();
        Executor exec = new DefaultExecutor();
        exec.setWorkingDirectory(new File(VAGRANTDIR));
        exec.setStreamHandler(new PumpStreamHandler(System.out));
        exec.execute(CommandLine.parse("vagrant up"));
        resourceManager = ctx.getResourceManager();
        resourceManager.registerJavaResource(TestResource.class, TEST);
        resourceManager.registerJavaResource(UniqueTestResource.class, UNIQUETEST);
        Resource vagrant = resourceManager.createResource("vagrant:vagrant");
        vagrant.set("dir", VAGRANTDIR);
        vagrant.set("box", "ubuntu-precise64");
        SharedFolder testFolder = new SharedFolder(true, true, "test", TESTDIR, "/test");
        Resource testDirRes = resourceManager.createResource(testFolder);
        ctx.setDefaultParent(vagrant);
        sshHost = VagrantResource.createSshHost(new LocalHost(), VAGRANTDIR);
        ctx.inject(sshHost);
//        try {
//            Field field = AbstractHost.class.getDeclaredField("hostProviderManager");
//            field.setAccessible(true);
//            field.set(sshHost, ctx.getProvidersManagementService().getProviderManager(HostProviderManager.class));
//        } catch (NoSuchFieldException | IllegalAccessException e) {
//            throw new STRuntimeException(e.getMessage(), e);
//        }
        sshHost.start();
    }

    public Resource createTestResource() throws ResourceCreationException {
        return resourceManager.createResource(TEST);
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
        return resourceManager.createResource(TEST, id, parent);
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
}
