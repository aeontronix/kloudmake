/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.systyrant.provider;

import com.kloudtek.systyrant.annotation.Provider;
import com.kloudtek.systyrant.exception.InvalidServiceException;
import com.kloudtek.systyrant.exception.STRuntimeException;
import com.kloudtek.systyrant.util.ListHashMap;
import org.reflections.Reflections;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Set;

public class ProvidersManagementService {
    protected HashMap<Class<? extends ProviderManager>, ProviderManager> providerManagers = new HashMap<>();
    protected HashMap<Class<?>, ProviderManager> providerClasses = new HashMap<>();
    protected ListHashMap<Class<?>, Object> providerImplementations = new ListHashMap<>();

    public void registerProviderManager(Class<? extends ProviderManager> clazz) throws InvalidServiceException {
        try {
            ProviderManager instance = clazz.newInstance();
            providerManagers.put(clazz, instance);
            providerClasses.put(instance.getProviderInterface(), instance);
        } catch (InstantiationException | IllegalAccessException e) {
            throw new InvalidServiceException("ProviderManager " + clazz.getName() + " cannot be instantiated", e);
        }
    }

    public void registerProvider(Class<?> provider) throws InvalidServiceException {
        ProviderManager providerManager = providerClasses.get(provider);
        if (providerManager == null) {
            throw new InvalidServiceException("Unable");
        }
    }

    public void init(Reflections libraries) throws STRuntimeException {
        Set<Class<?>> classes = libraries.getTypesAnnotatedWith(Provider.class);
        ArrayList<Class<? extends ProviderManager>> pmlist = new ArrayList<>();
        ArrayList<Class<?>> plist = new ArrayList<>();
        for (Class<?> clazz : classes) {
            if (ProviderManager.class.isAssignableFrom(clazz)) {
                pmlist.add(clazz.asSubclass(ProviderManager.class));
            } else {
                plist.add(clazz);
            }
        }
        for (Class<? extends ProviderManager> clazz : pmlist) {
            try {
                ProviderManager providerManager = clazz.newInstance();
                providerManagers.put(clazz, providerManager);
                providerClasses.put(providerManager.getProviderInterface(), providerManager);
            } catch (InstantiationException | IllegalAccessException e) {
                throw new STRuntimeException("Unable to instantiate provider manager " + clazz.getName());
            }
        }
        for (Class<?> clazz : plist) {
            try {
                Object provider = clazz.newInstance();
                providerImplementations.get(clazz).add(provider);
                LinkedList<Class> classList = new LinkedList<>();
                classList.add(clazz);
                while (!classList.isEmpty()) {
                    Class c = classList.removeLast();
                    ProviderManager providerManager = providerClasses.get(c);
                    if (providerManager != null) {
                        providerManager.registerProvider(provider);
                    }
                    Class superclass = c.getSuperclass();
                    if (superclass != null) {
                        classList.add(superclass);
                    }
                    Class[] interfaces = c.getInterfaces();
                    if (interfaces != null) {
                        for (Class iface : interfaces) {
                            classList.add(iface);
                        }
                    }
                }
            } catch (InstantiationException | IllegalAccessException e) {
                throw new STRuntimeException("Unable to instantiate provider " + clazz.getName());
            }
        }
    }

    @SuppressWarnings("unchecked")
    public <X extends ProviderManager> X getProviderManager(Class<X> declaringClass) {
        return (X) providerManagers.get(declaringClass);
    }
}
