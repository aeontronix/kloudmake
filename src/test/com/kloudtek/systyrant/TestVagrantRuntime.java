/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.systyrant;

import com.google.common.reflect.AbstractInvocationHandler;
import com.kloudtek.systyrant.exception.STRuntimeException;
import com.kloudtek.systyrant.host.LinuxMetadataProvider;
import com.kloudtek.systyrant.host.LocalHost;
import com.kloudtek.systyrant.host.SshHost;
import com.kloudtek.systyrant.resource.builtin.vagrant.SharedFolder;
import com.kloudtek.systyrant.resource.builtin.vagrant.VagrantResource;
import com.kloudtek.systyrant.service.ServiceManager;
import org.mockito.Mockito;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;

import static com.kloudtek.systyrant.util.ReflectionHelper.set;

public class TestVagrantRuntime {
    private final VagrantResource vagrantResource;
    private final SshHost sshHost;

    public TestVagrantRuntime() {
        this(null, null);
    }

    public TestVagrantRuntime(String user, VagrantResource.Ensure after) {
        try {
            ServiceManager serviceManager = (ServiceManager) Proxy.newProxyInstance(getClass().getClassLoader(), new Class[]{ServiceManager.class}, new AbstractInvocationHandler() {
                @Override
                protected Object handleInvocation(Object o, Method method, Object[] objects) throws Throwable {
                    SshHost host = (SshHost) objects[1];
                    set(host, "hostProvider", new LinuxMetadataProvider());
                    return null;
                }
            });
            Resource resource = Mockito.mock(Resource.class);
            vagrantResource = new VagrantResource("ubuntu-precise64", "_vagrant", VagrantResource.Ensure.UP, after,
                    LocalHost.createStandalone(), serviceManager, Arrays.asList(new SharedFolder(true, true, "test", "_vagrant/test", "/test")));
            set(vagrantResource, "resource", resource);
            vagrantResource.exec();
            sshHost = vagrantResource.getSshHost();
            set(sshHost, "hostProvider", new LinuxMetadataProvider());
            sshHost.start();
            if (user != null) {
                // TODO
            }
        } catch (STRuntimeException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public SshHost getSshHost() {
        return sshHost;
    }

    public void close() {
        try {
            vagrantResource.postChildrens();
        } catch (STRuntimeException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}
