/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.systyrant.service.host;

import com.kloudtek.systyrant.ExecutionResult;
import com.kloudtek.systyrant.FileInfo;
import com.kloudtek.systyrant.exception.STRuntimeException;
import com.kloudtek.util.TempFile;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Map;

import static com.kloudtek.util.CryptoUtils.sha1;
import static java.nio.file.LinkOption.NOFOLLOW_LINKS;

public class LocalHost extends AbstractHost {
    public LocalHost() {
        username = System.getProperty("user.name");
        execPrefix = "bash -c '";
        execSuffix = "'";
    }

    @Override
    public ExecutionResult execScript(String script, ScriptType type, long timeout, @Nullable Integer expectedRetCode, Logging logging, boolean includePreSuFix) throws STRuntimeException {
        try {
            try (TempFile temp = new TempFile("sts")) {
                FileUtils.write(temp, script);
                switch (type) {
                    case BASH:
                        return exec("bash " + temp.getAbsolutePath(), timeout, expectedRetCode, logging, includePreSuFix);
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
    protected CommandLine generateCommandLine(String command, boolean includePreSuFix) {
        CommandLine cmdLine = new CommandLine("/bin/bash");
        cmdLine.addArgument("-c");
        cmdLine.addArgument(command, false);
        return cmdLine;
    }
}
