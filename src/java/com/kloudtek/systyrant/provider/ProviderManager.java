/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.systyrant.provider;

public interface ProviderManager<X> {
    Class<X> getProviderInterface();

    void registerProvider(X providerClass);
}
