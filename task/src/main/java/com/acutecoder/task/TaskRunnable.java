package com.acutecoder.task;

public interface TaskRunnable<T> {
    T run() throws Exception;
}