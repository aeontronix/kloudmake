/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.kloudmake.host;

import com.kloudtek.kloudmake.exception.STRuntimeException;
import org.apache.commons.exec.CommandLine;

import java.util.HashMap;

public interface HostProvider {
    boolean supports(Host host, HashMap<String, Object> datacache) throws STRuntimeException;

    OperatingSystem getOperatingSystem();

    CommandLine generateCommandLine(String command, String currentUser, String user, boolean handleQuoting, String workdir);

    boolean isAbsolutePath(String workdir);

    String getWorkingDir(Host abstractHost) throws STRuntimeException;
}
