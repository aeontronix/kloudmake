/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.kloudmake.host;

import com.kloudtek.kloudmake.exception.STRuntimeException;
import com.kloudtek.kloudmake.resource.core.FilePermissions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.InputStream;
import java.util.Map;

/**
 * <p>The host is an abstraction layer to access the operating system of the host you wish to perform tasks against.</p>
 * <p>
 * Each context has it's own default host ({@link com.kloudtek.kloudmake.STContext#getHost()}), and each resource
 * can have a host override ({@link com.kloudtek.kloudmake.Resource#setHostOverride(Host)}) which applies to itself
 * and any of it's childrens.
 * </p>
 * <p>
 * Hosts are only usable after they have been started ({@link #isStarted()} will return false). The Context's host
 * is started when the context is executed, and host overrides are started during that resource's execute stage (before
 * any tasks are executed).
 * </p>
 */
public interface Host {
    public static final int DEFAULT_TIMEOUT = 300000;

    /**
     * Start this host.
     */
    void start() throws STRuntimeException;

    /**
     * Shutdown and release resources.
     */
    void close();

    /**
     * Checks if this host has been started.
     *
     * @return True if the host is started, or false otherwise.
     */
    boolean isStarted();

    /**
     * This method is used to execute a shell script
     *
     * @param script          Shell script to execute
     * @param type            Script type
     * @param timeout         How long to wait for execution before timing out (in millis)
     * @param expectedRetCode Optional expected return code (If specified and the return code is different, it will
     *                        throw a STRuntimeException
     * @param logging         Specify if the script output should be logged all the time, never or only if the script fails
     * @param user            Use to run the script as
     * @return Execution result
     * @throws STRuntimeException If an error occurs while running the script.
     */
    ExecutionResult execScript(String script, ScriptType type, long timeout, @Nullable Integer expectedRetCode, Logging logging, String user) throws STRuntimeException;

    String exec(String command) throws STRuntimeException;

    String exec(String command, String workdir) throws STRuntimeException;

    String exec(String command, Map<String, String> env) throws STRuntimeException;

    @NotNull
    ExecutionResult exec(String command, @Nullable Integer expectedRetCode, Logging logging) throws STRuntimeException;

    @NotNull
    ExecutionResult exec(String command, @Nullable Integer expectedRetCode, Logging logging, @Nullable Map<String, String> env) throws STRuntimeException;

    @NotNull
    ExecutionResult exec(String command, @Nullable Long timeout, @Nullable Integer expectedRetCode, Logging logging, String user) throws STRuntimeException;

    @NotNull
    ExecutionResult exec(String command, @Nullable Long timeout, @Nullable Integer expectedRetCode, Logging logging, String user, String workdir, Map<String, String> env) throws STRuntimeException;

    boolean fileExists(String path) throws STRuntimeException;

    String getFilePathSeparator() throws STRuntimeException;

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

    byte[] readFileData(String path) throws STRuntimeException;

    InputStream readFile(String path) throws STRuntimeException;

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

    HostProvider getMetadata() throws STRuntimeException;

    public enum Logging {
        NO, YES, ON_ERROR
    }

    public enum ScriptType {
        BASH
    }
}
