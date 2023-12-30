package com.acutecoder.threadtask.base

import com.acutecoder.threadtask.ThreadTask.Companion.onMainThread

/**
 * Created by Bhuvaneshwaran
 *
 * 12:43 am 26-12-2023
 * @author AcuteCoder
 */

interface Task<Result, Progress> {

    fun cancel()
    fun publishProgress(progress: Progress)
    fun ensureActive()

    fun withMain(runnable: () -> Unit) {
        onMainThread {
            runnable()
        }
    }

    fun delay(millis: Long) = Thread.sleep(millis)
    fun currentThread(): Thread = Thread.currentThread()

}