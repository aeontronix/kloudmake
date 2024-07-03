/*
 * Copyright (c) 2024 Aeontronix Inc
 */

package com.aeontronix.aeonbuild.host;

import com.aeontronix.aeonbuild.exception.KMRuntimeException;
import com.aeontronix.aeonbuild.util.DelayedLogger;
import com.aeontronix.aeonbuild.annotation.Provider;
import com.kloudtek.kryptotek.DigestUtils;
import org.apache.commons.exec.*;
import org.bouncycastle.util.io.TeeOutputStream;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Mandatory abstract implementation for host implementations
 */
public abstract class AbstractHost implements Host {
    private static final Logger logger = LoggerFactory.getLogger(AbstractHost.class);
    @Provider
    protected HostProviderManager hostProviderManager;
    protected HostProvider hostProvider;
    protected Executor executor = new DefaultExecutor();
    protected long defaultTimeout = DEFAULT_TIMEOUT;
    protected Logging defaultLogging = Logging.ON_ERROR;
    protected int defaultSuccessRetCode = 0;
    protected ArrayList<String> tempFiles = new ArrayList<>();
    protected ArrayList<String> tempDirs = new ArrayList<>();
    protected HashMap<String, Object> state = new HashMap<>();
    protected boolean handleQuoting = false;
    protected boolean started;

