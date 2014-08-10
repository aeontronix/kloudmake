/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.kloudmake.java;

import com.kloudtek.kloudmake.Resource;
import com.kloudtek.kloudmake.STContext;
import com.kloudtek.kloudmake.exception.STRuntimeException;
import com.kloudtek.kloudmake.host.OperatingSystem;

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
