/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.systyrant;

import com.kloudtek.systyrant.annotation.Execute;
import com.kloudtek.systyrant.annotation.STResource;
import com.kloudtek.systyrant.resource.JavaResourceFactory;
import com.kloudtek.systyrant.service.host.Host;
import com.kloudtek.systyrant.service.host.metadata.OperatingSystem;

import javax.annotation.Resource;

import static org.testng.Assert.assertEquals;

@STResource("test:vagrantvalidate")
public class VagrantValidationResource {
    @Resource
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
        for (com.kloudtek.systyrant.resource.Resource resource : ctx.getResourceManager()) {
            if (resource.getFQName().equals(new FQName("test", "vagrantvalidate"))) {
                for (STAction action : resource.getActions()) {
                    if (action instanceof JavaResourceFactory.JavaImpl) {
                        return (VagrantValidationResource) ((JavaResourceFactory.JavaImpl) action).getImpl();
                    }
                }
            }
        }
        return null;
    }
}
