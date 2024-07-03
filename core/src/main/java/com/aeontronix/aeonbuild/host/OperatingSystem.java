/*
 * Copyright (c) 2024 Aeontronix Inc
 */

package com.aeontronix.aeonbuild.host;

/**
 * Created with IntelliJ IDEA.
 * User: ymenager
 * Date: 03/03/2013
 * Time: 23:08
 * To change this template use File | Settings | File Templates.
 */
public enum OperatingSystem {
    LINUX, OSX, SOLARIS, AIX, WINDOWS, BSD, HPUX, UNKNOWN;

    public static OperatingSystem getSystemOS() {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.startsWith("windows")) {
            return WINDOWS;
        } else if (os.startsWith("linux")) {
            return LINUX;
        } else if (os.startsWith("mac")) {
            return OSX;
        } else if (os.startsWith("freebsd")) {
            return BSD;
        } else if (os.startsWith("sunos")) {
            return SOLARIS;
        } else {
            return UNKNOWN;
        }
    }
}
