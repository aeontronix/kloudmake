/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.systyrant.host;

import com.kloudtek.systyrant.exception.STRuntimeException;
import com.kloudtek.util.StringUtils;
import org.apache.commons.exec.CommandLine;

public abstract class UnixAbstractMetadataProvider extends AbstractHostProvider {
    protected OperatingSystem os;

    protected UnixAbstractMetadataProvider(OperatingSystem os) {
        this.os = os;
        attrs.put("os", os.name().toLowerCase());
    }

    @Override
    public boolean isAbsolutePath(String workdir) {
        return workdir.startsWith("/");
    }

    @Override
    public String getWorkingDir(Host host) throws STRuntimeException {
        return host.exec("pwd") + "/";
    }

    @Override
    public OperatingSystem getOperatingSystem() {
        return os;
    }

    @Override
    public CommandLine generateCommandLine(String command, String currentUser, String user, boolean handleQuoting, String workdir) {
        CommandLine cmd;
        if( ! user.equalsIgnoreCase(currentUser) ) {
            cmd = new CommandLine("sudo").addArgument("-u").addArgument(user).addArgument("bash");
        } else {
            cmd = new CommandLine("bash");
        }
        if( StringUtils.isNotEmpty(workdir) ) {
            command = "cd "+workdir+" && "+command;
        }
        if( handleQuoting ) {
            return cmd.addArgument("-c").addArgument("\""+command+"\"",false);
        } else {
            return cmd.addArgument("-c").addArgument(command,false);
        }
    }
}
