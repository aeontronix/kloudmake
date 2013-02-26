/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.systyrant.resource.host;

import com.jcraft.jsch.JSchException;
import org.testng.annotations.Factory;

import java.io.IOException;

public class SshHostTests {
    @Factory
    public Object[] createInstances() throws IOException, JSchException {
        return new Object[]{
                new HostTests(HostTests.Type.SSH_ROOT),
                new HostTests(HostTests.Type.SSH_SUDO)
        };
    }
}
