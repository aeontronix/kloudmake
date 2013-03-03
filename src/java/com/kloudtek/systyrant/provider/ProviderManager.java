/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.systyrant.provider;

public interface ProviderManager {
    Class<?> getProviderInterface();

    void registerProvider(Object providerClass);
}
