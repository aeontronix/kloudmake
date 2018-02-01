/*
 * Copyright (c) 2015. Kelewan Technologies Ltd
 */

package com.kloudtek.kloudmake.host;

import com.kloudtek.kloudmake.annotation.Provider;
import com.kloudtek.kloudmake.exception.KMRuntimeException;

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