    /**
     * {@inheritDoc}
     */
    public synchronized void start() throws KMRuntimeException {
        if (started) {
            return;
        }
        if (hostProvider == null) {
            hostProvider = hostProviderManager.find(this);
        }
        started = true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void close() {
        for (String tempDir : tempDirs) {
            try {
                deleteFile(tempDir, true);
            } catch (KMRuntimeException e) {
                logger.warn("Failed to delete temporary directory " + tempDir);
            }
        }
        for (String tempFile : tempFiles) {
            try {
                deleteFile(tempFile, false);
            } catch (KMRuntimeException e) {
                logger.warn("Failed to delete temporary directory " + tempFile);
            }
        }
        started = false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isStarted() {
        return started;
    }

    @Override
    public HostProvider getMetadata() {
        return hostProvider;
    }

    @Override
    public String exec(String command) throws KMRuntimeException {
        return exec(command, defaultTimeout, defaultSuccessRetCode, defaultLogging, null, null, null).getOutput();
    }

    @Override
    public String exec(String command, String workdir) throws KMRuntimeException {
        return exec(command, defaultTimeout, defaultSuccessRetCode, defaultLogging, null, workdir, null).getOutput();
    }

    @Override
    public String exec(String command, Map<String, String> env) throws KMRuntimeException {
        return exec(command, defaultTimeout, defaultSuccessRetCode, defaultLogging, null, null, env).getOutput();
    }

    @NotNull
    @Override
    public ExecutionResult exec(String command, @Nullable Integer expectedRetCode, Logging logging) throws KMRuntimeException {
        return exec(command, defaultTimeout, expectedRetCode, logging, null, null, null);
    }

    @NotNull
    @Override
    public ExecutionResult exec(String command, @Nullable Integer expectedRetCode, Logging logging, @Nullable Map<String, String> env) throws KMRuntimeException {
        return exec(command, defaultTimeout, expectedRetCode, logging, null, null, env);
    }

    @Override
    public String exec(String command, Logging logging) throws KMRuntimeException {
        return exec(command, defaultTimeout, defaultSuccessRetCode, logging, null, null, null).getOutput();
    }

    @NotNull
    @Override
    public ExecutionResult exec(String command, @Nullable Long timeout, @Nullable Integer expectedRetCode, Logging logging, String user) throws KMRuntimeException {
        return exec(command, timeout, expectedRetCode, logging, user, null, null);
    }

    @NotNull
    @Override
    public ExecutionResult exec(final String command, @Nullable Long timeout, @Nullable Integer expectedRetCode, Logging logging,
                                String user, String workdir, @Nullable Map<String, String> env) throws KMRuntimeException {
        CommandLine cmdLine;
        if (user == null) {
            user = this.getDefaultUser();
        }
        if (hostProvider != null) {
            cmdLine = hostProvider.generateCommandLine(command, getCurrentUser(), user, handleQuoting,
                    execSupportsWorkDir() ? null : workdir);
        } else {
            cmdLine = CommandLine.parse(command);
        }
        final DelayedLogger delayedLogger = new DelayedLogger(logger);
        LogOutputStream logOutputStream = new LogOutputStream() {
            @Override
            protected void processLine(String line, int level) {
                delayedLogger.log(line, null, null);
            }
        };
        ByteArrayOutputStream txtBuffer = new ByteArrayOutputStream();
        PumpStreamHandler streamHandler = new PumpStreamHandler(new TeeOutputStream(logOutputStream, txtBuffer));
        executor.setStreamHandler(streamHandler);
        ExecuteWatchdog watchdog = new ExecuteWatchdog(timeout == null ? DEFAULT_TIMEOUT : timeout) {
            @Override
            public synchronized void timeoutOccured(Watchdog w) {
                logger.error("Execution timed out: " + command);
                super.timeoutOccured(w);
            }
        };
        executor.setWatchdog(watchdog);
        if (workdir != null) {
            executor.setWorkingDirectory(new File(workdir));
        }
        final ExecutionResult result = new ExecutionResult();
        boolean failed;
        try {
            if (expectedRetCode != null) {
                result.setRetCode(executor.execute(cmdLine, env));
            } else {
                final DoNothingExecResultHandler doNothingExecResultHandler = new DoNothingExecResultHandler(result);
                executor.execute(cmdLine, env, doNothingExecResultHandler);
                //noinspection SynchronizationOnLocalVariableOrMethodParameter
                synchronized (doNothingExecResultHandler) {
                    if (!doNothingExecResultHandler.finished) {
                        try {
                            doNothingExecResultHandler.wait();
                        } catch (InterruptedException e) {
                            throw new KMRuntimeException(e.getLocalizedMessage());
                        }
                    }
                }
            }
            failed = expectedRetCode != null && result.getRetCode() != expectedRetCode;
        } catch (IOException e) {
            failed = true;
            logger.error("I/O Error occured while performing operation: " + e.getMessage());
        }
        result.setOutput(new String(txtBuffer.toByteArray()));
        if (logging != Logging.NO) {
            if (failed) {
                delayedLogger.setSeverity(DelayedLogger.Severity.ERROR);
            } else if (logging == Logging.YES) {
                delayedLogger.setSeverity(DelayedLogger.Severity.INFO);
            } else {
                delayedLogger.setSeverity(DelayedLogger.Severity.DEBUG);
            }
            delayedLogger.log();
        }
        if (failed) {
            throw new KMRuntimeException(toString() + " failed to execute '" + command + "'");
        }
        return result;
    }

    @Override
    public String readTextFile(String path, String encoding) throws KMRuntimeException {
        return new String(readFileData(path), Charset.forName(encoding));
    }

    @Override
    public String readTextFile(String path) throws KMRuntimeException {
        return readTextFile(path, "UTF-8");
    }

    @Override
    public void writeToFile(String path, String data) throws KMRuntimeException {
        writeToFile(path, data.getBytes());
    }

    public long getDefaultTimeout() {
        return defaultTimeout;
    }

    public Logging getDefaultLogging() {
        return defaultLogging;
    }

    public int getDefaultSuccessRetCode() {
        return defaultSuccessRetCode;
    }

    @Override
    public boolean fileIsSame(@NotNull String path, @NotNull String content) throws KMRuntimeException {
        return fileExists(path) && Arrays.equals(DigestUtils.sha1(content.getBytes()), getFileSha1(path));
    }

    @Override
    public void setState(String id, Object state) {
        this.state.put(id, state);
    }

    @Override
    public Object getState(String id) {
        return state.get(id);
    }

    @Override
    public Map<String, Object> getState() {
        return state;
    }

    public abstract String getCurrentUser();

    public abstract String getDefaultUser();

    protected abstract boolean execSupportsWorkDir();

    private class DoNothingExecResultHandler implements ExecuteResultHandler {
        private final ExecutionResult result;
        private boolean finished;

        public DoNothingExecResultHandler(ExecutionResult result) {
            this.result = result;
        }

        @Override
        public synchronized void onProcessComplete(int i) {
            result.setRetCode(i);
            finished = true;
            notifyAll();
        }

        @Override
        public synchronized void onProcessFailed(ExecuteException e) {
            result.setRetCode(e.getExitValue());
            finished = true;
            notifyAll();
        }
    }
}
