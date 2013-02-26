/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.systyrant.resource.builtin.core;

import com.kloudtek.systyrant.annotation.Execute;
import com.kloudtek.systyrant.annotation.STResource;
import com.kloudtek.systyrant.exception.STRuntimeException;
import com.kloudtek.systyrant.service.ServiceManager;
import com.kloudtek.systyrant.service.filestore.FileStore;
import com.kloudtek.systyrant.service.host.SshHost;
import org.hibernate.validator.constraints.NotEmpty;

import javax.annotation.Resource;

@STResource
public class SshHostResource {
    @Resource
    private FileStore fileStore;
    @Resource
    private ServiceManager serviceManager;
    @NotEmpty
    private String address;
    private Integer port;
    private String key;
    private SshHost sshHost;

    @Execute
    public void execute() throws STRuntimeException {
        sshHost = new SshHost();
        sshHost.setAddress(address);
        sshHost.setPort(port != null ? port : 22);
        serviceManager.addOverride("host", sshHost);
    }

    @Execute(postChildren = true)
    public void executePostChildren() throws STRuntimeException {
        serviceManager.removeOverride("host", sshHost);
    }
}
