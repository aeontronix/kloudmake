/*
 * Copyright (c) 2015. Kelewan Technologies Ltd
 */

package com.kloudtek.kloudmake.host;

import com.kloudtek.kloudmake.annotation.Provider;
import com.kloudtek.kloudmake.exception.KMRuntimeException;
import com.kloudtek.kloudmake.provider.ProviderManager;
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
