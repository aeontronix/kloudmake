/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.systyrant;

import com.kloudtek.systyrant.exception.InjectException;
import com.kloudtek.systyrant.exception.InvalidResourceDefinitionException;
import com.kloudtek.systyrant.exception.InvalidServiceException;
import com.kloudtek.systyrant.exception.STRuntimeException;
import org.testng.annotations.Test;

import javax.script.ScriptException;
import java.io.IOException;

import static org.testng.Assert.assertTrue;

public class E2ETests {
    @Test(groups = "vagrant")
    public void createVagrantTestServer() throws IOException, ScriptException, STRuntimeException, InvalidResourceDefinitionException, InvalidServiceException, InjectException {
        STContext ctx = new STContext();
        ctx.runScript(getClass().getResource("vagrant.stl"));
        VagrantValidationResource vvr = VagrantValidationResource.find(ctx);
        assertTrue(ctx.execute());
        assertTrue(vvr.isValidated());
    }
}
