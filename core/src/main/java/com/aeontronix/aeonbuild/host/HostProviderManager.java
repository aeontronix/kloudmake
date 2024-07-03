/*
 * Copyright (c) 2024 Aeontronix Inc
 */

package com.aeontronix.aeonbuild.host;

import com.aeontronix.aeonbuild.exception.KMRuntimeException;
import com.aeontronix.aeonbuild.provider.ProviderManager;
import com.aeontronix.aeonbuild.annotation.Provider;
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

    public HostProvider find(Host host) throws KMRuntimeException {
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
        public boolean supports(Host host, HashMap<String, Object> datacache) throws KMRuntimeException {
            return true;
        }

        @Override
        public OperatingSystem getOperatingSystem() {
            return OperatingSystem.UNKNOWN;
        }

        @Override
        public CommandLine generateCommandLine(String command, String currentUser, String user, boolean handleQuoting, String workdir) {
            return new CommandLine("bash").addArgument("-c").addArgument(command, handleQuoting);
        }

        @Override
        public boolean isAbsolutePath(String workdir) {
            return false;
        }

        @Override
        public String getWorkingDir(Host abstractHost) throws KMRuntimeException {
            return null;
        }
    }
}
