/*
 * Copyright (c) 2015. Kelewan Technologies Ltd
 */

package com.kloudtek.kloudmake;

import com.google.common.reflect.AbstractInvocationHandler;
import com.kloudtek.kloudmake.exception.KMRuntimeException;
import com.kloudtek.kloudmake.host.LinuxMetadataProvider;
import com.kloudtek.kloudmake.host.LocalHost;
import com.kloudtek.kloudmake.host.SshHost;
import com.kloudtek.kloudmake.resource.vagrant.SharedFolder;
import com.kloudtek.kloudmake.resource.vagrant.VagrantResource;
import org.mockito.Mockito;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;

import static com.kloudtek.kloudmake.util.ReflectionHelper.set;

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
