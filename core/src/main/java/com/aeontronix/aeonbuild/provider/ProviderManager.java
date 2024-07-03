/*
 * Copyright (c) 2024 Aeontronix Inc
 */

package com.aeontronix.aeonbuild.provider;

public interface ProviderManager<X> {
    Class<X> getProviderInterface();

    void registerProvider(X providerClass);
}
