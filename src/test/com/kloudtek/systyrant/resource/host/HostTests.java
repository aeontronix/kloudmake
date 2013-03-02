/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.systyrant.resource.host;

import com.jcraft.jsch.JSchException;
import com.kloudtek.systyrant.ExecutionResult;
import com.kloudtek.systyrant.FileInfo;
import com.kloudtek.systyrant.exception.STRuntimeException;
import com.kloudtek.systyrant.service.host.Host;
import com.kloudtek.systyrant.service.host.LocalHost;
import com.kloudtek.systyrant.service.host.SshHost;
import com.kloudtek.util.CryptoUtils;
import com.kloudtek.util.StringUtils;
import org.apache.commons.io.FileUtils;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.kloudtek.systyrant.FileInfo.Type.*;
import static com.kloudtek.systyrant.service.host.Host.Logging.YES;
import static java.nio.file.LinkOption.NOFOLLOW_LINKS;
import static org.testng.Assert.*;

public class HostTests {
    private final Type type;
    private String fileUser;
    private Host admin;
    private File realTestDir;
    private String testPath;
    private byte[] testData1 = new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
    private byte[] testData1Sha = CryptoUtils.sha1(testData1);
    private byte[] testData2 = new byte[]{1, 2, 3, 4, 5, 6};

    public HostTests() {
        this(Type.LOCAL);
    }

    public HostTests(Type type) {
        this.type = type;
    }

    @BeforeClass
    public void setup() throws IOException, JSchException {
        switch (type) {
            case LOCAL:
                setup(new LocalHost(), System.clearProperty("user.name"));
                break;
            case SSH_SUDO:
                setup(SshHost.createSSHAdminForVagrantInstance("localhost", 2222, "vagrant"), "vagrant");
                break;
            case SSH_ROOT:
                setup(SshHost.createSSHAdminForVagrantInstance("localhost", 2222, "root"), "vagrant");
                break;
        }
    }

    private void setup(Host admin, String fileUser) throws IOException {
        this.admin = admin;
        this.fileUser = fileUser;
        realTestDir = new File("_test");
        if (!realTestDir.exists()) {
            if (!realTestDir.mkdirs()) {
                throw new IOException("Unable to create test dir");
            }
        }

        if (admin instanceof LocalHost) {
            testPath = realTestDir.getAbsolutePath();
        } else {
            testPath = "/test";
        }
    }

    @BeforeMethod
    public void clean() throws IOException {
        File[] childrens = realTestDir.listFiles();
        if (childrens != null) {
            for (File file : childrens) {
                FileUtils.forceDelete(file);
            }
        }
    }

    @Test(dependsOnMethods = "testWriteFileByteArraySuccessful")
    public void testExecSuccessfulWithLogging() throws STRuntimeException {
        TestFile file = new TestFile().writeTestData();
        ExecutionResult result = admin.exec("dir " + file.path, 0, YES);
        Assert.assertEquals(result.getOutput().trim(), file.path);
        Assert.assertEquals(result.getRetCode(), 0);
        file.assertNoOtherFiles();
    }

    @Test(dependsOnMethods = "testWriteFileByteArraySuccessful")
    public void testExecSuccessfulWithoutException() throws STRuntimeException {
        TestFile file = new TestFile().writeTestData();
        ExecutionResult result = admin.exec("dir " + file.path, null, YES);
        Assert.assertEquals(result.getOutput().trim(), file.path);
        Assert.assertEquals(result.getRetCode(), 0);
        file.assertNoOtherFiles();
    }

    @Test(expectedExceptions = STRuntimeException.class)
    public void testExecUnsuccessfulWithException() throws STRuntimeException {
        admin.exec("dir sfdafadsfsda");
    }

    @Test()
    public void testExecUnsuccessfulWithRetCode() throws STRuntimeException {
        ExecutionResult result = admin.exec("dir asffdsa", null, YES);
        assertTrue(result.getRetCode() != 0);
    }

    @Test(dependsOnMethods = "testWriteFileByteArraySuccessful")
    public void testExecScript() throws IOException, STRuntimeException {
        TestFile file1 = new TestFile().writeTestData();
        TestFile file2 = new TestFile().writeTestData();
        String script = "#!/bin/bash\ndir " + file1.path + "\ndir " + file2.path;
        ExecutionResult result = admin.execScript(script, Host.ScriptType.BASH, Host.DEFAULT_TIMEOUT, 0, YES, true);
        Assert.assertEquals(result.getOutput().trim(), file1.path + "\n" + file2.path);
        Assert.assertEquals(result.getRetCode(), 0);
    }

    @Test(dependsOnMethods = "testWriteFileByteArraySuccessful")
    public void testFileEmptyExistExpectSuccess() throws STRuntimeException {
        TestFile file = new TestFile().writeEmpty();
        assertTrue(admin.fileExists(file.path));
        file.assertNoOtherFiles();
    }

    @Test(dependsOnMethods = "testWriteFileByteArraySuccessful")
    public void testNonEmptyFileExistExpectSuccess() throws STRuntimeException {
        TestFile file = new TestFile().writeTestData();
        assertTrue(admin.fileExists(file.path));
        file.assertNoOtherFiles();
    }

