package com.acutecoder.task

import android.os.Handler
import android.os.Looper

/**
 * Runs a task in Background thread or Foreground thread (UI thread)
 *
 * Created by Bhuvaneshwaran
 *
 * 4:30 PM, 8/28/2023
 *
 * @author AcuteCoder
 */

class Task<T>(private val runnable: Task<T>.() -> T) : AbstractTask<T> {
    private var taskResult: ((result: T) -> Unit)? = null
    private var taskError: ((e: Exception) -> Unit)? = null
    private var onStart: (() -> Unit)? = null
    private var onEnd: (() -> Unit)? = null
    private var onCancel: (() -> Unit)? = null
    private var onProgress: ((Array<out Any>) -> Unit)? = null
    private var isBackground = true
    private var isRunning = false
    private var isCancelled = false
    private var next: ((T) -> AbstractTask<*>)? = null

    /**
     * Called when the task is successfully completed without error
     */
    override fun onResult(taskResult: ((result: T) -> Unit)?): Task<T> {
        this.taskResult = taskResult
        return this
    }

    /**
     * Called when the task is failed
     */
    override fun onError(taskError: ((error: Exception) -> Unit)?): Task<T> {
        this.taskError = taskError
        return this
    }

    /**
     * Called before the execution of task
     */
    override fun onStart(onStart: (() -> Unit)?): Task<T> {
        this.onStart = onStart
        return this
    }

    /**
     * Called after the execution of task regardless the completion of the task (Whether the task is executed without error)
     */
    override fun onEnd(onEnd: (() -> Unit)?): Task<T> {
        this.onEnd = onEnd
        return this
    }

    /**
     * Called when the execution of task is cancelled
     */
    override fun onCancel(onCancel: (() -> Unit)?): Task<T> {
        this.onCancel = onCancel
        return this
    }

    /**
     * Called to update progress (in UI) by publishProgress function in UI Thread
     */
    override fun onProgress(onProgress: ((Array<out Any>) -> Unit)?): Task<T> {
        this.onProgress = onProgress
        return this
    }

    /**
     * Runs the task in background thread
     */
    fun doInBackground(): Task<T> {
        isBackground = true
        return this
    }

    /**
     * Runs the task in foreground thread (Main thread or UI thread)
     */
    fun doInForeground(): Task<T> {
        isBackground = false
        return this
    }

    /**
     * Starts the execution of the task
     */
    override fun start() {
        try {
            isCancelled = false
            if (isRunning) throw TaskException("Task already running!", isBackground)
            isRunning = true
            if (isBackground) {
                try {
                    onStart?.invoke()
                    Thread {
                        try {
                            val o = runnable(this@Task)
                            foregroundHandler.post {
                                taskResult?.invoke(o)
                                onEnd?.invoke()
                                next?.invoke(o)?.start()
                            }
                            isRunning = false
                        } catch (e: CancellationException) {
                            isRunning = false;
                            onCancel?.let {
                                foregroundHandler.post {
                                    it.invoke()
                                }
                            }
                        } catch (e: Exception) {
                            isRunning = false
                            onEnd?.let { foregroundHandler.post { it.invoke() } }
                            if (taskError != null)
                                onError(e)
                            else
                                throw TaskException(e, isBackground)
                        }
                    }.start()
                } catch (e: Exception) {
                    if (taskError != null) onError(e) else throw TaskException(e, isBackground)
                }
            } else {
                try {
                    foregroundHandler.post {
                        try {
                            onStart?.invoke()
                            val o = runnable(this@Task)
                            taskResult?.invoke(o)
                            isRunning = false
                            onEnd?.invoke()
                            next?.invoke(o)?.start()
                        } catch (e: CancellationException) {
                            isRunning = false;
                            onCancel?.invoke()
                        } catch (e: Exception) {
                            isRunning = false
                            onEnd?.invoke()
                            if (taskError != null)
                                onError(e)
                            else
                                throw TaskException(e, isBackground)
                        }
                    }
                } catch (e: Exception) {
                    if (taskError != null) onError(e) else throw TaskException(e, isBackground)
                }
            }
        } catch (e: Exception) {
            if (taskError != null) onError(e) else throw TaskException(e, isBackground)
        }
    }

    /**
     * Publish the progress in UI Thread
     */
    override fun publishProgress(vararg progress: Any): Task<T> {
        onProgress?.let {
            foregroundHandler.post {
                it.invoke(progress)
            }
        }
        return this
    }

    /**
     * Sleeps thread for given milliseconds
     */
    fun sleep(mills: Long): Task<T> {
        Thread.sleep(mills)
        return this
    }

    /**
     * Chains next Task
     */
    override fun then(nextTask: ((T) -> AbstractTask<*>)?): Task<T> {
        next = nextTask;
        return this
    }

    /**
     * Sets the cancellation flag
     */
    override fun cancel(): Task<T> {
        isCancelled = true
        return this
    }

    /**
     * Returns state of task
     */
    override  fun isActive(): Boolean {
        return !isCancelled
    }

    /**
     * Ensure task is alive (ie, task is not cancelled)
     */
    override fun ensureActive() {
        if (!isActive()) throw CancellationException()
    }

    private fun onError(e: Exception) {
        foregroundHandler.post { taskError?.invoke(e) }
    }

    object Foreground {
        /**
         * Runs a simple task in UI thread
         */
        fun start(runnable: Runnable?) {
            foregroundHandler.post((runnable)!!)
        }
    }

    object Background {
        /**
         * Runs a simple task in new thread
         */
        fun start(runnable: Runnable?) {
            Thread(runnable).start()
        }
    }

    companion object {
        private val foregroundHandler = Handler(Looper.getMainLooper())
    }
}