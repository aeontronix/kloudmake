/*
 * Copyright (c) 2015. Kelewan Technologies Ltd
 */

package com.kloudtek.kloudmake.resource.core;

import com.kloudtek.kloudmake.annotation.*;
import com.kloudtek.kloudmake.exception.KMRuntimeException;
import com.kloudtek.kloudmake.host.Host;
import com.kloudtek.util.StringUtils;
import org.hibernate.validator.constraints.NotEmpty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.kloudtek.kloudmake.host.OperatingSystem.LINUX;

/**
 * This resource is used to install a package on a linux system.
 */
@STResource("core.service")
@OnlyIfOperatingSystem(LINUX)
public class ServiceLinuxImpl {
    private static final Logger logger = LoggerFactory.getLogger(ServiceLinuxImpl.class);
    @NotEmpty()
    @Attr
    private String name;
    @Attr
    private Type type;
    @Attr(def = "true")
    private boolean running;
    @Attr(def = "true")
    private boolean autostart;
    @Inject
    private Host host;

    public ServiceLinuxImpl() {
    }

    public ServiceLinuxImpl(String name, Type type, boolean running, boolean autostart) {
        this.name = name;
        this.type = type;
        this.running = running;
        this.autostart = autostart;
    }

    @Execute(order = 2)
    public void findType() throws KMRuntimeException {
        if (type == null) {
            if (host.fileExists("/etc/init.d/" + name)) {
                type = Type.INITD;
            } else {
                throw new KMRuntimeException("Unable to find linux service script for " + name);
            }
        }
    }

    @Verify
    public boolean checkEnabled() throws KMRuntimeException {
        boolean status = host.exec("ls -1 /etc/rc2.d | grep '^S[0-9]*" + name + "$'", null, null).getRetCode() == 0;
        return autostart != status;
    }

    @Sync(order = 1)
    public void setEnabled() throws KMRuntimeException {
        String cmd = autostart ? "enable" : "disable";
        host.exec("update-rc.d " + name + " " + cmd);
        logger.info(StringUtils.capitalize(cmd) + "d service " + name);
    }

    @Verify("running")
    public boolean checkRunning() throws KMRuntimeException {
        switch (type) {
            case INITD:
                boolean status = host.exec("/etc/init.d/" + name + " status", null, null).getRetCode() == 0;
                return running != status;
            default:
                throw new KMRuntimeException("BUG: Unknown service type " + type);
        }
    }

    @Sync("running")
    public void setRunning() throws KMRuntimeException {
        switch (type) {
            case INITD:
                host.exec("/etc/init.d/" + name + " " + (running ? "start" : "stop"));
                break;
            default:
                throw new KMRuntimeException("BUG: Unknown service type " + type);
        }
        logger.info((running ? "Started" : "Stopped") + " service " + name);
    }

    public enum Type {
        INITD
    }
}
