/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.systyrant;

import com.jcraft.jsch.JSchException;
import com.kloudtek.systyrant.exception.InvalidResourceDefinitionException;
import com.kloudtek.systyrant.exception.InvalidServiceException;
import com.kloudtek.systyrant.exception.STRuntimeException;
import com.kloudtek.systyrant.service.host.SshHost;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import java.io.IOException;

import static org.testng.Assert.assertTrue;

public abstract class AbstractSSHTest {
    protected STContext ctx;
    protected SshHost host;

    @BeforeMethod(groups = "ssh")
    public void setup() throws JSchException, InvalidResourceDefinitionException, InvalidServiceException {
        host = SshHost.createSSHAdminForVagrantInstance("localhost", 2222, "vagrant");
        ctx = new STContext();
        ctx.getServiceManager().addOverride("host", host);
    }

    @AfterMethod(groups = "ssh")
    public void clean() throws IOException {
        ctx.close();
    }

    protected void execute() throws STRuntimeException {
        assertTrue(ctx.execute());
    }
}
