/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.systyrant.service.host;

import com.kloudtek.systyrant.exception.STRuntimeException;
import org.apache.commons.exec.CommandLine;

import java.util.HashMap;

public interface HostProvider {
    boolean supports(Host host, HashMap<String, Object> datacache) throws STRuntimeException;

    OperatingSystem getOperatingSystem();

    String getExecutionPrefix(String currentUser, String user);

    String getExecutionSuffix(String currentUser, String user);

    CommandLine generateCommandLine(String command, String currentUser, String user, boolean handleQuoting);
}
