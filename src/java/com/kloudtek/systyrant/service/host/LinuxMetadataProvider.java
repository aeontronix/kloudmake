/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.systyrant.service.host;

import com.kloudtek.systyrant.ExecutionResult;
import com.kloudtek.systyrant.annotation.Provider;
import com.kloudtek.systyrant.exception.STRuntimeException;
import org.apache.commons.exec.CommandLine;

import java.util.HashMap;

@Provider
public class LinuxMetadataProvider extends AbstractHostProvider {
    public LinuxMetadataProvider() {
        attrs.put("os", OperatingSystem.LINUX.name().toLowerCase());
    }

    @Override
    public boolean supports(Host host, HashMap<String, Object> datacache) throws STRuntimeException {
        ExecutionResult res = host.exec("uname", null, Host.Logging.ON_ERROR);
        if (res.getRetCode() == 0) {
            if (res.getOutput().trim().equals("Linux")) {
                return true;
            }
        }
        return false;
    }

    @Override
    public OperatingSystem getOperatingSystem() {
        return OperatingSystem.LINUX;
    }

    @Override
    public String getExecutionPrefix(String currentUser, String user) {
        return super.getUnixExecutionPrefix(currentUser, user);
    }

    @Override
    public String getExecutionSuffix(String currentUser, String user) {
        return getUnixExecutionSuffix(currentUser, user);
    }

    @Override
    public CommandLine generateCommandLine(String command, String currentUser, String user) {
        return new CommandLine("bash").addArgument("-c").addArgument(command, false);
    }
}
