package com.acutecoder.task;

public interface TaskRunnable<T> {
    T run(Task<T> task) throws Exception;
}