package com.acutecoder.jtask;

public class CancellationException extends RuntimeException {
    public CancellationException() {
        super("Task is cancelled");
    }
}
