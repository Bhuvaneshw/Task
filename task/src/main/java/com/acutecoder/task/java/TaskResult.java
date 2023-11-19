package com.acutecoder.task.java;

public interface TaskResult<T> {
    void onResult(T result);
}