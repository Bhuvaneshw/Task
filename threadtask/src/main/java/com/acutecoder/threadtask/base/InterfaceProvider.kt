package com.acutecoder.threadtask.base

/**
 * Created by Bhuvaneshwaran
 *
 * 10:50 pm 26-12-2023
 * @author AcuteCoder
 */

interface InterfaceProvider<Progress> {
    fun invokeOnEnd()
    fun publishProgress(progress: Progress)
}