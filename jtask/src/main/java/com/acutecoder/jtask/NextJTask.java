package com.acutecoder.jtask;

public interface NextJTask<T> {
    JTask<?> run(T result);
}
