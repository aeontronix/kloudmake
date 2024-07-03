/*
 * Copyright (c) 2024 Aeontronix Inc
 */

package com.aeontronix.aeonbuild;

import com.aeontronix.aeonbuild.annotation.Execute;
import com.aeontronix.aeonbuild.annotation.KMResource;
import com.aeontronix.aeonbuild.annotation.Service;
import com.aeontronix.aeonbuild.exception.KMRuntimeException;
import com.aeontronix.aeonbuild.host.Host;
import com.aeontronix.aeonbuild.host.OperatingSystem;

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

    public static VagrantValidationResource find(BuildContextImpl ctx) {
        for (Resource resource : ctx.getResourceManager()) {
            if (resource.getType().equals(new FQName("test", "vagrantvalidate"))) {
                return resource.getJavaImpl(VagrantValidationResource.class);
            }
        }
        return null;
    }
}
