/*
 * Copyright (c) 2024 Aeontronix Inc
 */

package com.aeontronix.aeonbuild.resource.core;

import com.aeontronix.aeonbuild.annotation.Execute;
import com.aeontronix.aeonbuild.annotation.KMResource;
import com.aeontronix.aeonbuild.exception.KMRuntimeException;
import com.aeontronix.aeonbuild.ServiceManager;
import com.aeontronix.aeonbuild.annotation.Attr;
import com.aeontronix.aeonbuild.annotation.Service;
import com.aeontronix.aeonbuild.host.SshHost;
import com.aeontronix.aeonbuild.service.filestore.FileStore;
import org.hibernate.validator.constraints.NotEmpty;

@KMResource
public class SshHostResource {
    @Service
    private FileStore fileStore;
    @Service
    private ServiceManager serviceManager;
    @NotEmpty
    @Attr
    private String address;
    @Attr
    private Integer port;
    @Attr
    private String key;
    private SshHost sshHost;

    @Execute
    public void execute() throws KMRuntimeException {
        sshHost = new SshHost();
        sshHost.setAddress(address);
        sshHost.setPort(port != null ? port : 22);
        serviceManager.addOverride("host", sshHost);
    }

    @Execute(postChildren = true)
    public void executePostChildren() throws KMRuntimeException {
        serviceManager.removeOverride("host", sshHost);
    }
}
