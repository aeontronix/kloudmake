/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.systyrant.resource.builtin.core;

import com.kloudtek.systyrant.AbstractVagrantTest;
import com.kloudtek.systyrant.ExecutionResult;
import com.kloudtek.systyrant.exception.InvalidQueryException;
import com.kloudtek.systyrant.exception.InvalidResourceDefinitionException;
import com.kloudtek.systyrant.exception.STRuntimeException;
import com.kloudtek.systyrant.resource.Resource;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;

/**
 * Tests {@link ServiceLinuxImpl}
 */
public class ServiceLinuxImplTest extends AbstractVagrantTest {
    @BeforeClass(groups = "vagrant")
    public void apt() throws STRuntimeException, IOException, InvalidResourceDefinitionException {
        super.init();
        sshHost.exec("apt-get update");
        sshHost.exec("apt-get install -y nginx");
    }

    @BeforeMethod(groups = "vagrant")
    public void init() throws STRuntimeException, IOException, InvalidResourceDefinitionException {
        super.init();
    }

    @Test
    public void testStartInitdService() throws STRuntimeException, InvalidQueryException {
        sshHost.exec("/etc/init.d/nginx stop");
        Resource resource = resourceManager.createResource("core:service").set("name", "nginx");
        Assert.assertEquals(ctx.findResources("type core:service").size(), 1);
        execute();
        Assert.assertEquals(ctx.findResources("type core:service").size(), 1);
        sshHost.exec("/etc/init.d/nginx status");
    }

    @Test
    public void testStopInitdScanervice() throws STRuntimeException, InvalidQueryException {
        sshHost.exec("/etc/init.d/nginx start");
        sshHost.exec("/etc/init.d/nginx status");
        Resource resource = resourceManager.createResource("core:service").set("name", "nginx").set("running", false);
        Assert.assertEquals(ctx.findResources("type core:service").size(), 1);
        execute();
        Assert.assertEquals(ctx.findResources("type core:service").size(), 1);
        ExecutionResult exec = sshHost.exec("/etc/init.d/nginx status", null, null);
        Assert.assertTrue(exec.getRetCode() != 0);
    }
}
