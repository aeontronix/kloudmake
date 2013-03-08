/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.systyrant.service.host;

import com.kloudtek.systyrant.ExecutionResult;
import com.kloudtek.systyrant.FileInfo;
import com.kloudtek.systyrant.exception.STRuntimeException;
import com.kloudtek.systyrant.resource.builtin.core.FilePermissions;
import com.kloudtek.util.TempFile;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.lang.reflect.Field;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFilePermissions;
import java.nio.file.attribute.UserPrincipalLookupService;
import java.util.Map;

import static com.kloudtek.util.CryptoUtils.sha1;
import static java.nio.file.LinkOption.NOFOLLOW_LINKS;

public class LocalHost extends AbstractHost {
    private String currentUser = System.getProperty("user.name");

    public LocalHost() {
        execPrefix = "bash -c '";
        execSuffix = "'";
    }

    @Override
    public ExecutionResult execScript(String script, ScriptType type, long timeout, @Nullable Integer expectedRetCode, Logging logging, String user) throws STRuntimeException {
        try {
            try (TempFile temp = new TempFile("sts")) {
                FileUtils.write(temp, script);
                switch (type) {
                    case BASH:
                        return exec("bash " + temp.getAbsolutePath(), timeout, expectedRetCode, logging, null);
                    default:
                        throw new STRuntimeException("Unsupported script type: " + type.toString());
                }
            }
        } catch (IOException e) {
            throw new STRuntimeException("failed to execute script: " + e.getLocalizedMessage());
        }
    }

    @Override
    public boolean fileExists(String path) {
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
    public FileInfo getFileInfo(String path) throws STRuntimeException {
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
            throw new STRuntimeException("Faile to get file info for " + path + ": " + e.getLocalizedMessage());
        }
        return fileInfo;
    }

    @Override
    public boolean mkdir(String path) throws STRuntimeException {
        File file = new File(path);
        if (!file.exists()) {
            if (!file.mkdir()) {
                throw new STRuntimeException("Unable to create directory " + path);
            }
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean mkdirs(String path) throws STRuntimeException {
        File file = new File(path);
        if (!file.exists()) {
            if (!file.mkdirs()) {
                throw new STRuntimeException("Unable to create directory " + path);
            }
            return true;
        } else {
            return false;
        }
    }

    @Override
    public byte[] getFileSha1(String path) throws STRuntimeException {
        try (FileInputStream is = new FileInputStream(path)) {
            return sha1(is);
        } catch (IOException e) {
            throw new STRuntimeException("Error occured while reading " + path, e);
        }
    }

    @Override
    public void writeToFile(String path, byte[] data) throws STRuntimeException {
        try {
            try (FileOutputStream w = new FileOutputStream(path)) {
                w.write(data);
            }
        } catch (IOException e) {
            throw new STRuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public void writeToFile(String path, InputStream data) throws STRuntimeException {
        try {
            try (FileOutputStream w = new FileOutputStream(path)) {
                IOUtils.copy(data, w);
            }
        } catch (IOException e) {
            throw new STRuntimeException("Unable to write file " + path + ": " + e.getMessage(), e);
        }
    }

    @Override
    public byte[] readFile(String path) throws STRuntimeException {
        try {
            return FileUtils.readFileToByteArray(new File(path));
        } catch (IOException e) {
            throw new STRuntimeException("Unable to read file " + path + ": " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteFile(String path, boolean recursive) throws STRuntimeException {
        File file = new File(path);
        if (file.exists()) {
            if (file.isFile() || !recursive) {
                if (!file.delete()) {
                    throw new STRuntimeException("Unable to delete file " + path);
                }
            } else {
                try {
                    FileUtils.deleteDirectory(file);
                } catch (IOException e) {
                    throw new STRuntimeException("Unable to delete directory " + path);
                }
            }
        }
    }

    @Override
    public void createSymlink(String path, String target) throws STRuntimeException {
        try {
            Files.createSymbolicLink(new File(path).toPath(), new File(target).toPath());
        } catch (IOException e) {
            throw new STRuntimeException("Unable to create symlink " + path + ": " + e.getLocalizedMessage());
        }
    }

    @Override
    public void setFileOwner(String path, String owner) throws STRuntimeException {
        try {
            UserPrincipalLookupService lookupService = FileSystems.getDefault().getUserPrincipalLookupService();
            Files.setOwner(new File(path).toPath(), lookupService.lookupPrincipalByName(owner));
        } catch (IOException e) {
            throw new STRuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public void setFileGroup(String path, String group) throws STRuntimeException {
        try {
            Path jpath = new File(path).toPath();
            PosixFileAttributeView view = Files.getFileAttributeView(jpath, PosixFileAttributeView.class);
            UserPrincipalLookupService lookupService = FileSystems.getDefault().getUserPrincipalLookupService();
            view.setGroup(lookupService.lookupPrincipalByGroupName(group));
        } catch (IOException e) {
            throw new STRuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public void setFilePerms(String path, FilePermissions perms) throws STRuntimeException {
        try {
            Files.setPosixFilePermissions(Paths.get(path), PosixFilePermissions.fromString(perms.toString()));
        } catch (IOException e) {
            throw new STRuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public String createTempDir() throws STRuntimeException {
        try {
            File tempFile = File.createTempFile("sttmpdir", "tmp");
            if (!tempFile.delete()) {
                throw new STRuntimeException("Unable to create temporary directory: failed to delete tmp file " + tempFile.getPath());
            }
            if (!tempFile.mkdirs()) {
                throw new STRuntimeException("Unable to create temporary directory " + tempFile.getPath());
            }
            String tempdir = tempFile.getPath();
            tempDirs.add(tempdir);
            return tempdir;
        } catch (IOException e) {
            throw new STRuntimeException("Unable to create temporary directory: " + e.getMessage(), e);
        }
    }

    @Override
    public String createTempFile() throws STRuntimeException {
        try {
            String tmpfile = File.createTempFile("sttmpdir", "tmp").getPath();
            tempFiles.add(tmpfile);
            return tmpfile;
        } catch (IOException e) {
            throw new STRuntimeException("Unable to create temporary directory: " + e.getMessage(), e);
        }
    }

    @Override
    public String getUser() {
        return System.getProperty("user.name");
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
        forceSet(host, "hostProvider", hostProvider);
        return host;
    }

    // TODO use latest ktutils instead

    public static void forceSet(Object obj, String name, Object value) {
        try {
            Field field = findField(obj, name);
            field.setAccessible(true);
            field.set(obj, value);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static void set(Object obj, String name, Object value) {
        try {
            findField(obj, name).set(obj, value);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private static Field findField(Object obj, String name) {
        Class<?> cl = obj.getClass();
        while (cl != null) {
            try {
                return cl.getDeclaredField(name);
            } catch (NoSuchFieldException e) {
                cl = cl.getSuperclass();
            }
        }
        throw new IllegalArgumentException("Field " + name + " not found in " + obj.getClass().getName());
    }

}
