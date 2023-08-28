package com.acutecoder.task;

public class TaskException extends RuntimeException {
    public TaskException(Exception e, boolean isBackground) {
        super("Error while executing " + (isBackground ? "Background" : "Foreground") + " task", e);
    }

    public TaskException(String msg, boolean isBackground) {
        super("Error while executing " + (isBackground ? "Background" : "Foreground") + " task: " + msg);
    }
}
