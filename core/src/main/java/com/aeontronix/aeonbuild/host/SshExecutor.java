/*
 * Copyright (c) 2024 Aeontronix Inc
 */

package com.aeontronix.aeonbuild.host;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.kloudtek.util.ThreadUtils;
import org.apache.commons.exec.*;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.PipedInputStream;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

/**
 * Implementation of Executor that uses JSch to run commands on a remote server via SSH.
 */
public class SshExecutor implements Executor {
    private static final Logger logger = LoggerFactory.getLogger(SshExecutor.class);
    private Session session;
    private ExecuteStreamHandler streamHandler;
    private ExecuteWatchdog watchdog;
    private int[] exitValues;
    private File workingDirectory;
    private ProcessDestroyer processDestroyer;

    public SshExecutor(Session session) {
        this.session = session;
        this.exitValues = new int[0];
        streamHandler = new PumpStreamHandler();
    }

    @Override
    public int execute(CommandLine commandLine) throws IOException {
        return execute(commandLine, (Map) null);
    }

    @Override
    public int execute(CommandLine commandLine, Map env) throws IOException {
        final DefaultExecuteResultHandler resultHandler = new DefaultExecuteResultHandler();
        execute(commandLine, env, resultHandler);
        return resultHandler.getExitValue();
    }

    @Override
    public void execute(CommandLine commandLine, ExecuteResultHandler executeResultHandler) throws IOException {
        execute(commandLine, null, executeResultHandler);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void execute(CommandLine commandLine, @Nullable Map env, ExecuteResultHandler executeResultHandler) throws IOException {
        try {
            ChannelExec channel = (ChannelExec) session.openChannel("exec");
            String cmd = toString(commandLine, env);
            logger.debug("SSH Exec: " + cmd);
            channel.setCommand(cmd);
            channel.setInputStream(null);
            CountDownLatch countDownLatch = new CountDownLatch(2);
            final PipedInputStream stderrIn = new PipedInputStream();
            PipedStream stderrOut = new PipedStream(stderrIn, countDownLatch);
            final PipedInputStream stdoutIn = new PipedInputStream();
            PipedStream stdoutOut = new PipedStream(stdoutIn, countDownLatch);
            streamHandler.setProcessErrorStream(stderrIn);
            channel.setErrStream(stderrOut);
            streamHandler.setProcessOutputStream(stdoutIn);
            channel.setOutputStream(stdoutOut);

            streamHandler.start();

            channel.connect();

            try {
                countDownLatch.await();
            } catch (InterruptedException e) {
                //
            }
            while (!channel.isClosed()) {
                ThreadUtils.sleep(200);
            }

            streamHandler.stop();
            executeResultHandler.onProcessComplete(channel.getExitStatus());
        } catch (JSchException e) {
            throw new ExecuteException(e.getMessage(), -1, e);
        }
    }

    /**
     * @see Executor#getStreamHandler()
     */
    @Override
    public ExecuteStreamHandler getStreamHandler() {
        return streamHandler;
    }

    /**
     * @see Executor#setStreamHandler(ExecuteStreamHandler)
     */
    @Override
    public void setStreamHandler(ExecuteStreamHandler streamHandler) {
        this.streamHandler = streamHandler;
    }

    /**
     * @see Executor#getWatchdog()
     */
    @Override
    public ExecuteWatchdog getWatchdog() {
        return watchdog;
    }

    /**
     * @see Executor#setWatchdog(ExecuteWatchdog)
     */
    @Override
    public void setWatchdog(ExecuteWatchdog watchDog) {
        this.watchdog = watchDog;
    }

    /**
     * @see Executor#getProcessDestroyer()
     */
    @Override
    public ProcessDestroyer getProcessDestroyer() {
        return this.processDestroyer;
    }

    /**
     * @see Executor#setProcessDestroyer(ProcessDestroyer)
     */
    @Override
    public void setProcessDestroyer(ProcessDestroyer processDestroyer) {
        this.processDestroyer = processDestroyer;
    }

    /**
     * @see Executor#getWorkingDirectory()
     */
    @Override
    public File getWorkingDirectory() {
        return workingDirectory;
    }

    /**
     * @see Executor#setWorkingDirectory(java.io.File)
     */
    @Override
    public void setWorkingDirectory(File dir) {
        this.workingDirectory = dir;
    }

    /**
     * @see Executor#setExitValue(int)
     */
    @Override
    public void setExitValue(final int value) {
        this.setExitValues(new int[]{value});
    }

    /**
     * @see Executor#setExitValues(int[])
     */
    @Override
    public void setExitValues(final int[] values) {
        this.exitValues = (values == null ? null : values.clone());
    }

    /**
     * @see Executor#isFailure(int)
     */
    @Override
    public boolean isFailure(final int exitValue) {
        if (this.exitValues == null) {
            return false;
        } else if (this.exitValues.length == 0) {
            return exitValue != 0;
        } else {
            for (int val : this.exitValues) {
                if (val == exitValue) {
                    return false;
                }
            }
        }
        return true;
    }

    private String toString(CommandLine commandLine, Map<String, String> env) {
        StringBuilder cmd = new StringBuilder();
        if (env != null && !env.isEmpty()) {
            cmd.append("env ");
            for (Map.Entry entry : env.entrySet()) {
                cmd.append(entry.getKey()).append("=").append(entry.getValue()).append(' ');
            }
        }
        cmd.append(commandLine.toString());
        return cmd.toString();
    }
}
