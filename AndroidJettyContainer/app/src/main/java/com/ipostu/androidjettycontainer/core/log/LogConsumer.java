package com.ipostu.androidjettycontainer.core.log;

@FunctionalInterface
public interface LogConsumer {
    void consumeLog(String tag, String message);
}
