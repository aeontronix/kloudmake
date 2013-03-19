/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.systyrant.resource.builtin.core;

import com.kloudtek.systyrant.annotation.*;
import com.kloudtek.systyrant.host.Host;
import org.hibernate.validator.constraints.NotEmpty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This resource is used to install a package.
 */
@STResource
public class ServiceResource {
    private static final Logger logger = LoggerFactory.getLogger(ServiceResource.class);
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

    @Verify
    public boolean checkEnabled() {

        return false;
    }

    @Sync(order = 1)
    public void setEnabled() {

    }

    @Verify
    public boolean checkRunning() {
        return false;
    }

    @Sync
    public void setRunning() {

    }

    public enum Type {
        INITD
    }
}
