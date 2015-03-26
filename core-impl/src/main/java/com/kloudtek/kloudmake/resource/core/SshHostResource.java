/*
 * Copyright (c) 2015. Kelewan Technologies Ltd
 */

package com.kloudtek.kloudmake.resource.core;

import com.kloudtek.kloudmake.ServiceManager;
import com.kloudtek.kloudmake.annotation.Attr;
import com.kloudtek.kloudmake.annotation.Execute;
import com.kloudtek.kloudmake.annotation.STResource;
import com.kloudtek.kloudmake.annotation.Service;
import com.kloudtek.kloudmake.exception.KMRuntimeException;
import com.kloudtek.kloudmake.host.SshHost;
import com.kloudtek.kloudmake.service.filestore.FileStore;
import org.hibernate.validator.constraints.NotEmpty;

@STResource
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
