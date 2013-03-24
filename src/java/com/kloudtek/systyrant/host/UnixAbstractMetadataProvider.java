/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.systyrant.host;

import com.kloudtek.systyrant.exception.STRuntimeException;
import com.kloudtek.util.StringUtils;
import org.apache.commons.exec.CommandLine;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
    public CommandLine generateCommandLine(@NotNull String command, @NotNull String currentUser, @NotNull String user,
                                           boolean handleQuoting, @Nullable String workdir) {
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
