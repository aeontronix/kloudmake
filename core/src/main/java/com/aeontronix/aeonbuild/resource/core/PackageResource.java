/*
 * Copyright (c) 2024 Aeontronix Inc
 */

package com.aeontronix.aeonbuild.resource.core;

import com.aeontronix.aeonbuild.annotation.Execute;
import com.aeontronix.aeonbuild.annotation.Inject;
import com.aeontronix.aeonbuild.annotation.KMResource;
import com.aeontronix.aeonbuild.exception.KMRuntimeException;
import com.aeontronix.aeonbuild.annotation.Attr;
import com.aeontronix.aeonbuild.host.Host;
import org.hibernate.validator.constraints.NotEmpty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This resource is used to install a package.
 */
@KMResource
public class PackageResource {
    private static final Logger logger = LoggerFactory.getLogger(PackageResource.class);
    @NotEmpty()
    @Attr
    private String name;
    @Attr
    private boolean installed;
    @Attr
    private String version;
    @Attr
    private Provider provider;
    @Attr
    private PackageProvider pkgProvider;
    @Attr
    private boolean includeRecommended;
    @Inject
    private Host host;

    @Execute
    public void execute() throws KMRuntimeException {
        pkgProvider = new AptPackageProvider(host);
        pkgProvider.update();
        String installed = pkgProvider.checkCurrentlyInstalled(name);
        logger.debug("Installed version for package {} is {}", name, installed);
        if (version == null) {
            version = pkgProvider.checkLatestAvailable(name);
            logger.debug("Available version for package {} is {}", name, version);
        }
        if (pkgProvider.isNewer(version, installed)) {
            logger.info("installing package {} - {}", name, version);
            pkgProvider.install(name, version, includeRecommended);
        } else {
            logger.debug("Package {} is already same version or newer than {}", name, version);
        }
    }

    public enum Provider {
        APT
    }
}
