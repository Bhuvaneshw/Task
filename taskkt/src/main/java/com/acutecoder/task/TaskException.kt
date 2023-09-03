package com.acutecoder.task

class TaskException : RuntimeException {
    constructor(
        e: Exception?, isBackground: Boolean
    ) : super(
        "Error while executing " + (if (isBackground) "Background" else "Foreground") + " task", e
    )

    constructor(
        msg: String, isBackground: Boolean
    ) : super("Error while executing " + (if (isBackground) "Background" else "Foreground") + " task: " + msg)
}