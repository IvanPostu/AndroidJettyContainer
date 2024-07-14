package com.ipostu.androidjettycontainer.core.log;

import android.util.Log;

import java.util.logging.Handler;
import java.util.logging.LogRecord;

public class AndroidLogHandler extends Handler {
    private static final String TAG = "JUL";

    private final LogConsumer logConsumer;

    public AndroidLogHandler(LogConsumer logConsumer) {
        this.logConsumer = logConsumer;
    }

    @Override
    public void publish(LogRecord record) {
        String message = record.getMessage();
        int level = record.getLevel().intValue();
        String calculatedTag = TAG + ":" + record.getLoggerName();
        Throwable throwable = record.getThrown();

        if (level >= java.util.logging.Level.SEVERE.intValue()) {
            log(Level.ERROR, calculatedTag, message, throwable);
        } else if (level >= java.util.logging.Level.WARNING.intValue()) {
            log(Level.WARN, calculatedTag, message, throwable);
        } else if (level >= java.util.logging.Level.INFO.intValue()) {
            log(Level.INFO, calculatedTag, message, throwable);
        } else {
            log(Level.DEBUG, calculatedTag, message, throwable);
        }
    }

    @Override
    public void flush() {
    }

    @Override
    public void close() throws SecurityException {
    }

    private void log(Level level, String tag, String message, Throwable throwable) {
        if (Level.ERROR == level) {
            Log.e(tag, message, throwable);
        }
        if (Level.WARN == level) {
            Log.e(tag, message, throwable);
        }
        if (Level.INFO == level) {
            Log.e(tag, message, throwable);
        }
        if (Level.DEBUG == level) {
            Log.e(tag, message, throwable);
        }
        logConsumer.consumeLog(level.name() + ":" + tag, message);
    }

    private enum Level {
        ERROR, WARN, INFO, DEBUG
    }
}
