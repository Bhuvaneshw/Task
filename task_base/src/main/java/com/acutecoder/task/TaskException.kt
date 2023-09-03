package com.acutecoder.task

/**
 * Created by Bhuvaneshwaran
 *
 * 10:44 PM, 9/3/2023
 * @author AcuteCoder
 */

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