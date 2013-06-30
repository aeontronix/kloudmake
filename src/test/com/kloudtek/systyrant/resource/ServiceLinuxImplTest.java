/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.systyrant.resource;

import com.kloudtek.systyrant.AbstractVagrantTest;
import com.kloudtek.systyrant.Resource;
import com.kloudtek.systyrant.exception.InvalidQueryException;
import com.kloudtek.systyrant.exception.InvalidResourceDefinitionException;
import com.kloudtek.systyrant.exception.STRuntimeException;
import com.kloudtek.systyrant.host.ExecutionResult;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Tests {@link com.kloudtek.systyrant.resource.core.ServiceLinuxImpl}
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

    @Test(groups = "vagrant")
    public void testStartInitdService() throws STRuntimeException, InvalidQueryException {
        sshHost.exec("/etc/init.d/nginx stop");
        Resource resource = resourceManager.createResource("core.service").set("name", "nginx");
        assertEquals(ctx.findResources("type core.service").size(), 1);
        execute();
        assertEquals(ctx.findResources("type core.service").size(), 1);
        sshHost.exec("/etc/init.d/nginx status");
    }

    @Test(groups = "vagrant")
    public void testStopInitdService() throws STRuntimeException, InvalidQueryException {
        sshHost.exec("/etc/init.d/nginx start");
        sshHost.exec("/etc/init.d/nginx status");
        Resource resource = resourceManager.createResource("core.service").set("name", "nginx").set("running", false);
        assertEquals(ctx.findResources("type core.service").size(), 1);
        execute();
        assertEquals(ctx.findResources("type core.service").size(), 1);
        ExecutionResult exec = sshHost.exec("/etc/init.d/nginx status", null, null);
        assertTrue(exec.getRetCode() != 0);
    }

    @Test(groups = "vagrant")
    public void testDisabledAutoStartInitdService() throws STRuntimeException, InvalidQueryException {
        sshHost.exec("update-rc.d nginx enable");
        Resource resource = resourceManager.createResource("core.service").set("name", "nginx")
                .set("running", true).set("autostart", "false");
        execute();
        assertTrue(sshHost.fileExists("/etc/rc2.d/K80nginx"));
    }

    @Test(groups = "vagrant")
    public void testEnabledAutoStartInitdService() throws STRuntimeException, InvalidQueryException {
        sshHost.exec("update-rc.d nginx disable");
        Resource resource = resourceManager.createResource("core.service").set("name", "nginx")
                .set("running", true).set("autostart", "true");
        execute();
        assertTrue(sshHost.fileExists("/etc/rc2.d/S20nginx"));
    }
}
