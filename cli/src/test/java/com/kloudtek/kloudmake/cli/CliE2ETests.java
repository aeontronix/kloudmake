/*
 * Copyright (c) 2015. Kelewan Technologies Ltd
 */

package com.kloudtek.kloudmake.cli;

import com.kloudtek.util.FileUtils;
import com.kloudtek.util.TempDir;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;

public class CliE2ETests {
    @Test(groups = "vagrant")
    public void testSetupFullEnv() {
        cli("-d", "src/test/com/kloudtek/kloudmake/e2e.stl");
    }

    @Test
    public void testCredStoreEncryptionViaCli() throws IOException {
        try (TempDir dir = new TempDir("testclienc")) {
            File crypPw = new File(dir, "cryppw.txt");
            FileUtils.write(crypPw, "secretpw");
            File stl = new File(dir, "test.stl");
            File creds = new File(dir, "creds.xml");
            FileUtils.write(stl, "core.file { 'myid': path='" + dir.getPath() + File.separator + "pw.txt' content=password() }");
            Assert.assertEquals(Cli.execute("-c", "-cpw", crypPw.getPath(), "-cf", creds.getPath(), stl.getPath()), 0);
            String pw1 = FileUtils.toString(new File(dir, "pw.txt"));
            FileUtils.write(stl, "core.file { 'myid': path='" + dir.getPath() + File.separator + "pw2.txt' content=password() }");
            Assert.assertEquals(Cli.execute("-c", "-cpw", crypPw.getPath(), "-cf", creds.getPath(), stl.getPath()), 0);
            String pw2 = FileUtils.toString(new File(dir, "pw2.txt"));
            Assert.assertEquals(pw1, pw2);
            Assert.assertFalse(FileUtils.toString(creds).contains("myid"), "Credentials not encryption could find 'myid' in creds file");
            Assert.assertFalse(FileUtils.toString(creds).contains(pw1), "Credentials not encryption could find the password in creds file");
        }
    }

    @Test
    public void testCredStorePlainTextViaCli() throws IOException {
        try (TempDir dir = new TempDir("testclienc")) {
            File stl = new File(dir, "test.stl");
            File creds = new File(dir, "creds.xml");
            FileUtils.write(stl, "core.file { 'myid': path='" + dir.getPath() + File.separator + "pw.txt' content=password() }");
            Assert.assertEquals(Cli.execute("-cf", creds.getPath(), stl.getPath()), 0);
            String pw1 = FileUtils.toString(new File(dir, "pw.txt"));
            FileUtils.write(stl, "core.file { 'myid': path='" + dir.getPath() + File.separator + "pw2.txt' content=password() }");
            cli("-cf", creds.getPath(), stl.getPath());
            String pw2 = FileUtils.toString(new File(dir, "pw2.txt"));
            Assert.assertEquals(pw1, pw2);
            Assert.assertTrue(FileUtils.toString(creds).contains("myid"));
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
            FileUtils.write(new File(mod, "mytest.stl"), "def mytest { core.file { path='" + results.getAbsolutePath() + "', content=\"ohyeah\" } }");
            File stl = new File(dir, "dotest.stl");
            FileUtils.write(stl, "mytest.mytest {}");
            cli("-l", libs.getAbsolutePath(), stl.getAbsolutePath());
            Assert.assertTrue(results.exists());
            Assert.assertEquals(FileUtils.toString(results), "ohyeah");
        }
    }

    private static void cli(String... args) {
        Assert.assertEquals(Cli.execute(args), 0);
    }
}
