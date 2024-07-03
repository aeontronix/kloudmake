/*
 * Copyright (c) 2024 Aeontronix Inc
 */

package com.aeontronix.aeonbuild.host;

import com.aeontronix.aeonbuild.exception.KMRuntimeException;
import org.apache.commons.exec.CommandLine;

import java.util.HashMap;

public interface HostProvider {
    boolean supports(Host host, HashMap<String, Object> datacache) throws KMRuntimeException;

    OperatingSystem getOperatingSystem();

    CommandLine generateCommandLine(String command, String currentUser, String user, boolean handleQuoting, String workdir);

    boolean isAbsolutePath(String workdir);

    String getWorkingDir(Host abstractHost) throws KMRuntimeException;
}
