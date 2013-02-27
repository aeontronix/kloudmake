/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.systyrant.core;

import com.kloudtek.systyrant.AbstractVagrantTest;
import com.kloudtek.systyrant.exception.STRuntimeException;
import com.kloudtek.systyrant.service.host.SshHost;
import org.testng.Assert;
import org.testng.annotations.Test;

import javax.script.ScriptException;
import java.io.IOException;

public class FileResourceVagrantTest extends AbstractVagrantTest {
    @Test(groups = "vagrant")
    public void testCreateNonExistingFile() throws STRuntimeException, IOException, ScriptException {
        host.exec("rm -f /root/testfile");
        ctx.runDSLScript("new core:file { path='/root/testfile', content='hello', owner='uucp', group='fuse' permissions='rwxr-xrw-'}");
        SshHost sshHost = execute();
        String stats = sshHost.exec("stat -c '%F:%s:%A:%U:%G' /root/testfile");
        sshHost.stop();
        Assert.assertEquals(stats, "regular file:5:-rwxr-xrw-:uucp:fuse\n");
    }
}
