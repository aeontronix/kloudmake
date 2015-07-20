/*
 * Copyright (c) 2015. Kelewan Technologies Ltd
 */

package com.kloudtek.kloudmake;

import com.kloudtek.kloudmake.annotation.Execute;
import com.kloudtek.kloudmake.annotation.KMResource;
import com.kloudtek.kloudmake.annotation.Service;
import com.kloudtek.kloudmake.exception.KMRuntimeException;
import com.kloudtek.kloudmake.host.Host;
import com.kloudtek.kloudmake.host.OperatingSystem;

import static org.testng.Assert.assertEquals;

@KMResource("test.vagrantvalidate")
public class VagrantValidationResource {
    @Service
    private Host host;
    private boolean validated;

    @Execute
    public void validate() throws KMRuntimeException {
        assertEquals(host.getMetadata().getOperatingSystem(), OperatingSystem.LINUX);
        validated = true;
    }

    public boolean isValidated() {
        return validated;
    }

    public static VagrantValidationResource find(KMContextImpl ctx) {
        for (Resource resource : ctx.getResourceManager()) {
            if (resource.getType().equals(new FQName("test", "vagrantvalidate"))) {
                return resource.getJavaImpl(VagrantValidationResource.class);
            }
        }
        return null;
    }
}
