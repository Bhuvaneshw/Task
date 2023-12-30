package com.acutecoder.jtask;

public interface JTaskResult<T> {
    void onResult(T result);
}