    @Test
    public void testFileExistExpectFailure() throws STRuntimeException {
        assertFalse(admin.fileExists("/afdsfasfasfsadfsafasafs"));
    }

    @Test
    public void testMkdirSuccessful() throws STRuntimeException {
        TestFile dir = new TestFile();
        admin.mkdir(dir.path);
        dir.assertIsDir();
        dir.assertNoOtherFiles();
    }

    @Test(expectedExceptions = STRuntimeException.class)
    public void testMkdirFails() throws STRuntimeException {
        admin.mkdir(testPath + "/does/not/exist");
        assertNoFilesExist();
    }

    @Test()
    public void testWriteFileByteArraySuccessful() throws STRuntimeException, IOException {
        TestFile file = new TestFile();
        admin.writeToFile(file.path, testData2);
        assertEquals(FileUtils.readFileToByteArray(file.realFile), testData2);
        file.assertNoOtherFiles();
    }

    @Test
    public void testWriteFileStreamSuccessful() throws STRuntimeException, IOException {
        TestFile file = new TestFile();
        admin.writeToFile(file.path, new ByteArrayInputStream(testData2));
        assertEquals(FileUtils.readFileToByteArray(file.realFile), testData2);
        file.assertNoOtherFiles();
    }

    @Test(expectedExceptions = STRuntimeException.class, dependsOnMethods = {"testWriteFileByteArraySuccessful"})
    public void testWriteFileFails() throws STRuntimeException {
        admin.writeToFile(testPath + "/does/not/exist", new ByteArrayInputStream(testData2));
        assertNoFilesExist();
    }

    @Test
    public void testGetShaOnExistingFile() throws STRuntimeException {
        TestFile file = new TestFile().writeTestData();
        assertEquals(admin.getFileSha1(file.path), testData1Sha);
    }

    @Test(expectedExceptions = STRuntimeException.class, dependsOnMethods = "testGetShaOnExistingFile")
    public void testGetShaOnNonExistingFile() throws STRuntimeException {
        admin.getFileSha1("/afsdfasdfsda/fsdafdsafdsa");
    }

    @Test(dependsOnMethods = "testWriteFileByteArraySuccessful")
    public void testDeleteExistingFile() throws IOException, STRuntimeException {
        TestFile file = new TestFile().writeTestData();
        admin.deleteFile(file.path, false);
        assertNoFilesExist();
    }

    @Test(dependsOnMethods = "testMkdirSuccessful")
    public void testDeleteEmptyDir() throws IOException, STRuntimeException {
        TestFile file = new TestFile().mkdir().assertExists();
        admin.deleteFile(file.path, false);
        file.assertAbsent();
    }

    @Test(expectedExceptions = STRuntimeException.class, dependsOnMethods = {"testDeleteEmptyDir", "testMkdirSuccessful", "testWriteFileByteArraySuccessful"})
    public void testDeleteNonEmptyDir() throws IOException, STRuntimeException {
        TestFile dir = new TestFile().mkdir().createChild();
        admin.deleteFile(dir.path, false);
    }

    @Test(dependsOnMethods = {"testDeleteEmptyDir", "testMkdirSuccessful", "testWriteFileByteArraySuccessful"})
    public void testDeleteNonEmptyDirRecursive() throws IOException, STRuntimeException {
        TestFile dir = new TestFile().mkdir().createChild();
        admin.deleteFile(dir.path, true);
        dir.assertAbsent();
    }

    @Test(dependsOnMethods = "testWriteFileByteArraySuccessful")
    public void testFileInfoOnExistingFile() throws STRuntimeException, IOException {
        TestFile file = new TestFile().writeTestData();
        compare(file);
    }

    @Test(expectedExceptions = STRuntimeException.class, dependsOnMethods = "testFileInfoOnExistingFile")
    public void testFileInfoOnNonExistingFile() throws STRuntimeException {
        admin.getFileInfo("/dfsafdsafdasfadsfdsa");
    }

    @Test(dependsOnMethods = "testMkdirSuccessful")
    public void testFileInfoOnExistingDir() throws STRuntimeException, IOException {
        TestFile file = new TestFile().mkdir();
        compare(file);
    }

    @Test(dependsOnMethods = {"testWriteFileByteArraySuccessful", "testExecSuccessfulWithLogging",
            "testFileEmptyExistExpectSuccess", "testMkdirSuccessful"})
    public void testCreateSymlink() throws STRuntimeException {
        String path = createAltTestDir();
        TestFile target = new TestFile().writeTestData();
        String name = path + "/" + rndName();
        admin.createSymlink(name, target.path);
        assertTrue(admin.fileExists(name));
        assertEquals(admin.exec("readlink " + name).trim(), target.path);
    }

    @Test(dependsOnMethods = "testExecSuccessfulWithoutException")
    public void testFileInfoOnSymlink() throws STRuntimeException, IOException {
        String path = createAltTestDir();
        admin.exec("ln -s /test " + path + "/testsymlink");
        FileInfo fileInfo = admin.getFileInfo(path + "/testsymlink");
        assertEquals(fileInfo.getType(), SYMLINK);
        assertEquals(fileInfo.getLinkTarget(), "/test");
    }

