/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.systyrant.resource.builtin.core;

import com.kloudtek.systyrant.ExecutionResult;
import com.kloudtek.systyrant.annotation.Attr;
import com.kloudtek.systyrant.annotation.STResource;
import com.kloudtek.systyrant.annotation.Sync;
import com.kloudtek.systyrant.annotation.Verify;
import com.kloudtek.systyrant.exception.STRuntimeException;
import com.kloudtek.systyrant.service.host.Host;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;

import static com.kloudtek.util.StringUtils.isEmpty;

@STResource
public class ExecResource {
    private static Logger logger = LoggerFactory.getLogger(ExecResource.class);
    @Resource
    private Host host;
    @Attr
    private String command;
    @Attr
    private String user;
    @Attr("if")
    private String ifAttr;
    @Attr
    private String unless;
    @Attr
    private String logging;
    @Attr
    private Long timeout;
    private Host.Logging loggingEnum;

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    @Verify
    public boolean testIfAndUnless() throws STRuntimeException {
        try {
            loggingEnum = isEmpty(logging) ? Host.Logging.ON_ERROR : Host.Logging.valueOf(logging.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new STRuntimeException("Invalid logging attribute: " + logging);
        }
        if (unless != null) {
            ExecutionResult exec = host.exec(unless, null, loggingEnum);
            if (exec.getErrCode() == 0) {
                return true;
            }
        }
        if (ifAttr != null) {
            ExecutionResult exec = host.exec(ifAttr, null, loggingEnum);
            if (exec.getErrCode() != 0) {
                return true;
            }
        }
        return false;
    }

    @Sync
    public void exec() throws STRuntimeException {
        host.exec(command, timeout, null, loggingEnum, false);
    }
}
