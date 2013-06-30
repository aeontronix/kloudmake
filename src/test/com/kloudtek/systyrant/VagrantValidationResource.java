/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.systyrant;

import com.kloudtek.systyrant.annotation.Execute;
import com.kloudtek.systyrant.annotation.STResource;
import com.kloudtek.systyrant.annotation.Service;
import com.kloudtek.systyrant.host.Host;
import com.kloudtek.systyrant.host.OperatingSystem;

import static org.testng.Assert.assertEquals;

@STResource("test.vagrantvalidate")
public class VagrantValidationResource {
    @Service
    private Host host;
    private boolean validated;

    @Execute
    public void validate() {
        assertEquals(host.getMetadata().getOperatingSystem(), OperatingSystem.LINUX);
        validated = true;
    }

    public boolean isValidated() {
        return validated;
    }

    public static VagrantValidationResource find(STContext ctx) {
        for (Resource resource : ctx.getResourceManager()) {
            if (resource.getType().equals(new FQName("test", "vagrantvalidate"))) {
                return resource.getJavaImpl(VagrantValidationResource.class);
            }
        }
        return null;
    }
}
