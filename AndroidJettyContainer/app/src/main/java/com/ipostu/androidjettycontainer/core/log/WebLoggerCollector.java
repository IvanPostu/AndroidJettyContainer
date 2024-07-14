package com.ipostu.androidjettycontainer.core.log;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

public final class WebLoggerCollector {
    private static WebLoggerCollector INSTANCE;

    public static WebLoggerCollector getInstance() {
        if (INSTANCE != null) {
            return INSTANCE;
        }
        synchronized (WebLoggerCollector.class) {
            if (INSTANCE != null) {
                return INSTANCE;
            }
            INSTANCE = new WebLoggerCollector();
        }
        return INSTANCE;
    }

    private final ConcurrentLinkedQueue<String> logsQueue = new ConcurrentLinkedQueue<>();

    private WebLoggerCollector() {
        Logger logger = Logger.getLogger("");
        logger.addHandler(new AndroidLogHandler((tag, message) -> {
            logsQueue.add(String.format("%s | %s", tag, message));
            if (logsQueue.size() > 10000) {
                logsQueue.clear();
            }
        }));
    }

    public ConcurrentLinkedQueue<String> getLogsQueue() {
        return logsQueue;
    }
}
