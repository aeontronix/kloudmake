/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.systyrant.resource.builtin.core;

import com.kloudtek.systyrant.exception.STRuntimeException;
import org.jetbrains.annotations.NotNull;

public interface PackageProvider {
    String checkCurrentlyInstalled(String name) throws STRuntimeException;

    String checkLatestAvailable(String name) throws STRuntimeException;

    boolean isNewer(String proposed, String current) throws STRuntimeException;

    void install(String name, String version, boolean includeRecommended) throws STRuntimeException;

    void install(@NotNull String name, String version) throws STRuntimeException;

    void update() throws STRuntimeException;
}
