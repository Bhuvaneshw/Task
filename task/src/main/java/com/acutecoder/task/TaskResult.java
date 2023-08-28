package com.acutecoder.task;

public interface TaskResult<T> {
    void onResult(T result);
}