/*
 * Copyright (c) 2024 Aeontronix Inc
 */

package com.aeontronix.aeonbuild;

import com.aeontronix.aeonbuild.inject.Injector;

import java.util.List;
import java.util.WeakHashMap;

public class KMInjectionManager {
    private final WeakHashMap<Class<?>, List<Injector>> injectors = new WeakHashMap<>();

    public KMInjectionManager() {
    }

    public void register(Object obj) {

    }

    public void inject(Object obj) {

    }

    public void registerAndInject(Object obj) {

    }
}
