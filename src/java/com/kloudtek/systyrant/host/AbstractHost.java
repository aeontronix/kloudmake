/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.systyrant.host;

import com.kloudtek.systyrant.DelayedLogger;
import com.kloudtek.systyrant.ExecutionResult;
import com.kloudtek.systyrant.STContext;
import com.kloudtek.systyrant.annotation.Provider;
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
import static com.kloudtek.systyrant.host.Host.Logging.*;

/** Abstract implementation of Host */
public abstract class AbstractHost implements Host {
    private static final Logger logger = LoggerFactory.getLogger(AbstractHost.class);
    @Provider
    protected HostProviderManager hostProviderManager;
    protected HostProvider hostProvider;
    protected Executor executor = new DefaultExecutor();
    protected String execPrefix = "";
    protected String execSuffix = "";
    protected long defaultTimeout = DEFAULT_TIMEOUT;
    protected Logging defaultLogging = ON_ERROR;
    protected int defaultSuccessRetCode = 0;
    protected ArrayList<String> tempFiles = new ArrayList<>();
    protected ArrayList<String> tempDirs = new ArrayList<>();
    protected HashMap<String, Object> state = new HashMap<>();
    protected boolean handleQuoting = false;
    protected boolean started;

    @Override
    public final void start() throws STRuntimeException {
        if (hostProvider == null) {
            hostProvider = hostProviderManager.find(this);
        }
        started = true;
        doStart();
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
        started = false;
    }

    public void doStart() throws STRuntimeException {
    }

    public void doStop() {
    }

    @Override
    public boolean isStarted() {
        return started;
    }

    @Override
    public HostProvider getMetadata() {
        return hostProvider;
    }

    @Override
    public String exec(String command) throws STRuntimeException {
        return exec(command, defaultTimeout, defaultSuccessRetCode, defaultLogging, null).getOutput();
    }

    @Override
    public String exec(String command, Map<String, String> env) throws STRuntimeException {
        return exec(command, defaultTimeout, defaultSuccessRetCode, defaultLogging, null, env).getOutput();
    }

    @NotNull
    @Override
    public ExecutionResult exec(String command, @Nullable Integer expectedRetCode, Logging logging) throws STRuntimeException {
        return exec(command, defaultTimeout, expectedRetCode, logging, null);
    }

    @NotNull
    @Override
    public ExecutionResult exec(String command, @Nullable Integer expectedRetCode, Logging logging, @Nullable Map<String, String> env) throws STRuntimeException {
        return exec(command, defaultTimeout, expectedRetCode, logging, null, env);
    }

    @Override
    public String exec(String command, Logging logging) throws STRuntimeException {
        return exec(command, defaultTimeout, defaultSuccessRetCode, logging, null).getOutput();
    }

    @NotNull
    @Override
    public ExecutionResult exec(String command, @Nullable Long timeout, @Nullable Integer expectedRetCode, Logging logging, String user) throws STRuntimeException {
        return exec(command, timeout, expectedRetCode, logging, null, null);
    }

    @NotNull
    @Override
    public ExecutionResult exec(final String command, @Nullable Long timeout, @Nullable Integer expectedRetCode, Logging logging,
                                String user, @Nullable Map<String, String> env) throws STRuntimeException {
        CommandLine cmdLine;
        if( hostProvider != null ) {
            cmdLine = hostProvider.generateCommandLine(command, getCurrentUser(), user, handleQuoting);
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


    public abstract String getUser();

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

    public abstract String getCurrentUser();

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
