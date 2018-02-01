/*
 * Copyright (c) 2015. Kelewan Technologies Ltd
 */

package com.kloudtek.kloudmake.host;

import com.kloudtek.kloudmake.exception.KMRuntimeException;
import com.kloudtek.kloudmake.resource.core.FilePermissions;
import com.kloudtek.kloudmake.util.ReflectionHelper;
import com.kloudtek.kryptotek.DigestUtils;
import com.kloudtek.util.FileUtils;
import com.kloudtek.util.TempFile;
import com.kloudtek.util.io.IOUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFilePermissions;
import java.nio.file.attribute.UserPrincipalLookupService;
import java.util.Map;

import static java.nio.file.LinkOption.NOFOLLOW_LINKS;

public class LocalHost extends AbstractHost {
    private String currentUser = System.getProperty("user.name");

    public LocalHost() {
    }

    @Override
    public ExecutionResult execScript(String script, ScriptType type, long timeout, @Nullable Integer expectedRetCode, Logging logging, String user) throws KMRuntimeException {
        try {
            try (TempFile temp = new TempFile("sts")) {
                FileUtils.write(temp, script);
                switch (type) {
                    case BASH:
                        return exec("bash " + temp.getAbsolutePath(), timeout, expectedRetCode, logging, null);
                    default:
                        throw new KMRuntimeException("Unsupported script type: " + type.toString());
                }
            }
        } catch (IOException e) {
            throw new KMRuntimeException("failed to execute script: " + e.getLocalizedMessage());
        }
    }

    @Override
    public boolean fileExists(String path) throws KMRuntimeException {
        return new File(path).exists();
    }

    @Override
    public String getFilePathSeparator() {
        return File.separator;
    }

    /**
     * {@inheritDoc}
     */
    @NotNull
    @Override
    public FileInfo getFileInfo(String path) throws KMRuntimeException {
        FileInfo fileInfo = new FileInfo(path);
        try {
            File file = new File(path);
            Path filePath = file.toPath();
            Map<String, Object> attrs = Files.readAttributes(filePath, "posix:*", NOFOLLOW_LINKS);
            fileInfo.setGroup(attrs.get("group").toString());
            fileInfo.setOwner(attrs.get("owner").toString());
            fileInfo.setPermissions(PosixFilePermissions.toString(Files.getPosixFilePermissions(filePath, NOFOLLOW_LINKS)));
            fileInfo.setModified(Files.getLastModifiedTime(filePath, NOFOLLOW_LINKS).toMillis());
            if ((Boolean) attrs.get("isDirectory")) {
                fileInfo.setType(FileInfo.Type.DIRECTORY);
                fileInfo.setSize(Files.size(filePath));
            } else if ((Boolean) attrs.get("isRegularFile")) {
                fileInfo.setType(FileInfo.Type.FILE);
                fileInfo.setSize(Files.size(filePath));
            } else if ((Boolean) attrs.get("isSymbolicLink")) {
                fileInfo.setType(FileInfo.Type.SYMLINK);
                fileInfo.setLinkTarget(Files.readSymbolicLink(filePath).toString());
            } else {
                fileInfo.setType(FileInfo.Type.OTHER);
            }
        } catch (IOException e) {
            throw new KMRuntimeException("Faile to get file info for " + path + ": " + e.getLocalizedMessage());
        }
        return fileInfo;
    }

