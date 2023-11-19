package com.acutecoder.task.java;

public class CancellationException extends RuntimeException {
    public CancellationException() {
        super("Task is cancelled");
    }
}
