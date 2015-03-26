/*
 * Copyright (c) 2015. Kelewan Technologies Ltd
 */

package com.kloudtek.kloudmake.resource.core;

import com.kloudtek.kloudmake.exception.KMRuntimeException;
import org.jetbrains.annotations.NotNull;

public interface PackageProvider {
    String checkCurrentlyInstalled(String name) throws KMRuntimeException;

    String checkLatestAvailable(String name) throws KMRuntimeException;

    boolean isNewer(String proposed, String current) throws KMRuntimeException;

    void install(String name, String version, boolean includeRecommended) throws KMRuntimeException;

    void install(@NotNull String name, String version) throws KMRuntimeException;

    void update() throws KMRuntimeException;
}
