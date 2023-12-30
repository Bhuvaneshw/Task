package com.acutecoder.jtask;

public interface JTaskRunnable<T> {
    T run(JTask<T> task) throws Exception;
}