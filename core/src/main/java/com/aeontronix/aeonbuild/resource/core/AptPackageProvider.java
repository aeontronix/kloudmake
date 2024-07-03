/*
 * Copyright (c) 2024 Aeontronix Inc
 */

package com.aeontronix.aeonbuild.resource.core;

import com.aeontronix.aeonbuild.exception.KMRuntimeException;
import com.aeontronix.aeonbuild.host.ExecutionResult;
import com.aeontronix.aeonbuild.host.Host;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AptPackageProvider implements PackageProvider {
    private static final Logger logger = LoggerFactory.getLogger(AptPackageProvider.class);
    private static final String APTCMD = "apt-get -o Dpkg::Options::='--force-confdef' -o Dpkg::Options::='--force-confold' -f -q -y ";
    private static final Pattern REGEX_STATUS = Pattern.compile("Status: (.*)");
    private static final Pattern REGEX_VERSION = Pattern.compile("Version: (.*)");
    public static final String APT_UPDATE_TIMESTAMP = "apt-update-timestamp";
    private HashMap<String, String> dpkgEnv = new HashMap<>();
    private Host host;

    public AptPackageProvider(Host host) {
        dpkgEnv.put("DEBIAN_FRONTEND", "noninteractive");
        this.host = host;
    }

    @Override
    public String checkCurrentlyInstalled(String name) throws KMRuntimeException {
        ExecutionResult checkResult = exec("dpkg -s " + name, null, Host.Logging.ON_ERROR);
        String result = checkResult.getOutput();
        if (checkResult.getRetCode() != 0) {
            if (result != null && result.trim().toLowerCase().contains("is not installed")) {
                return null;
            } else {
                throw new KMRuntimeException("An error occured while checking package status: " + result);
            }
        } else {
            Matcher versionMatcher = REGEX_VERSION.matcher(result);
            if (!versionMatcher.find()) {
                throw new KMRuntimeException("An error occured while checking package status: " + result);
            }
            return versionMatcher.group(1);
        }
    }

    @Override
    public String checkLatestAvailable(String name) throws KMRuntimeException {
        String versionStr = exec("apt-cache show --no-all-versions " + name + " | grep -E 'Version:.*'");
        if (!versionStr.startsWith("Version: ") || versionStr.trim().contains("\n")) {
            throw new KMRuntimeException("apt-cache returned unexpected string: " + versionStr);
        }
        return versionStr.substring(9).trim();
    }

    @Override
    public boolean isNewer(@Nullable String proposed, @Nullable String current) throws KMRuntimeException {
        if (proposed == null && current == null) {
            return false;
        } else if (proposed == null) {
            return false;
        } else if (current == null) {
            return true;
        } else {
            ExecutionResult res = exec("dpkg --compare-versions " + proposed + " gt " + current, null, Host.Logging.NO);
            if (res.getRetCode() != 0 && (res.getOutput() != null && !res.getOutput().trim().isEmpty())) {
                throw new KMRuntimeException("Unexpected error comparing apt versions: " + res.getOutput());
            }
            return res.getRetCode() == 0;
        }
    }

    @Override
    public void install(@NotNull String name, String version) throws KMRuntimeException {
        install(name, version, false);
    }

    @Override
    public void update() throws KMRuntimeException {
        Date lastUpdated = (Date) host.getState(APT_UPDATE_TIMESTAMP);
        if (lastUpdated == null) {
            host.exec(APTCMD + " update");
            host.setState(APT_UPDATE_TIMESTAMP, new Date());
        }
    }

    @Override
    public void install(@NotNull String name, @Nullable String version, boolean includeRecommended) throws KMRuntimeException {
        StringBuilder cmd = new StringBuilder(APTCMD).append("install ");
        if (includeRecommended) {
            cmd.append("--install-recommends");
        } else {
            cmd.append("--no-install-recommends");
        }
        cmd.append(' ').append(name);
        if (version != null) {
            cmd.append('=').append(version);
        }
        exec(cmd.toString());
    }

    private String exec(String cmd) throws KMRuntimeException {
        logger.debug("Executing: {}", cmd);
        return host.exec(cmd);
    }

    private ExecutionResult exec(String cmd, @Nullable Integer expectedRetCode, Host.Logging logging) throws KMRuntimeException {
        logger.debug("Executing: {} expecting {} logging {}", cmd, expectedRetCode, logging);
        return host.exec(cmd, 30L * 60000L, expectedRetCode, logging, null);
    }
}