    @Test
    public void testEchoSetVariable() throws STRuntimeException {
        final String key = "TESTVAL";
        final String value = "yay";
        final String keyvalue = key + "=" + value;
        String stdout;
        HashMap<String, String> env = new HashMap<>();
        env.put(key, value);
        stdout = admin.exec("env", env);
        assertTrue(stdout.contains(keyvalue));
        ExecutionResult res = admin.exec("env", null, YES, env);
        assertEquals(res.getRetCode(), 0);
        assertTrue(res.getOutput().contains(keyvalue));
        assertTrue(stdout.contains(keyvalue));
        stdout = admin.exec("env");
        assertFalse(stdout.contains(keyvalue));
    }

    private void compare(TestFile file) throws STRuntimeException, IOException {
        String path = file.path;
        Path realPath = file.realPath;
        FileInfo fileInfo = admin.getFileInfo(path);
        Map<String, Object> attrs = Files.readAttributes(realPath, "*", NOFOLLOW_LINKS);
        assertNotNull(fileInfo);
        assertEquals(fileInfo.getPath(), path);
        assertEquals(fileInfo.getGroup(), fileUser);
        assertEquals(fileInfo.getOwner(), fileUser);
        if (Files.isDirectory(realPath, NOFOLLOW_LINKS)) {
            assertEquals(fileInfo.getType(), DIRECTORY);
            assertNull(fileInfo.getLinkTarget());
        } else if (Files.isRegularFile(realPath, NOFOLLOW_LINKS)) {
            assertEquals(fileInfo.getType(), FILE);
            assertNull(fileInfo.getLinkTarget());
            assertEquals(fileInfo.getSize(), Files.size(realPath));
        } else if (Files.isSymbolicLink(realPath)) {
            assertEquals(fileInfo.getType(), SYMLINK);
            assertEquals(fileInfo.getLinkTarget(), file.target);
        } else {
            assertEquals(fileInfo.getType(), OTHER);
            assertNull(fileInfo.getLinkTarget());
        }
        String expectedPerms = PosixFilePermissions.toString(Files.getPosixFilePermissions(realPath, NOFOLLOW_LINKS));
        if (admin instanceof SshHost) {
            expectedPerms = (fileInfo.getType() == DIRECTORY ? "d" : "-") + expectedPerms;
        }
        assertEquals(fileInfo.getPermissions(), expectedPerms);
        assertEquals(fileInfo.getModified(), ((FileTime) attrs.get("lastModifiedTime")).toMillis());
    }

    private void assertNoFilesExist() {
        File[] childrens = realTestDir.listFiles();
        assertTrue(childrens == null || childrens.length == 0);
    }

    private static String rndName() {
        return StringUtils.urlEncode(UUID.randomUUID().toString());
    }

    private String createAltTestDir() throws STRuntimeException {
        if (admin instanceof SshHost) {
            if (!admin.fileExists("/test2")) {
                admin.mkdir("/test2");
            }
            admin.exec("rm -rf /test2/*");
            return "/test2";
        } else {
            return realTestDir.getAbsolutePath();
        }
    }

    class TestFile {
        String name = rndName();
        String path;
        File realFile;
        Path realPath;
        String target;

        TestFile() {
            path = testPath + "/" + name;
            realFile = new File(realTestDir, name);
            realPath = realFile.toPath();
        }

        TestFile(TestFile parent) {
            path = parent.path + "/" + name;
            realFile = new File(parent.realFile, name);
            realPath = realFile.toPath();
        }

        public boolean exists() {
            return realFile.exists();
        }

        public TestFile assertExists() {
            assertTrue(exists());
            return this;
        }

        public TestFile assertAbsent() {
            assertFalse(exists());
            return this;
        }

        public TestFile assertIsDir() {
            assertExists();
            assertTrue(realFile.isDirectory());
            return this;
        }

        public TestFile assertIsFile() {
            assertExists();
            assertTrue(realFile.isFile());
            return this;
        }

        public TestFile mkdir() throws STRuntimeException {
            admin.mkdir(path);
            return this;
        }

        public TestFile writeTestData() throws STRuntimeException {
            admin.writeToFile(path, testData1);
            return this;
        }

        public TestFile writeEmpty() throws STRuntimeException {
            admin.writeToFile(path, new byte[0]);
            return this;
        }

        public TestFile assertNoOtherFiles() {
            File[] files = realFile.listFiles();
            if (files != null) {
                for (File file : files) {
                    assertEquals(file.getName(), name);
                }
            }
            return this;
        }

        public TestFile createChild() throws STRuntimeException {
            TestFile children = new TestFile(this);
            children.writeTestData();
            return this;
        }

        public TestFile mksymlink(TestFile target) throws STRuntimeException {
            admin.createSymlink(path, target.name);
            this.target = target.name;
            return this;
        }
    }

    public enum Type {
        LOCAL, SSH_SUDO, SSH_ROOT
    }
}
