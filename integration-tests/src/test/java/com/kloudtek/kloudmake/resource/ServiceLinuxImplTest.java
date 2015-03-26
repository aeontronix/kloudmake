/*
 * Copyright (c) 2015. Kelewan Technologies Ltd
 */

package com.kloudtek.kloudmake.resource;

import com.kloudtek.kloudmake.AbstractVagrantTest;
import com.kloudtek.kloudmake.Resource;
import com.kloudtek.kloudmake.exception.InvalidQueryException;
import com.kloudtek.kloudmake.exception.InvalidResourceDefinitionException;
import com.kloudtek.kloudmake.exception.KMRuntimeException;
import com.kloudtek.kloudmake.host.ExecutionResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;

import static org.testng.Assert.assertTrue;

/**
 * Tests {@link com.kloudtek.kloudmake.resource.core.ServiceLinuxImpl}
 */
public class ServiceLinuxImplTest extends AbstractVagrantTest {
    @BeforeClass(groups = "vagrant")
    public void apt() throws KMRuntimeException, IOException, InvalidResourceDefinitionException {
        super.init();
        sshHost.exec("apt-get update");
        sshHost.exec("apt-get install -y nginx");
    }

    @BeforeMethod(groups = "vagrant")
    public void init() throws KMRuntimeException, IOException, InvalidResourceDefinitionException {
        super.init();
    }

    @Test(groups = "vagrant")
    public void testStartInitdService() throws KMRuntimeException, InvalidQueryException {
        sshHost.exec("/etc/init.d/nginx stop");
        Resource resource = resourceManager.createResource("core.service").set("name", "nginx");
        Assert.assertEquals(ctx.findResources("type core.service").size(), 1);
        execute();
        Assert.assertEquals(ctx.findResources("type core.service").size(), 1);
        sshHost.exec("/etc/init.d/nginx status");
    }

    @Test(groups = "vagrant")
    public void testStopInitdService() throws KMRuntimeException, InvalidQueryException {
        sshHost.exec("/etc/init.d/nginx start");
        sshHost.exec("/etc/init.d/nginx status");
        Resource resource = resourceManager.createResource("core.service").set("name", "nginx").set("running", false);
        Assert.assertEquals(ctx.findResources("type core.service").size(), 1);
        execute();
        Assert.assertEquals(ctx.findResources("type core.service").size(), 1);
        ExecutionResult exec = sshHost.exec("/etc/init.d/nginx status", null, null);
        assertTrue(exec.getRetCode() != 0);
    }

    @Test(groups = "vagrant")
    public void testDisabledAutoStartInitdService() throws KMRuntimeException, InvalidQueryException {
        sshHost.exec("update-rc.d nginx enable");
        Resource resource = resourceManager.createResource("core.service").set("name", "nginx")
                .set("running", true).set("autostart", "false");
        execute();
        assertTrue(sshHost.fileExists("/etc/rc2.d/K80nginx"));
    }

    @Test(groups = "vagrant")
    public void testEnabledAutoStartInitdService() throws KMRuntimeException, InvalidQueryException {
        sshHost.exec("update-rc.d nginx disable");
        Resource resource = resourceManager.createResource("core.service").set("name", "nginx")
                .set("running", true).set("autostart", "true");
        execute();
        assertTrue(sshHost.fileExists("/etc/rc2.d/S20nginx"));
    }
}
