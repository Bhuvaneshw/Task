package com.acutecoder.task

/**
 * Created by Bhuvaneshwaran
 *
 * 10:34 PM, 9/3/2023
 * @author AcuteCoder
 */

interface AbstractTask<T> {

    /**
     * Called when the task is successfully completed without error
     */
    fun onResult(taskResult: ((result: T) -> Unit)?): AbstractTask<T>

    /**
     * Called when the task is failed
     */
    fun onError(taskError: ((error: Exception) -> Unit)?): AbstractTask<T>

    /**
     * Called before the execution of task
     */
    fun onStart(onStart: (() -> Unit)?): AbstractTask<T>

    /**
     * Called after the execution of task regardless the completion of the task (Whether the task is executed without error or not)
     */
    fun onEnd(onEnd: (() -> Unit)?): AbstractTask<T>

    /**
     * Called when the execution of task is cancelled
     */
    fun onCancel(onCancel: (() -> Unit)?): AbstractTask<T>

    /**
     * Called to update progress (in UI) by publishProgress function in UI Thread
     */
    fun onProgress(onProgress: ((Array<out Any>) -> Unit)?): AbstractTask<T>

    /**
     * Starts the execution of the task
     */
    fun start()

    /**
     * Publish the progress in UI Thread
     */
    fun publishProgress(vararg progress: Any): AbstractTask<T>

    /**
     * Chains next Task
     */
    fun then(nextTask: ((T) -> AbstractTask<*>)?): AbstractTask<T>

    /**
     * Sets the cancellation flag
     */
    fun cancel(): AbstractTask<T>

    /**
     * Returns state of task
     */
    fun isActive(): Boolean

    /**
     * Ensure task is alive (ie, task is not cancelled)
     */
    fun ensureActive()

}