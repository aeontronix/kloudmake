/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.systyrant.service.host;

import org.apache.commons.exec.CommandLine;

import java.io.File;

public class ExtendedCommandLine extends CommandLine {
    private String prefix;
    private String suffix;

    public ExtendedCommandLine(String executable) {
        super(executable);
    }

    public ExtendedCommandLine(File executable) {
        super(executable);
    }

    public ExtendedCommandLine(CommandLine other) {
        super(other);
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    public static ExtendedCommandLine parse(String arg, boolean includePSFixes, String prefix, String suffix) {
        CommandLine cmd = CommandLine.parse(arg);
        ExtendedCommandLine xcmd = new ExtendedCommandLine(cmd);
        if (includePSFixes) {
            xcmd.setPrefix(prefix);
            xcmd.setSuffix(suffix);
        } else {
            xcmd.setPrefix("");
            xcmd.setSuffix("");
        }
        return xcmd;
    }
}
