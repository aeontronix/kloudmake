/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.systyrant.java;

import com.kloudtek.systyrant.Resource;
import com.kloudtek.systyrant.STContext;
import com.kloudtek.systyrant.exception.STRuntimeException;
import com.kloudtek.systyrant.host.OperatingSystem;

/**
 * Class that implements @OnlyIfOS annotation
 */
public class EnforceOnlyIfOS extends EnforceOnlyIf {
    private OperatingSystem[] operatingSystems;

    public EnforceOnlyIfOS(OperatingSystem[] operatingSystems) {
        this.operatingSystems = operatingSystems;
    }

    @Override
    public boolean execAllowed(STContext context, Resource resource) throws STRuntimeException {
        OperatingSystem hostOS = resource.getHost().getMetadata().getOperatingSystem();
        for (OperatingSystem os : operatingSystems) {
            if (!hostOS.equals(os)) {
                return false;
            }
        }
        return true;
    }
}
