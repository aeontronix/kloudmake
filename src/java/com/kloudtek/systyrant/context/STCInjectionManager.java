/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.systyrant.context;

import com.kloudtek.systyrant.context.ioc.Injector;

import java.util.List;
import java.util.WeakHashMap;

public class STCInjectionManager {
    private STContextData data;
    private final WeakHashMap<Class<?>, List<Injector>> injectors = new WeakHashMap<>();

    public STCInjectionManager(STContextData data) {
        this.data = data;
    }

    public void register(Object obj) {

    }

    public void inject(Object obj) {

    }

    public void registerAndInject(Object obj) {

    }
}
