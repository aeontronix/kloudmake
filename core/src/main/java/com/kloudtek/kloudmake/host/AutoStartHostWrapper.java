/*
 * Copyright (c) 2015. Kelewan Technologies Ltd
 */

package com.kloudtek.kloudmake.host;

import com.kloudtek.kloudmake.exception.KMRuntimeException;
import com.kloudtek.kloudmake.resource.core.FilePermissions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.InputStream;
import java.util.Map;

/**
 * Created by yannick on 17/08/13.
 */
public class AutoStartHostWrapper implements Host {
    private Host wrapped;

    public AutoStartHostWrapper(@NotNull Host wrapped) {
        this.wrapped = wrapped;
    }

    private void checkStarted() throws KMRuntimeException {
        if (!wrapped.isStarted()) {
            start();
        }
    }

    @Override
    public boolean isStarted() {
        return wrapped.isStarted();
    }

    @Override
    public void start() throws KMRuntimeException {
        wrapped.start();
    }

    @Override
    public void close() {
        wrapped.close();
    }

    @Override
    public ExecutionResult execScript(String script, ScriptType type, long timeout, @Nullable Integer expectedRetCode, Logging logging, String user) throws KMRuntimeException {
        checkStarted();
        return wrapped.execScript(script, type, timeout, expectedRetCode, logging, user);
    }

    @Override
    public String exec(String command) throws KMRuntimeException {
        checkStarted();
        return wrapped.exec(command);
    }

    @Override
    public String exec(String command, String workdir) throws KMRuntimeException {
        checkStarted();
        return wrapped.exec(command, workdir);
    }

    @Override
    public String exec(String command, Map<String, String> env) throws KMRuntimeException {
        checkStarted();
        return wrapped.exec(command, env);
    }

    @Override
    @NotNull
    public ExecutionResult exec(String command, @Nullable Integer expectedRetCode, Logging logging) throws KMRuntimeException {
        checkStarted();
        return wrapped.exec(command, expectedRetCode, logging);
    }

    @Override
    @NotNull
    public ExecutionResult exec(String command, @Nullable Integer expectedRetCode, Logging logging, @Nullable Map<String, String> env) throws KMRuntimeException {
        checkStarted();
        return wrapped.exec(command, expectedRetCode, logging, env);
    }

    @Override
    @NotNull
    public ExecutionResult exec(String command, @Nullable Long timeout, @Nullable Integer expectedRetCode, Logging logging, String user) throws KMRuntimeException {
        checkStarted();
        return wrapped.exec(command, timeout, expectedRetCode, logging, user);
    }

    @Override
    @NotNull
    public ExecutionResult exec(String command, @Nullable Long timeout, @Nullable Integer expectedRetCode, Logging logging, String user, String workdir, Map<String, String> env) throws KMRuntimeException {
        checkStarted();
        return wrapped.exec(command, timeout, expectedRetCode, logging, user, workdir, env);
    }

    @Override
    public boolean fileExists(String path) throws KMRuntimeException {
        checkStarted();
        return wrapped.fileExists(path);
    }

    @Override
    public String getFilePathSeparator() throws KMRuntimeException {
        checkStarted();
        return wrapped.getFilePathSeparator();
    }

    @Override
    @NotNull
    public FileInfo getFileInfo(String path) throws KMRuntimeException {
        checkStarted();
        return wrapped.getFileInfo(path);
    }

    @Override
    public boolean mkdir(String path) throws KMRuntimeException {
        checkStarted();
        return wrapped.mkdir(path);
    }

    @Override
    public boolean mkdirs(String path) throws KMRuntimeException {
        checkStarted();
        return wrapped.mkdirs(path);
    }

    @Override
    public byte[] getFileSha1(String path) throws KMRuntimeException {
        checkStarted();
        return wrapped.getFileSha1(path);
    }

    @Override
    public byte[] readFileData(String path) throws KMRuntimeException {
        checkStarted();
        return wrapped.readFileData(path);
    }

    @Override
    public InputStream readFile(String path) throws KMRuntimeException {
        checkStarted();
        return wrapped.readFile(path);
    }

    @Override
    public String readTextFile(String path, String encoding) throws KMRuntimeException {
        checkStarted();
        return wrapped.readTextFile(path, encoding);
    }

    @Override
    public String readTextFile(String path) throws KMRuntimeException {
        checkStarted();
        return wrapped.readTextFile(path);
    }

    @Override
    public void writeToFile(String path, String data) throws KMRuntimeException {
        checkStarted();
        wrapped.writeToFile(path, data);
    }

    @Override
    public void writeToFile(String path, byte[] data) throws KMRuntimeException {
        checkStarted();
        wrapped.writeToFile(path, data);
    }

    @Override
    public void writeToFile(String path, InputStream data) throws KMRuntimeException {
        checkStarted();
        wrapped.writeToFile(path, data);
    }

    @Override
    public String exec(String command, Logging logging) throws KMRuntimeException {
        checkStarted();
        return wrapped.exec(command, logging);
    }

    @Override
    public void deleteFile(String path, boolean recursive) throws KMRuntimeException {
        checkStarted();
        wrapped.deleteFile(path, recursive);
    }

    @Override
    public void createSymlink(String path, String target) throws KMRuntimeException {
        checkStarted();
        wrapped.createSymlink(path, target);
    }

    @Override
    public void setFileOwner(String path, String owner) throws KMRuntimeException {
        checkStarted();
        wrapped.setFileOwner(path, owner);
    }

    @Override
    public void setFileGroup(String path, String group) throws KMRuntimeException {
        checkStarted();
        wrapped.setFileGroup(path, group);
    }

    @Override
    public void setFilePerms(String path, FilePermissions perms) throws KMRuntimeException {
        checkStarted();
        wrapped.setFilePerms(path, perms);
    }

    @Override
    public String createTempDir() throws KMRuntimeException {
        checkStarted();
        return wrapped.createTempDir();
    }

    @Override
    public String createTempFile() throws KMRuntimeException {
        checkStarted();
        return wrapped.createTempFile();
    }

    @Override
    public boolean fileIsSame(@NotNull String path, @NotNull String content) throws KMRuntimeException {
        checkStarted();
        return wrapped.fileIsSame(path, content);
    }

    @Override
    public void setState(String id, Object state) {
        wrapped.setState(id, state);
    }

    @Override
    public Object getState(String id) {
        return wrapped.getState(id);
    }

    @Override
    public Map<String, Object> getState() {
        return wrapped.getState();
    }

    @Override
    public HostProvider getMetadata() throws KMRuntimeException {
        checkStarted();
        return wrapped.getMetadata();
    }
}
