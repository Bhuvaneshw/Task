package com.acutecoder.task.java;

public interface NextTask<T> {
    Task<?> run(T result);
}
