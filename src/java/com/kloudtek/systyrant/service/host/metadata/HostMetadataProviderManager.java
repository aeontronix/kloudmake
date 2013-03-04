/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.systyrant.service.host.metadata;

import com.kloudtek.systyrant.annotation.Provider;
import com.kloudtek.systyrant.exception.STRuntimeException;
import com.kloudtek.systyrant.provider.ProviderManager;
import com.kloudtek.systyrant.service.host.Host;

import java.util.ArrayList;
import java.util.HashMap;

@Provider
public class HostMetadataProviderManager implements ProviderManager<HostMetadataProvider> {
    private ArrayList<HostMetadataProvider> providers = new ArrayList<>();

    @Override
    public Class<HostMetadataProvider> getProviderInterface() {
        return HostMetadataProvider.class;
    }

    @Override
    public void registerProvider(HostMetadataProvider provider) {
        providers.add(provider);
    }

    public HostMetadataProvider find(Host host) throws STRuntimeException {
        HashMap<String, Object> datacache = new HashMap<>();
        for (HostMetadataProvider provider : providers) {
            if (provider.supports(host, datacache)) {
                return provider;
            }
        }
        return new UnsupportedMetadataProvider();
    }

    class UnsupportedMetadataProvider implements HostMetadataProvider {
        @Override
        public boolean supports(Host host, HashMap<String, Object> datacache) throws STRuntimeException {
            return true;
        }

        @Override
        public OperatingSystem getOperatingSystem() {
            return OperatingSystem.UNKNOWN;
        }
    }
}
