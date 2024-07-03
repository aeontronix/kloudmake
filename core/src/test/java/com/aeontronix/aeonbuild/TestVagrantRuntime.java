/*
 * Copyright (c) 2024 Aeontronix Inc
 */

package com.aeontronix.aeonbuild;

import com.google.common.reflect.AbstractInvocationHandler;
import com.aeontronix.aeonbuild.exception.KMRuntimeException;
import com.aeontronix.aeonbuild.host.LinuxMetadataProvider;
import com.aeontronix.aeonbuild.host.LocalHost;
import com.aeontronix.aeonbuild.host.SshHost;
import com.aeontronix.aeonbuild.resource.vagrant.SharedFolder;
import com.aeontronix.aeonbuild.resource.vagrant.VagrantResource;
import org.mockito.Mockito;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;

import static com.aeontronix.aeonbuild.util.ReflectionHelper.set;

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
        } catch (KMRuntimeException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public SshHost getSshHost() {
        return sshHost;
    }

    public void close() {
        try {
            vagrantResource.postChildrens();
        } catch (KMRuntimeException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}
