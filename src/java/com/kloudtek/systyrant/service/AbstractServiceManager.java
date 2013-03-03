/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.systyrant.service;

import org.jetbrains.annotations.NotNull;

/**
 * Simple implementation of the {@link com.kloudtek.systyrant.service.ServiceManager} interface.
 */
public abstract class AbstractServiceManager implements ServiceManager {


    protected abstract Object doGetService(@NotNull String id);
}
