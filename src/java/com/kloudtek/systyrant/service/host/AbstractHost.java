/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.systyrant.service.host;

import com.kloudtek.systyrant.DelayedLogger;
import com.kloudtek.systyrant.ExecutionResult;
import com.kloudtek.systyrant.exception.STRuntimeException;
import com.kloudtek.util.CryptoUtils;
import org.apache.commons.exec.*;
import org.apache.commons.io.output.TeeOutputStream;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static com.kloudtek.systyrant.DelayedLogger.Severity.*;
import static com.kloudtek.systyrant.service.host.Host.Logging.*;

/**
 * Abstract implementation of Host
 */
public abstract class AbstractHost implements Host {
    private static final Logger logger = LoggerFactory.getLogger(AbstractHost.class);
    protected Executor executor = new DefaultExecutor();
    protected String execPrefix = "";
    protected String execSuffix = "";
    protected long defaultTimeout = DEFAULT_TIMEOUT;
    protected Logging defaultLogging = ON_ERROR;
    protected int defaultSuccessRetCode = 0;
    protected String username;
    protected ArrayList<String> tempFiles = new ArrayList<>();
    protected ArrayList<String> tempDirs = new ArrayList<>();
    protected HashMap<String, Object> state = new HashMap<>();

    @Override
    public void start() throws STRuntimeException {
    }

    @Override
    public final void stop() {
        for (String tempDir : tempDirs) {
            try {
                deleteFile(tempDir, true);
            } catch (STRuntimeException e) {
                logger.warn("Failed to delete temporary directory " + tempDir);
            }
        }
        for (String tempFile : tempFiles) {
            try {
                deleteFile(tempFile, false);
            } catch (STRuntimeException e) {
                logger.warn("Failed to delete temporary directory " + tempFile);
            }
        }
        doStop();
    }

    public void doStop() {
    }

    @Override
    public String exec(String command) throws STRuntimeException {
        return exec(command, defaultTimeout, defaultSuccessRetCode, defaultLogging, true, null).getOutput();
    }

    @Override
    public String exec(String command, Map<String, String> env) throws STRuntimeException {
        return exec(command, defaultTimeout, defaultSuccessRetCode, defaultLogging, true, env).getOutput();
    }

    @NotNull
    @Override
    public ExecutionResult exec(String command, @Nullable Integer expectedRetCode, Logging logging) throws STRuntimeException {
        return exec(command, defaultTimeout, expectedRetCode, logging, true, null);
    }

    @NotNull
    @Override
    public ExecutionResult exec(String command, @Nullable Integer expectedRetCode, Logging logging, @Nullable Map<String, String> env) throws STRuntimeException {
        return exec(command, defaultTimeout, expectedRetCode, logging, true, env);
    }

    @Override
    public String exec(String command, Logging logging) throws STRuntimeException {
        return exec(command, defaultTimeout, defaultSuccessRetCode, logging, true, null).getOutput();
    }

    @NotNull
    @Override
    public ExecutionResult exec(String command, @Nullable Long timeout, @Nullable Integer expectedRetCode, Logging logging,
                                boolean includePreSuFix) throws STRuntimeException {
        return exec(command, timeout, expectedRetCode, logging, includePreSuFix, null);
    }

    @NotNull
    @Override
    public ExecutionResult exec(final String command, @Nullable Long timeout, @Nullable Integer expectedRetCode, Logging logging,
                                boolean includePreSuFix, @Nullable Map<String, String> env) throws STRuntimeException {
        CommandLine cmdLine = generateCommandLine(command, includePreSuFix);
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
                            throw new STRuntimeException(e.getLocalizedMessage());
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
        if (logging != NO) {
            if (failed) {
                delayedLogger.setSeverity(ERROR);
            } else if (logging == YES) {
                delayedLogger.setSeverity(INFO);
            } else {
                delayedLogger.setSeverity(DEBUG);
            }
            delayedLogger.log();
        }
        if (failed) {
            throw new STRuntimeException("Failed to execute '" + command + "'");
        }
        return result;
    }

    protected abstract CommandLine generateCommandLine(String command, boolean includePreSuFix);

    @Override
    public String readTextFile(String path, String encoding) throws STRuntimeException {
        return new String(readFile(path), Charset.forName(encoding));
    }

    @Override
    public String readTextFile(String path) throws STRuntimeException {
        return readTextFile(path, "UTF-8");
    }

    @Override
    public void writeToFile(String path, String data) throws STRuntimeException {
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

    public String getUsername() {
        return username;
    }

    @Override
    public boolean fileIsSame(@NotNull String path, @NotNull String content) throws STRuntimeException {
        return fileExists(path) && Arrays.equals(CryptoUtils.sha1(content.getBytes()), getFileSha1(path));
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
