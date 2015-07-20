/*
 * Copyright (c) 2015. Kelewan Technologies Ltd
 */

package com.kloudtek.kloudmake.provider;

public interface ProviderManager<X> {
    Class<X> getProviderInterface();

    void registerProvider(X providerClass);
}
