/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.systyrant.host;

import com.kloudtek.systyrant.exception.STRuntimeException;
import com.kloudtek.systyrant.resource.core.FilePermissions;
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

    private void checkStarted() throws STRuntimeException {
        if (!wrapped.isStarted()) {
            start();
        }
    }

    @Override
    public boolean isStarted() {
        return wrapped.isStarted();
    }

    @Override
    public void start() throws STRuntimeException {
        wrapped.start();
    }

    @Override
    public void close() {
        wrapped.close();
    }

    @Override
    public ExecutionResult execScript(String script, ScriptType type, long timeout, @Nullable Integer expectedRetCode, Logging logging, String user) throws STRuntimeException {
        checkStarted();
        return wrapped.execScript(script, type, timeout, expectedRetCode, logging, user);
    }

    @Override
    public String exec(String command) throws STRuntimeException {
        checkStarted();
        return wrapped.exec(command);
    }

    @Override
    public String exec(String command, String workdir) throws STRuntimeException {
        checkStarted();
        return wrapped.exec(command, workdir);
    }

    @Override
    public String exec(String command, Map<String, String> env) throws STRuntimeException {
        checkStarted();
        return wrapped.exec(command, env);
    }

    @Override
    @NotNull
    public ExecutionResult exec(String command, @Nullable Integer expectedRetCode, Logging logging) throws STRuntimeException {
        checkStarted();
        return wrapped.exec(command, expectedRetCode, logging);
    }

    @Override
    @NotNull
    public ExecutionResult exec(String command, @Nullable Integer expectedRetCode, Logging logging, @Nullable Map<String, String> env) throws STRuntimeException {
        checkStarted();
        return wrapped.exec(command, expectedRetCode, logging, env);
    }

    @Override
    @NotNull
    public ExecutionResult exec(String command, @Nullable Long timeout, @Nullable Integer expectedRetCode, Logging logging, String user) throws STRuntimeException {
        checkStarted();
        return wrapped.exec(command, timeout, expectedRetCode, logging, user);
    }

    @Override
    @NotNull
    public ExecutionResult exec(String command, @Nullable Long timeout, @Nullable Integer expectedRetCode, Logging logging, String user, String workdir, Map<String, String> env) throws STRuntimeException {
        checkStarted();
        return wrapped.exec(command, timeout, expectedRetCode, logging, user, workdir, env);
    }

    @Override
    public boolean fileExists(String path) throws STRuntimeException {
        checkStarted();
        return wrapped.fileExists(path);
    }

    @Override
    public String getFilePathSeparator() throws STRuntimeException {
        checkStarted();
        return wrapped.getFilePathSeparator();
    }

    @Override
    @NotNull
    public FileInfo getFileInfo(String path) throws STRuntimeException {
        checkStarted();
        return wrapped.getFileInfo(path);
    }

    @Override
    public boolean mkdir(String path) throws STRuntimeException {
        checkStarted();
        return wrapped.mkdir(path);
    }

    @Override
    public boolean mkdirs(String path) throws STRuntimeException {
        checkStarted();
        return wrapped.mkdirs(path);
    }

    @Override
    public byte[] getFileSha1(String path) throws STRuntimeException {
        checkStarted();
        return wrapped.getFileSha1(path);
    }

    @Override
    public byte[] readFileData(String path) throws STRuntimeException {
        checkStarted();
        return wrapped.readFileData(path);
    }

    @Override
    public InputStream readFile(String path) throws STRuntimeException {
        checkStarted();
        return wrapped.readFile(path);
    }

    @Override
    public String readTextFile(String path, String encoding) throws STRuntimeException {
        checkStarted();
        return wrapped.readTextFile(path, encoding);
    }

    @Override
    public String readTextFile(String path) throws STRuntimeException {
        checkStarted();
        return wrapped.readTextFile(path);
    }

    @Override
    public void writeToFile(String path, String data) throws STRuntimeException {
        checkStarted();
        wrapped.writeToFile(path, data);
    }

    @Override
    public void writeToFile(String path, byte[] data) throws STRuntimeException {
        checkStarted();
        wrapped.writeToFile(path, data);
    }

    @Override
    public void writeToFile(String path, InputStream data) throws STRuntimeException {
        checkStarted();
        wrapped.writeToFile(path, data);
    }

    @Override
    public String exec(String command, Logging logging) throws STRuntimeException {
        checkStarted();
        return wrapped.exec(command, logging);
    }

    @Override
    public void deleteFile(String path, boolean recursive) throws STRuntimeException {
        checkStarted();
        wrapped.deleteFile(path, recursive);
    }

    @Override
    public void createSymlink(String path, String target) throws STRuntimeException {
        checkStarted();
        wrapped.createSymlink(path, target);
    }

    @Override
    public void setFileOwner(String path, String owner) throws STRuntimeException {
        checkStarted();
        wrapped.setFileOwner(path, owner);
    }

    @Override
    public void setFileGroup(String path, String group) throws STRuntimeException {
        checkStarted();
        wrapped.setFileGroup(path, group);
    }

    @Override
    public void setFilePerms(String path, FilePermissions perms) throws STRuntimeException {
        checkStarted();
        wrapped.setFilePerms(path, perms);
    }

    @Override
    public String createTempDir() throws STRuntimeException {
        checkStarted();
        return wrapped.createTempDir();
    }

    @Override
    public String createTempFile() throws STRuntimeException {
        checkStarted();
        return wrapped.createTempFile();
    }

    @Override
    public boolean fileIsSame(@NotNull String path, @NotNull String content) throws STRuntimeException {
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
    public HostProvider getMetadata() throws STRuntimeException {
        checkStarted();
        return wrapped.getMetadata();
    }
}
