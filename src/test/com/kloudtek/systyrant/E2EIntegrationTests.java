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
    public void testSetupFullEnv() {
        cli("-d", "src/test/com/kloudtek/systyrant/e2e.stl");
    }

    @Test
    public void testCredStoreEncryptionViaCli() throws IOException {
        try (TempDir dir = new TempDir("testclienc")) {
            File crypPw = new File(dir, "cryppw.txt");
            FileUtils.writeStringToFile(crypPw, "secretpw");
            File stl = new File(dir, "test.stl");
            File creds = new File(dir, "creds.xml");
            FileUtils.write(stl, "core.file { 'myid': path='" + dir.getPath() + File.separator + "pw.txt' content=password() }");
            Assert.assertEquals(Cli.execute("-c", "-cpw", crypPw.getPath(), "-cf", creds.getPath(), stl.getPath()), 0);
            String pw1 = FileUtils.readFileToString(new File(dir, "pw.txt"));
            FileUtils.write(stl, "core.file { 'myid': path='" + dir.getPath() + File.separator + "pw2.txt' content=password() }");
            Assert.assertEquals(Cli.execute("-c", "-cpw", crypPw.getPath(), "-cf", creds.getPath(), stl.getPath()), 0);
            String pw2 = FileUtils.readFileToString(new File(dir, "pw2.txt"));
            Assert.assertEquals(pw1, pw2);
            Assert.assertFalse(FileUtils.readFileToString(creds).contains("myid"), "Credentials not encryption could find 'myid' in creds file");
            Assert.assertFalse(FileUtils.readFileToString(creds).contains(pw1), "Credentials not encryption could find the password in creds file");
        }
    }

    @Test
    public void testCredStorePlainTextViaCli() throws IOException {
        try (TempDir dir = new TempDir("testclienc")) {
            File stl = new File(dir, "test.stl");
            File creds = new File(dir, "creds.xml");
            FileUtils.write(stl, "core.file { 'myid': path='" + dir.getPath() + File.separator + "pw.txt' content=password() }");
            Assert.assertEquals(Cli.execute("-cf", creds.getPath(), stl.getPath()), 0);
            String pw1 = FileUtils.readFileToString(new File(dir, "pw.txt"));
            FileUtils.write(stl, "core.file { 'myid': path='" + dir.getPath() + File.separator + "pw2.txt' content=password() }");
            cli("-cf", creds.getPath(), stl.getPath());
            String pw2 = FileUtils.readFileToString(new File(dir, "pw2.txt"));
            Assert.assertEquals(pw1, pw2);
            Assert.assertTrue(FileUtils.readFileToString(creds).contains("myid"));
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Test
    public void testDynaLoad() throws IOException {
        try (TempDir dir = new TempDir("testclienc")) {
            File results = new File(dir, "results.txt");
            File libs = new File(dir, "libs");
            File mod = new File(libs, "mytest/mytest");
            mod.mkdirs();
            FileUtils.writeStringToFile(new File(mod, "mytest.stl"), "def mytest { core.file { path='" + results.getAbsolutePath() + "', content=\"ohyeah\" } }");
            File stl = new File(dir, "dotest.stl");
            FileUtils.writeStringToFile(stl, "mytest.mytest {}");
            cli("-l", libs.getAbsolutePath(), stl.getAbsolutePath());
            Assert.assertTrue(results.exists());
            Assert.assertEquals(FileUtils.readFileToString(results), "ohyeah");
        }
    }

    private static void cli(String... args) {
        Assert.assertEquals(Cli.execute(args), 0);
    }
}
