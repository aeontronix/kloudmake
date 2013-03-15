/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.systyrant.host;

import com.kloudtek.systyrant.ExecutionResult;
import com.kloudtek.systyrant.FileInfo;
import com.kloudtek.systyrant.annotation.Service;
import com.kloudtek.systyrant.exception.STRuntimeException;
import com.kloudtek.systyrant.resource.builtin.core.FilePermissions;
import com.kloudtek.systyrant.service.Startable;
import com.kloudtek.systyrant.service.Stoppable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.InputStream;
import java.util.Map;

public interface Host extends Stoppable, Startable {
    public static final int DEFAULT_TIMEOUT = 300000;

    boolean isStarted();

    ExecutionResult execScript(String script, ScriptType type, long timeout, @Nullable Integer expectedRetCode, Logging logging, String user) throws STRuntimeException;

    String exec(String command) throws STRuntimeException;

    String exec(String command, Map<String, String> env) throws STRuntimeException;

    @NotNull
    ExecutionResult exec(String command, @Nullable Integer expectedRetCode, Logging logging) throws STRuntimeException;

    @NotNull
    ExecutionResult exec(String command, @Nullable Integer expectedRetCode, Logging logging, @Nullable Map<String, String> env) throws STRuntimeException;

    @NotNull
    ExecutionResult exec(String command, @Nullable Long timeout, @Nullable Integer expectedRetCode, Logging logging, String user) throws STRuntimeException;

    @NotNull
    ExecutionResult exec(String command, @Nullable Long timeout, @Nullable Integer expectedRetCode, Logging logging, String user, Map<String, String> env) throws STRuntimeException;

    boolean fileExists(String path) throws STRuntimeException;

    String getFilePathSeparator();

    /**
     * Retrieve file details.
     * Please note this will throw an exception if the file doesn't exist. If you are not sure of the file presence,
     * please use {@link #fileExists(String)} first.
     *
     * @param path DataFile path
     * @return {@link FileInfo} object.
     * @throws STRuntimeException If an error occured while retrieving file details, or if the file doesn't exist.
     */
    @NotNull
    FileInfo getFileInfo(String path) throws STRuntimeException;

    boolean mkdir(String path) throws STRuntimeException;

    boolean mkdirs(String path) throws STRuntimeException;

    /**
     * get a SHA1 checkum of a file.
     *
     * @param path file to calculate checksum from.
     * @return SHA1 digest
     * @throws STRuntimeException If an error occurs calculating SHA1 checksum.
     */
    byte[] getFileSha1(String path) throws STRuntimeException;

    byte[] readFile(String path) throws STRuntimeException;

    String readTextFile(String path, String encoding) throws STRuntimeException;

    String readTextFile(String path) throws STRuntimeException;

    void writeToFile(String path, String data) throws STRuntimeException;

    void writeToFile(String path, byte[] data) throws STRuntimeException;

    void writeToFile(String path, InputStream data) throws STRuntimeException;

    String exec(String command, Logging logging) throws STRuntimeException;

    void deleteFile(String path, boolean recursive) throws STRuntimeException;

    void createSymlink(String path, String target) throws STRuntimeException;

    void setFileOwner(String path, String owner) throws STRuntimeException;

    void setFileGroup(String path, String group) throws STRuntimeException;

    void setFilePerms(String path, FilePermissions perms) throws STRuntimeException;

    String createTempDir() throws STRuntimeException;

    String createTempFile() throws STRuntimeException;

    boolean fileIsSame(@NotNull String path, @NotNull String content) throws STRuntimeException;

    void setState(String id, Object state);

    Object getState(String id);

    Map<String, Object> getState();

    HostProvider getMetadata();

    public enum Logging {
        NO, YES, ON_ERROR
    }

    public enum ScriptType {
        BASH
    }
}
