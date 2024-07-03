/*
 * Copyright (c) 2024 Aeontronix Inc
 */

package com.aeontronix.aeonbuild.host;

import com.aeontronix.aeonbuild.BuildContextImpl;
import com.aeontronix.aeonbuild.exception.KMRuntimeException;
import com.aeontronix.aeonbuild.resource.core.FilePermissions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.InputStream;
import java.util.Map;

/**
 * <p>The host is an abstraction layer to access the operating system of the host you wish to perform tasks against.</p>
 * <p>
 * Each context has it's own default host ({@link BuildContextImpl#getHost()}), and each resource
 * can have a host override ({@link com.aeontronix.aeonbuild.Resource#setHostOverride(Host)}) which applies to itself
 * and any of it's childrens.
 * </p>
 * <p>
 * Hosts are only usable after they have been started ({@link #isStarted()} will return false). The Context's host
 * is started when the context is executed, and host overrides are started during that resource's execute stage (before
 * any tasks are executed).
 * </p>
 */
public interface Host {
    int DEFAULT_TIMEOUT = 300000;

    /**
     * Start this host.
     * @throws KMRuntimeException If an error occurs
     */
    void start() throws KMRuntimeException;

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
     * @throws KMRuntimeException If an error occurs while running the script.
     */
    ExecutionResult execScript(String script, ScriptType type, long timeout, @Nullable Integer expectedRetCode, Logging logging, String user) throws KMRuntimeException;

    String exec(String command) throws KMRuntimeException;

    String exec(String command, String workdir) throws KMRuntimeException;

    String exec(String command, Map<String, String> env) throws KMRuntimeException;

    @NotNull
    ExecutionResult exec(String command, @Nullable Integer expectedRetCode, Logging logging) throws KMRuntimeException;

    @NotNull
    ExecutionResult exec(String command, @Nullable Integer expectedRetCode, Logging logging, @Nullable Map<String, String> env) throws KMRuntimeException;

    @NotNull
    ExecutionResult exec(String command, @Nullable Long timeout, @Nullable Integer expectedRetCode, Logging logging, String user) throws KMRuntimeException;

    @NotNull
    ExecutionResult exec(String command, @Nullable Long timeout, @Nullable Integer expectedRetCode, Logging logging, String user, String workdir, Map<String, String> env) throws KMRuntimeException;

    boolean fileExists(String path) throws KMRuntimeException;

    String getFilePathSeparator() throws KMRuntimeException;

    /**
     * Retrieve file details.
     * Please note this will throw an exception if the file doesn't exist. If you are not sure of the file presence,
     * please use {@link #fileExists(String)} first.
     *
     * @param path DataFile path
     * @return {@link FileInfo} object.
     * @throws KMRuntimeException If an error occured while retrieving file details, or if the file doesn't exist.
     */
    @NotNull
    FileInfo getFileInfo(String path) throws KMRuntimeException;

    boolean mkdir(String path) throws KMRuntimeException;

    boolean mkdirs(String path) throws KMRuntimeException;

    /**
     * get a SHA1 checkum of a file.
     *
     * @param path file to calculate checksum from.
     * @return SHA1 digest
     * @throws KMRuntimeException If an error occurs calculating SHA1 checksum.
     */
    byte[] getFileSha1(String path) throws KMRuntimeException;

    byte[] readFileData(String path) throws KMRuntimeException;

    InputStream readFile(String path) throws KMRuntimeException;

    String readTextFile(String path, String encoding) throws KMRuntimeException;

    String readTextFile(String path) throws KMRuntimeException;

    void writeToFile(String path, String data) throws KMRuntimeException;

    void writeToFile(String path, byte[] data) throws KMRuntimeException;

    void writeToFile(String path, InputStream data) throws KMRuntimeException;

    String exec(String command, Logging logging) throws KMRuntimeException;

    void deleteFile(String path, boolean recursive) throws KMRuntimeException;

    void createSymlink(String path, String target) throws KMRuntimeException;

    void setFileOwner(String path, String owner) throws KMRuntimeException;

    void setFileGroup(String path, String group) throws KMRuntimeException;

    void setFilePerms(String path, FilePermissions perms) throws KMRuntimeException;

    String createTempDir() throws KMRuntimeException;

    String createTempFile() throws KMRuntimeException;

    boolean fileIsSame(@NotNull String path, @NotNull String content) throws KMRuntimeException;

    void setState(String id, Object state);

    Object getState(String id);

    Map<String, Object> getState();

    HostProvider getMetadata() throws KMRuntimeException;

    enum Logging {
        NO, YES, ON_ERROR
    }

    enum ScriptType {
        BASH
    }
}
