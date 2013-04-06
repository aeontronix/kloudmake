/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.systyrant;

import com.kloudtek.systyrant.cli.Cli;
import com.kloudtek.util.TempDir;
import org.apache.commons.io.FileUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;

public class E2EIntegrationTests {
    @Test(groups = "vagrant")
    public void testE2E() {
        Cli.main(new String[]{"src/test/com/kloudtek/systyrant/e2e.stl"});
    }

    @Test
    public void testCredStoreEncryptionViaCli() throws IOException {
        try (TempDir dir = new TempDir("testclienc")) {
            File stl = new File(dir, "test.stl");
            FileUtils.write(stl, "new core:file { 'myid': path='" + dir.getPath() + File.separator + "pw.txt' content=password() }");
            Assert.assertEquals(Cli.execute(stl.getPath()), 0);
        }
    }
}
