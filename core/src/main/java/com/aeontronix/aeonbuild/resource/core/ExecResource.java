/*
 * Copyright (c) 2024 Aeontronix Inc
 */

package com.aeontronix.aeonbuild.resource.core;

import com.aeontronix.aeonbuild.annotation.*;
import com.aeontronix.aeonbuild.exception.KMRuntimeException;
import com.aeontronix.aeonbuild.annotation.*;
import com.aeontronix.aeonbuild.host.ExecutionResult;
import com.aeontronix.aeonbuild.host.Host;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@KMResource
public class ExecResource {
    private static Logger logger = LoggerFactory.getLogger(ExecResource.class);
    @Inject
    private Host host;
    @Attr
    private String command;
    @Attr
    private String user;
    @Attr("if")
    private String ifAttr;
    @Attr
    private String unless;
    @Attr(def = "ON_ERROR")
    private String logging;
    @Attr(def = "60")
    private Long timeout;
    @Attr(def = "0")
    private Long returns;
    private Host.Logging loggingEnum;

    @Verify
    public boolean testIfAndUnless() throws KMRuntimeException {
        try {
            loggingEnum = Host.Logging.valueOf(logging.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new KMRuntimeException("Invalid logging attribute: " + logging);
        }
        if (unless != null) {
            ExecutionResult exec = host.exec(unless, null, loggingEnum);
            if (exec.getRetCode() == 0) {
                return false;
            }
        }
        if (ifAttr != null) {
            ExecutionResult exec = host.exec(ifAttr, null, loggingEnum);
            if (exec.getRetCode() != 0) {
                return false;
            }
        }
        return true;
    }

    @Sync
    public void exec() throws KMRuntimeException {
        ExecutionResult exec = host.exec(command, timeout * 1000, null, loggingEnum, null);
        if (exec.getRetCode() != returns) {
            throw new KMRuntimeException("Execution of " + command + " returned " + exec.getRetCode());
        }
    }
}
