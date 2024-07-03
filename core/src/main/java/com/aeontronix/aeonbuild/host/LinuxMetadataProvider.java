/*
 * Copyright (c) 2024 Aeontronix Inc
 */

package com.aeontronix.aeonbuild.host;

import com.aeontronix.aeonbuild.exception.KMRuntimeException;
import com.aeontronix.aeonbuild.annotation.Provider;

import java.util.HashMap;

@Provider
public class LinuxMetadataProvider extends UnixAbstractMetadataProvider {
    public LinuxMetadataProvider() {
        super(OperatingSystem.LINUX);
    }

    @Override
    public boolean supports(Host host, HashMap<String, Object> datacache) throws KMRuntimeException {
        ExecutionResult res = host.exec("uname", null, Host.Logging.ON_ERROR);
        if (res.getRetCode() == 0) {
            if (res.getOutput().trim().equals("Linux")) {
                return true;
            }
        }
        return false;
    }
}
