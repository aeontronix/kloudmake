/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.systyrant.service.host;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: ymenager
 * Date: 03/03/2013
 * Time: 23:11
 * To change this template use File | Settings | File Templates.
 */
public abstract class AbstractHostProvider implements HostProvider {
    protected HashMap<String, String> attrs = new HashMap<>();

    protected String getUnixExecutionPrefix(@NotNull String currentUser, @NotNull String user) {
        if (currentUser.equalsIgnoreCase(user)) {
            return "";
        } else {
            StringBuilder txt = new StringBuilder("sudo");
            if (!user.equalsIgnoreCase("root")) {
                txt.append(" -u ").append(user);
            }
            txt.append(" -c '");
            return txt.toString();
        }
    }

    protected String getUnixExecutionSuffix(@NotNull String currentUser, @NotNull String user) {
        return currentUser.equalsIgnoreCase(user) ? "" : "'";
    }

}
