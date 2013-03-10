/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.systyrant.service.host;

import com.kloudtek.systyrant.annotation.Provider;
import com.kloudtek.systyrant.exception.STRuntimeException;
import com.kloudtek.systyrant.provider.ProviderManager;
import org.apache.commons.exec.CommandLine;

import java.util.ArrayList;
import java.util.HashMap;

@Provider
public class HostProviderManager implements ProviderManager<HostProvider> {
    private ArrayList<HostProvider> providers = new ArrayList<>();

    @Override
    public Class<HostProvider> getProviderInterface() {
        return HostProvider.class;
    }

    @Override
    public void registerProvider(HostProvider provider) {
        providers.add(provider);
    }

    public HostProvider find(Host host) throws STRuntimeException {
        HashMap<String, Object> datacache = new HashMap<>();
        for (HostProvider provider : providers) {
            if (provider.supports(host, datacache)) {
                return provider;
            }
        }
        return new UnsupportedMetadataProvider();
    }

    class UnsupportedMetadataProvider implements HostProvider {
        @Override
        public boolean supports(Host host, HashMap<String, Object> datacache) throws STRuntimeException {
            return true;
        }

        @Override
        public OperatingSystem getOperatingSystem() {
            return OperatingSystem.UNKNOWN;
        }

        @Override
        public String getExecutionPrefix(String currentUser, String user) {
            return "";
        }

        @Override
        public String getExecutionSuffix(String currentUser, String user) {
            return "";
        }

        @Override
        public CommandLine generateCommandLine(String command, String currentUser, String user, boolean handleQuoting) {
            return new CommandLine("bash").addArgument("-c").addArgument(command, handleQuoting);
        }
    }
}
