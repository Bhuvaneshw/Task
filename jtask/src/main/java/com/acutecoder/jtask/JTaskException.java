package com.acutecoder.jtask;

public class JTaskException extends RuntimeException {
    public JTaskException(Exception e, boolean isBackground) {
        super("Error while executing " + (isBackground ? "Background" : "Foreground") + " task", e);
    }

    public JTaskException(String msg, boolean isBackground) {
        super("Error while executing " + (isBackground ? "Background" : "Foreground") + " task: " + msg);
    }
}
