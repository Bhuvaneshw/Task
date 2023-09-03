package com.acutecoder.task;

public class CancellationException extends RuntimeException {
    public CancellationException() {
        super("Task is cancelled");
    }
}
