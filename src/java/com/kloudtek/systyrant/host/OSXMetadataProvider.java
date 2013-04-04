/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.systyrant.host;

import com.kloudtek.systyrant.ExecutionResult;
import com.kloudtek.systyrant.annotation.Provider;
import com.kloudtek.systyrant.exception.STRuntimeException;

import java.util.HashMap;

@Provider
public class OSXMetadataProvider extends UnixAbstractMetadataProvider {
    public OSXMetadataProvider() {
        super(OperatingSystem.OSX);
    }

    @Override
    public boolean supports(Host host, HashMap<String, Object> datacache) throws STRuntimeException {
        ExecutionResult res = host.exec("uname", null, Host.Logging.ON_ERROR);
        if (res.getRetCode() == 0) {
            if (res.getOutput().trim().equals("Darwin")) {
                return true;
            }
        }
        return false;
    }
}
