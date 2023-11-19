package com.acutecoder.task.java;

public interface TaskRunnable<T> {
    T run(Task<T> task) throws Exception;
}