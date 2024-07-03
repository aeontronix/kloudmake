/*
 * Copyright (c) 2024 Aeontronix Inc
 */

package com.aeontronix.aeonbuild.java;

import com.aeontronix.aeonbuild.exception.KMRuntimeException;
import com.aeontronix.aeonbuild.host.OperatingSystem;
import com.aeontronix.aeonbuild.BuildContextImpl;
import com.aeontronix.aeonbuild.Resource;

/**
 * Class that implements @OnlyIfOS annotation
 */
public class EnforceOnlyIfOS extends EnforceOnlyIf {
    private OperatingSystem[] operatingSystems;

    public EnforceOnlyIfOS(OperatingSystem[] operatingSystems) {
        this.operatingSystems = operatingSystems;
    }

    @Override
    public boolean execAllowed(BuildContextImpl context, Resource resource) throws KMRuntimeException {
        OperatingSystem hostOS = resource.getHost().getMetadata().getOperatingSystem();
        for (OperatingSystem os : operatingSystems) {
            if (!hostOS.equals(os)) {
                return false;
            }
        }
        return true;
    }
}