    @Override
    public boolean mkdir(String path) throws KMRuntimeException {
        File file = new File(path);
        if (!file.exists()) {
            if (!file.mkdir()) {
                throw new KMRuntimeException("Unable to create directory " + path);
            }
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean mkdirs(String path) throws KMRuntimeException {
        File file = new File(path);
        if (!file.exists()) {
            if (!file.mkdirs()) {
                throw new KMRuntimeException("Unable to create directory " + path);
            }
            return true;
        } else {
            return false;
        }
    }

    @Override
    public byte[] getFileSha1(@NotNull String path) throws KMRuntimeException {
        try (FileInputStream is = new FileInputStream(path)) {
            return DigestUtils.sha1(is);
        } catch (IOException e) {
            throw new KMRuntimeException("Error occured while reading " + path, e);
        }
    }

    @Override
    public void writeToFile(@NotNull String path, @NotNull byte[] data) throws KMRuntimeException {
        try {
            try (FileOutputStream w = new FileOutputStream(path)) {
                w.write(data);
            }
        } catch (IOException e) {
            throw new KMRuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public void writeToFile(@NotNull String path, @NotNull InputStream data) throws KMRuntimeException {
        try {
            try (FileOutputStream w = new FileOutputStream(path)) {
                IOUtils.copy(data, w);
            }
        } catch (IOException e) {
            throw new KMRuntimeException("Unable to write file " + path + ": " + e.getMessage(), e);
        }
    }

    @Override
    public byte[] readFileData(@NotNull String path) throws KMRuntimeException {
        try {
            return FileUtils.toByteArray(new File(path));
        } catch (IOException e) {
            throw new KMRuntimeException("Unable to read file " + path + ": " + e.getMessage(), e);
        }
    }

    @Override
    public InputStream readFile(@NotNull String path) throws KMRuntimeException {
        try {
            return new FileInputStream(path);
        } catch (IOException e) {
            throw new KMRuntimeException("Unable to read file " + path + ": " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteFile(@NotNull String path, boolean recursive) throws KMRuntimeException {
        File file = new File(path);
        if (file.exists()) {
            if (file.isFile() || !recursive) {
                if (!file.delete()) {
                    throw new KMRuntimeException("Unable to delete file " + path);
                }
            } else {
                try {
                    FileUtils.delete(file);
                } catch (IOException e) {
                    throw new KMRuntimeException("Unable to delete directory " + path);
                }
            }
        }
    }

    @Override
    public void createSymlink(String path, String target) throws KMRuntimeException {
        try {
            Files.createSymbolicLink(new File(path).toPath(), new File(target).toPath());
        } catch (IOException e) {
            throw new KMRuntimeException("Unable to create symlink " + path + ": " + e.getLocalizedMessage());
        }
    }

    @Override
    public void setFileOwner(String path, String owner) throws KMRuntimeException {
        try {
            UserPrincipalLookupService lookupService = FileSystems.getDefault().getUserPrincipalLookupService();
            Files.setOwner(new File(path).toPath(), lookupService.lookupPrincipalByName(owner));
        } catch (IOException e) {
            throw new KMRuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public void setFileGroup(String path, String group) throws KMRuntimeException {
        try {
            Path jpath = new File(path).toPath();
            PosixFileAttributeView view = Files.getFileAttributeView(jpath, PosixFileAttributeView.class);
            UserPrincipalLookupService lookupService = FileSystems.getDefault().getUserPrincipalLookupService();
            view.setGroup(lookupService.lookupPrincipalByGroupName(group));
        } catch (IOException e) {
            throw new KMRuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public void setFilePerms(String path, FilePermissions perms) throws KMRuntimeException {
        try {
            Files.setPosixFilePermissions(Paths.get(path), PosixFilePermissions.fromString(perms.toString()));
        } catch (IOException e) {
            throw new KMRuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public String createTempDir() throws KMRuntimeException {
        try {
            File tempFile = File.createTempFile("sttmpdir", "tmp");
            if (!tempFile.delete()) {
                throw new KMRuntimeException("Unable to create temporary directory: failed to delete tmp file " + tempFile.getPath());
            }
            if (!tempFile.mkdirs()) {
                throw new KMRuntimeException("Unable to create temporary directory " + tempFile.getPath());
            }
            String tempdir = tempFile.getPath();
            tempDirs.add(tempdir);
            return tempdir;
        } catch (IOException e) {
            throw new KMRuntimeException("Unable to create temporary directory: " + e.getMessage(), e);
        }
    }

    @Override
    public String createTempFile() throws KMRuntimeException {
        try {
            String tmpfile = File.createTempFile("sttmpdir", "tmp").getPath();
            tempFiles.add(tmpfile);
            return tmpfile;
        } catch (IOException e) {
            throw new KMRuntimeException("Unable to create temporary directory: " + e.getMessage(), e);
        }
    }

    @Override
    public String getDefaultUser() {
        return currentUser;
    }

    @Override
    protected boolean execSupportsWorkDir() {
        return true;
    }

    @Override
    public String getCurrentUser() {
        return currentUser;
    }

    public static LocalHost createStandalone() {
        LocalHost host = new LocalHost();
        HostProvider hostProvider;
        switch (OperatingSystem.getSystemOS()) {
            case OSX:
                hostProvider = new OSXMetadataProvider();
                break;
            default:
                hostProvider = new LinuxMetadataProvider();
                break;
        }
        ReflectionHelper.set(host, "hostProvider", hostProvider);
        return host;
    }

    // TODO use latest ktutils instead

    @Override
    public String toString() {
        return "Local Host #" + hashCode();
    }
}
