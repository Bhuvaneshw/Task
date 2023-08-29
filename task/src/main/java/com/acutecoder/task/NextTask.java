package com.acutecoder.task;

public interface NextTask<T> {
    Task<?> run(T result);
}
