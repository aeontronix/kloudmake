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
    private boolean enabled;
    @Inject
    private Host host;

    public ServiceLinuxImpl() {
    }

    public ServiceLinuxImpl(String name, Type type, boolean running, boolean enabled) {
        this.name = name;
        this.type = type;
        this.running = running;
        this.enabled = enabled;
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
System.out.println("1RUNNING="+running);
        boolean status = host.exec("ls -1 /etc/rc2.d | grep '^S[0-9]*" + name + "$'", null, null).getRetCode() == 0;
        return enabled != status;
    }

    @Sync(order = 1)
    public void setEnabled() throws STRuntimeException {
System.out.println("2RUNNING="+running);
        String cmd = enabled ? "enable" : "disable";
        host.exec("update-rc.d nginx " + cmd);
        logger.info(StringUtils.capitalize(cmd) + "d service " + name);
    }

    @Verify("running")
    public boolean checkRunning() throws STRuntimeException {
System.out.println("3RUNNING="+running);
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
System.out.println("5RUNNING="+running);
        switch (type) {
            case INITD:
                host.exec("/etc/init.d/" + name + " " + (running ? "start" : "stop"));
                break;
            default:
                throw new STRuntimeException("BUG: Unknown service type " + type);
        }
        logger.info((running ? "Started" : "Stopped") + " service " + name);
System.out.println("6RUNNING="+running);
    }

    public enum Type {
        INITD
    }
}
