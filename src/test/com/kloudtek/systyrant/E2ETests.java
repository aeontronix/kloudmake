/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.systyrant;

import com.kloudtek.systyrant.exception.InvalidResourceDefinitionException;
import com.kloudtek.systyrant.exception.InvalidServiceException;
import com.kloudtek.systyrant.exception.STRuntimeException;
import org.testng.Assert;
import org.testng.annotations.Test;

import javax.script.ScriptException;
import java.io.IOException;

public class E2ETests {
    @Test(groups = "vagrant")
    public void createVagrantTestServer() throws IOException, ScriptException, STRuntimeException, InvalidResourceDefinitionException, InvalidServiceException {
        STContext ctx = new STContext();
        ctx.runScript(getClass().getResource("vagrant.stl"));
        Assert.assertTrue(ctx.execute());
    }
}
