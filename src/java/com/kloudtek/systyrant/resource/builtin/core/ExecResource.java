/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.systyrant.resource.builtin.core;

import com.kloudtek.systyrant.STContext;
import com.kloudtek.systyrant.annotation.STResource;

@STResource
public class ExecResource {
    private String command;
    private String user;

    public ExecResource(STContext context) {
    }

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
}
