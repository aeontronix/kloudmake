/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.systyrant.resource.builtin.core;

import com.kloudtek.systyrant.annotation.*;
import com.kloudtek.systyrant.exception.STRuntimeException;
import com.kloudtek.systyrant.host.Host;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.validator.constraints.NotEmpty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.kloudtek.systyrant.host.OperatingSystem.LINUX;

/**
 * This resource is used to install a package.
 */
@STResource("core:service")
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
    public void findType() throws STRuntimeException {
        if (type == null) {
            if (host.fileExists("/etc/init.d/" + name)) {
                type = Type.INITD;
            } else {
                throw new STRuntimeException("Unsupported service " + name);
            }
        }
    }

    @Verify
    public boolean checkEnabled() throws STRuntimeException {
        boolean status = host.exec("ls -1 /etc/rc2.d | grep '^S[0-9]*" + name + "$'", null, null).getRetCode() == 0;
        return autostart != status;
    }

    @Sync(order = 1)
    public void setEnabled() throws STRuntimeException {
        String cmd = autostart ? "enable" : "disable";
        host.exec("update-rc.d "+name+" " + cmd);
        logger.info(StringUtils.capitalize(cmd) + "d service " + name);
    }

    @Verify("running")
    public boolean checkRunning() throws STRuntimeException {
        switch (type) {
            case INITD:
                boolean status = host.exec("/etc/init.d/" + name + " status", null, null).getRetCode() == 0;
                return running != status;
            default:
                throw new STRuntimeException("BUG: Unknown service type " + type);
        }
    }

    @Sync("running")
    public void setRunning() throws STRuntimeException {
        switch (type) {
            case INITD:
                host.exec("/etc/init.d/" + name + " " + (running ? "start" : "stop"));
                break;
            default:
                throw new STRuntimeException("BUG: Unknown service type " + type);
        }
        logger.info((running ? "Started" : "Stopped") + " service " + name);
    }

    public enum Type {
        INITD
    }
}
