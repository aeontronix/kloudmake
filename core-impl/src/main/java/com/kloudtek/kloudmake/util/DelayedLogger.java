/*
 * Copyright (c) 2015. Kelewan Technologies Ltd
 */

package com.kloudtek.kloudmake.util;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.ArrayList;

/**
 * Used to temporary keep a log record
 */
public class DelayedLogger {
    private final ArrayList<LogRecord> records = new ArrayList<>();
    private final Logger logger;

    public DelayedLogger(Logger logger) {
        this.logger = logger;
    }


    public synchronized void log(String message, @Nullable Severity severity, @Nullable Throwable exception) {
        records.add(new LogRecord(message, severity, exception));
    }

    public synchronized void setSeverity(Severity severity) {
        for (LogRecord record : records) {
            record.setSeverity(severity);
        }
    }

    public synchronized void clear() {
        records.clear();
    }

    public synchronized void log() {
        for (LogRecord record : records) {
            switch (record.getSeverity()) {
                case TRACE:
                    logger.trace(record.getMessage(), record.getException());
                    break;
                case DEBUG:
                    logger.debug(record.getMessage(), record.getException());
                    break;
                case INFO:
                    logger.info(record.getMessage(), record.getException());
                    break;
                case ERROR:
                    logger.error(record.getMessage(), record.getException());
                    break;
            }
        }
    }

    public class LogRecord {
        private String message;
        private Severity severity;
        private Throwable exception;

        public LogRecord() {
        }

        public LogRecord(String message, Severity severity) {
            this();
            this.message = message;
            this.severity = severity;
        }

        public LogRecord(String message, Severity severity, Throwable exception) {
            this(message, severity);
            this.exception = exception;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public Severity getSeverity() {
            return severity;
        }

        public void setSeverity(Severity severity) {
            this.severity = severity;
        }

        public Throwable getException() {
            return exception;
        }

        public void setException(Throwable exception) {
            this.exception = exception;
        }
    }

    public enum Severity {
        DEBUG, INFO, ERROR, TRACE, WARNING
    }
}
