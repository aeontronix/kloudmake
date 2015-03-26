/*
 * Copyright (c) 2015. Kelewan Technologies Ltd
 */

package com.kloudtek.kloudmake.resource;/*
 * Copyright (c) 2013 KloudTek Ltd
 */

import com.kloudtek.kloudmake.AbstractVagrantTest;
import com.kloudtek.kloudmake.exception.InvalidResourceDefinitionException;
import com.kloudtek.kloudmake.exception.KMRuntimeException;
import com.kloudtek.kloudmake.host.Host;
import com.kloudtek.kloudmake.resource.core.AptPackageProvider;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.HashMap;

import static org.testng.Assert.*;

public class AptPackageProviderSSHTest extends AbstractVagrantTest {
    private static final String APTCMD = "apt-get -o Dpkg::Options::='--force-confdef' -o Dpkg::Options::='--force-confold' -f -q -y ";
    public static final String AESPIPE = "aespipe";
    private AptPackageProvider provider;

    @BeforeClass(groups = "vagrant")
    public void init() throws KMRuntimeException, IOException, InvalidResourceDefinitionException {
        super.init();
        provider = new AptPackageProvider(sshHost);
        sshHost.exec("apt-get update");
    }

    @Test(groups = "vagrant")
    public void testCheckCurrentlyInstalledThatIs() throws Exception {
        String version = findVersion(AESPIPE);
        installPkg(AESPIPE);
        Assert.assertEquals(provider.checkCurrentlyInstalled(AESPIPE), version);
    }

    @Test(groups = "vagrant")
    public void testCheckCurrentlyInstalledThatIsNot() throws Exception {
        removePkg(AESPIPE);
        assertNull(provider.checkCurrentlyInstalled(AESPIPE));
    }

    @Test(groups = "vagrant")
    public void testCheckAvailableExistent() throws Exception {
        String version = findVersion(AESPIPE);
        Assert.assertEquals(provider.checkLatestAvailable(AESPIPE), version);
    }

    @Test(groups = "vagrant")
    public void testOneIsNewerThanNothing() throws Exception {
        assertTrue(provider.isNewer("1.0", null));
    }

    @Test(groups = "vagrant")
    public void testNothingIsNotNewerThanOne() throws Exception {
        assertFalse(provider.isNewer(null, "1.0"));
    }

    @Test(groups = "vagrant")
    public void test10IsNotNewerThan10() throws Exception {
        assertFalse(provider.isNewer("1.0", "1.0"));
    }

    @Test(groups = "vagrant")
    public void test10IsNotNewerThan11() throws Exception {
        assertFalse(provider.isNewer("1.0", "1.1"));
    }

    @Test(groups = "vagrant")
    public void testNullIsNotNewerThan11() throws Exception {
        assertFalse(provider.isNewer(null, "1.1"));
    }

    @Test(groups = "vagrant")
    public void test10devIsNotNewerThan10() throws Exception {
        assertFalse(provider.isNewer("1.0~dev", "1.0"));
    }

    @Test(groups = "vagrant")
    public void test11IsNewerThan10() throws Exception {
        assertTrue(provider.isNewer("1.1", "1.0"));
    }

    @Test(groups = "vagrant")
    public void test11IsNewerThanNull() throws Exception {
        assertTrue(provider.isNewer("1.1", null));
    }

    @Test(groups = "vagrant")
    public void test10IsNewerThan10test() throws Exception {
        assertTrue(provider.isNewer("1.0", "1.0~test"));
    }

    @Test(groups = "vagrant")
    public void testInstallLatest() throws Exception {
        String available = sshHost.exec("apt-cache show aespipe | grep Version", null, 0, Host.Logging.YES, "root").getOutput().substring(9).trim();
        removePkg(AESPIPE);
        provider.install(AESPIPE, null);
        String installed = sshHost.exec("dpkg -s aespipe | grep Version").substring(9).trim();
        assertEquals(installed, available);
    }

    private String findVersion(String pkg) throws KMRuntimeException {
        return sshHost.exec("apt-cache show " + pkg + " | grep Version").substring(9).trim();
    }

    private void removePkg(String pkg) throws KMRuntimeException {
        apt("purge -y " + pkg);
    }

    private String apt(String cmd) throws KMRuntimeException {
        HashMap<String, String> env = new HashMap<>();
        env.put("DEBIAN_FRONTEND", "noninteractive");
        return sshHost.exec(APTCMD + " " + cmd, null, Host.Logging.YES, env).getOutput();
    }

    private void installPkg(String pkg) throws KMRuntimeException {
        apt("install -y " + pkg);
        apt("upgrade -y " + pkg);
    }
}
