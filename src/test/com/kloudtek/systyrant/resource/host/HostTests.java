/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.systyrant.resource.host;

import com.jcraft.jsch.JSchException;
import com.kloudtek.systyrant.ExecutionResult;
import com.kloudtek.systyrant.FileInfo;
import com.kloudtek.systyrant.TestVagrantRuntime;
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
    private String fileGroup;
    private Host host;
    private File realTestDir;
    private String testPath;
    private byte[] testData1 = new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
    private byte[] testData1Sha = CryptoUtils.sha1(testData1);
    private byte[] testData2 = new byte[]{1, 2, 3, 4, 5, 6};

    public HostTests() {
        this(Type.SSH_SUDO);
    }

    public HostTests(Type type) {
        this.type = type;
    }

    @BeforeClass
    public void setup() throws IOException, JSchException {
        if (type == Type.LOCAL) {
            setup(LocalHost.createStandalone(), System.clearProperty("user.name"));
        } else {
            TestVagrantRuntime testVagrantRuntime = new TestVagrantRuntime();
            setup(testVagrantRuntime.getSshHost(), type == Type.SSH_ROOT ? "root" : "vagrant");
        }
    }

    private void setup(Host admin, String fileUser) throws IOException {
        this.host = admin;
        this.fileUser = fileUser;
        String os = System.getProperty("os.name");
        switch (os) {
            case "Mac OS X":
                fileGroup = "staff";
                break;
            default:
                fileGroup = fileUser;
        }
        realTestDir = new File("_test");
        if (!realTestDir.exists()) {
            if (!realTestDir.mkdirs()) {
                throw new IOException("Unable to create test dir");
            }
        }

        if (admin instanceof LocalHost) {
            testPath = realTestDir.getAbsolutePath();
        } else {
            testPath = "/tmp";
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
        ExecutionResult result = host.exec("ls " + file.path, 0, YES);
        Assert.assertEquals(normalize(result.getOutput().trim()), file.path);
        Assert.assertEquals(result.getRetCode(), 0);
        file.assertNoOtherFiles();
    }

    @Test(dependsOnMethods = "testWriteFileByteArraySuccessful")
    public void testExecSuccessfulWithoutException() throws STRuntimeException {
        TestFile file = new TestFile().writeTestData();
        ExecutionResult result = host.exec("ls " + file.path, null, YES);
        Assert.assertEquals(normalize(result.getOutput().trim()), file.path);
        Assert.assertEquals(result.getRetCode(), 0);
        file.assertNoOtherFiles();
    }

    @Test(expectedExceptions = STRuntimeException.class)
    public void testExecUnsuccessfulWithException() throws STRuntimeException {
        host.exec("ls sfdafadsfsda");
    }

    @Test()
    public void testExecUnsuccessfulWithRetCode() throws STRuntimeException {
        ExecutionResult result = host.exec("ls asffdsa", null, YES);
        assertTrue(result.getRetCode() != 0);
    }

    @Test(dependsOnMethods = "testWriteFileByteArraySuccessful")
    public void testExecScript() throws IOException, STRuntimeException {
        TestFile file1 = new TestFile().writeTestData();
        TestFile file2 = new TestFile().writeTestData();
        String script = "#!/bin/bash\nls " + file1.path + "\nls  " + file2.path;
        ExecutionResult result = host.execScript(script, Host.ScriptType.BASH, Host.DEFAULT_TIMEOUT, 0, YES, null);
        Assert.assertEquals(normalize(result.getOutput().trim()), file1.path.toString() + file2.path.toString());
        Assert.assertEquals(result.getRetCode(), 0);
    }

    private String normalize(String str) {
        StringBuilder txt = new StringBuilder();
        for (char c : str.toCharArray()) {
            if (!Character.isWhitespace(c)) {
                txt.append(c);
            }
        }
        return txt.toString();
    }

    @Test(dependsOnMethods = "testWriteFileByteArraySuccessful")
    public void testFileEmptyExistExpectSuccess() throws STRuntimeException {
        TestFile file = new TestFile().writeEmpty();
        assertTrue(host.fileExists(file.path));
        file.assertNoOtherFiles();
    }

    @Test(dependsOnMethods = "testWriteFileByteArraySuccessful")
    public void testNonEmptyFileExistExpectSuccess() throws STRuntimeException {
        TestFile file = new TestFile().writeTestData();
        assertTrue(host.fileExists(file.path));
        file.assertNoOtherFiles();
    }

    @Test
    public void testFileExistExpectFailure() throws STRuntimeException {
        assertFalse(host.fileExists("/afdsfasfasfsadfsafasafs"));
    }

    @Test
    public void testMkdirSuccessful() throws STRuntimeException {
        TestFile dir = new TestFile();
        host.mkdir(dir.path);
        dir.assertIsDir();
        dir.assertNoOtherFiles();
    }

    @Test(expectedExceptions = STRuntimeException.class)
    public void testMkdirFails() throws STRuntimeException {
        host.mkdir(testPath + "/does/not/exist");
        assertNoFilesExist();
    }

    @Test()
    public void testWriteFileByteArraySuccessful() throws STRuntimeException, IOException {
        TestFile file = new TestFile();
        host.writeToFile(file.path, testData2);
        assertEquals(FileUtils.readFileToByteArray(file.realFile), testData2);
        file.assertNoOtherFiles();
    }

    @Test
    public void testWriteFileStreamSuccessful() throws STRuntimeException, IOException {
        TestFile file = new TestFile();
        host.writeToFile(file.path, new ByteArrayInputStream(testData2));
        assertEquals(FileUtils.readFileToByteArray(file.realFile), testData2);
        file.assertNoOtherFiles();
    }

    @Test(expectedExceptions = STRuntimeException.class, dependsOnMethods = {"testWriteFileByteArraySuccessful"})
    public void testWriteFileFails() throws STRuntimeException {
        host.writeToFile(testPath + "/does/not/exist", new ByteArrayInputStream(testData2));
        assertNoFilesExist();
    }

    @Test
    public void testGetShaOnExistingFile() throws STRuntimeException {
        TestFile file = new TestFile().writeTestData();
        assertEquals(host.getFileSha1(file.path), testData1Sha);
    }

    @Test(expectedExceptions = STRuntimeException.class, dependsOnMethods = "testGetShaOnExistingFile")
    public void testGetShaOnNonExistingFile() throws STRuntimeException {
        host.getFileSha1("/afsdfasdfsda/fsdafdsafdsa");
    }

    @Test(dependsOnMethods = "testWriteFileByteArraySuccessful")
    public void testDeleteExistingFile() throws IOException, STRuntimeException {
        TestFile file = new TestFile().writeTestData();
        host.deleteFile(file.path, false);
        assertNoFilesExist();
    }

    @Test(dependsOnMethods = "testMkdirSuccessful")
    public void testDeleteEmptyDir() throws IOException, STRuntimeException {
        TestFile file = new TestFile().mkdir().assertExists();
        host.deleteFile(file.path, false);
        file.assertAbsent();
    }

    @Test(expectedExceptions = STRuntimeException.class, dependsOnMethods = {"testDeleteEmptyDir", "testMkdirSuccessful", "testWriteFileByteArraySuccessful"})
    public void testDeleteNonEmptyDir() throws IOException, STRuntimeException {
        TestFile dir = new TestFile().mkdir().createChild();
        host.deleteFile(dir.path, false);
    }

    @Test(dependsOnMethods = {"testDeleteEmptyDir", "testMkdirSuccessful", "testWriteFileByteArraySuccessful"})
    public void testDeleteNonEmptyDirRecursive() throws IOException, STRuntimeException {
        TestFile dir = new TestFile().mkdir().createChild();
        host.deleteFile(dir.path, true);
        dir.assertAbsent();
    }

    @Test(dependsOnMethods = "testWriteFileByteArraySuccessful")
    public void testFileInfoOnExistingFile() throws STRuntimeException, IOException {
        TestFile file = new TestFile().writeTestData();
        compare(file);
    }

    @Test(expectedExceptions = STRuntimeException.class, dependsOnMethods = "testFileInfoOnExistingFile")
    public void testFileInfoOnNonExistingFile() throws STRuntimeException {
        host.getFileInfo("/dfsafdsafdasfadsfdsa");
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
        host.createSymlink(name, target.path);
        assertTrue(host.fileExists(name));
        assertEquals(host.exec("readlink " + name).trim(), target.path);
    }

    @Test(dependsOnMethods = "testExecSuccessfulWithoutException")
    public void testFileInfoOnSymlink() throws STRuntimeException, IOException {
        String path = createAltTestDir();
        host.exec("ln -s /test " + path + "/testsymlink");
        FileInfo fileInfo = host.getFileInfo(path + "/testsymlink");
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
        stdout = host.exec("env", env);
        assertTrue(stdout.contains(keyvalue));
        ExecutionResult res = host.exec("env", null, YES, env);
        assertEquals(res.getRetCode(), 0);
        assertTrue(res.getOutput().contains(keyvalue));
        assertTrue(stdout.contains(keyvalue));
        stdout = host.exec("env");
        assertFalse(stdout.contains(keyvalue));
    }

    private void compare(TestFile file) throws STRuntimeException, IOException {
        String path = file.path;
        Path realPath = file.realPath;
        FileInfo fileInfo = host.getFileInfo(path);
        Map<String, Object> attrs = Files.readAttributes(realPath, "*", NOFOLLOW_LINKS);
        assertNotNull(fileInfo);
        assertEquals(fileInfo.getPath(), path);
        assertEquals(fileInfo.getGroup(), fileGroup);
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
        if (host instanceof SshHost) {
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
        if (host instanceof SshHost) {
            if (!host.fileExists("/test2")) {
                host.mkdir("/test2");
            }
            host.exec("rm -rf /test2/*");
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
            host.mkdir(path);
            return this;
        }

        public TestFile writeTestData() throws STRuntimeException {
            host.writeToFile(path, testData1);
            return this;
        }

        public TestFile writeEmpty() throws STRuntimeException {
            host.writeToFile(path, new byte[0]);
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
            host.createSymlink(path, target.name);
            this.target = target.name;
            return this;
        }
    }

    public enum Type {
        LOCAL, SSH_SUDO, SSH_ROOT
    }
}
