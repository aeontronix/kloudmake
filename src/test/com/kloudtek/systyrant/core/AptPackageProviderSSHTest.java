/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.systyrant.core;

import com.kloudtek.systyrant.AbstractVagrantTest;
import com.kloudtek.systyrant.exception.InvalidServiceException;
import com.kloudtek.systyrant.exception.STRuntimeException;
import com.kloudtek.systyrant.resource.builtin.core.AptPackageProvider;
import com.kloudtek.systyrant.host.Host;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class AptPackageProviderSSHTest extends AbstractVagrantTest {
    private static final String APTCMD = "apt-get -o Dpkg::Options::='--force-confdef' -o Dpkg::Options::='--force-confold' -f -q -y ";
    private AptPackageProvider provider;

    @BeforeMethod(groups = "vagrant")
    public void init() throws STRuntimeException, InvalidServiceException {
        provider = new AptPackageProvider(ctx.host());
    }

    @Test(groups = "vagrant")
    public void testCheckCurrentlyInstalledThatIs() throws Exception {
        String version = ctx.host().exec("dpkg -s bash | grep Version").substring(9).trim();
        assertEquals(provider.checkCurrentlyInstalled("bash"), version);
    }

    @Test(groups = "vagrant")
    public void testCheckCurrentlyInstalledThatIsNot() throws Exception {
        ctx.host().exec(APTCMD + " purge aespipe");
        assertNull(provider.checkCurrentlyInstalled("aespipe"));
    }

    @Test(groups = "vagrant")
    public void testCheckAvailableExistent() throws Exception {
        String version = ctx.host().exec("apt-cache show aespipe | grep Version").substring(9).trim();
        assertEquals(provider.checkLatestAvailable("aespipe"), version);
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
        Host h = ctx.host();
        String available = h.exec("apt-cache show aespipe | grep Version").substring(9).trim();
        h.exec(APTCMD + " purge aespipe");
        provider.install("aespipe", null);
        String installed = h.exec("dpkg -s aespipe | grep Version").substring(9).trim();
        assertEquals(installed, available);
    }

    @Test(groups = "vagrant")
    public void testInstallSpecific() throws Exception {
        Host h = ctx.host();
        String available = h.exec("apt-cache show aespipe | grep Version").substring(9).trim();
        h.exec(APTCMD + " purge aespipe");
        provider.install("aespipe", available);
        String installed = h.exec("dpkg -s aespipe | grep Version").substring(9).trim();
        assertEquals(installed, available);
    }
}
