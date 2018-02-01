/*
 * Copyright (c) 2015. Kelewan Technologies Ltd
 */

package com.kloudtek.kloudmake.java;

import com.kloudtek.kloudmake.KMContextImpl;
import com.kloudtek.kloudmake.Resource;
import com.kloudtek.kloudmake.exception.KMRuntimeException;
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
    public boolean execAllowed(KMContextImpl context, Resource resource) throws KMRuntimeException {
        OperatingSystem hostOS = resource.getHost().getMetadata().getOperatingSystem();
        for (OperatingSystem os : operatingSystems) {
            if (!hostOS.equals(os)) {
                return false;
            }
        }
        return true;
    }
}
