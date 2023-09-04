package com.acutecoder.task

import android.os.Handler
import android.os.Looper
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * Created by Bhuvaneshwaran
 *
 * 5:06 PM, 9/3/2023
 * @author AcuteCoder
 */

class CoroutineTask<T>(
    dispatcher: CoroutineDispatcher? = null,
    private val scope: CoroutineScope = dispatcher?.let { scopeWithDispatcher(it) }
        ?: DEFAULT_SCOPE,
    private val runnable: suspend CoroutineTask<T>.() -> T
) : AbstractTask<T> {

    companion object {
        private const val SCOPE_NAME = "TaskScope"
        val DEFAULT_SCOPE = CoroutineScope(CoroutineName(SCOPE_NAME))

        private val uiThreadHandler = Handler(Looper.getMainLooper())
        private fun scopeWithDispatcher(dispatcher: CoroutineDispatcher): CoroutineScope {
            return CoroutineScope(dispatcher + CoroutineName(SCOPE_NAME))
        }
    }

    private var taskResult: ((result: T) -> Unit)? = null
    private var taskError: ((e: Exception) -> Unit)? = null
    private var onStart: (() -> Unit)? = null
    private var onEnd: (() -> Unit)? = null
    private var onCancel: (() -> Unit)? = null
    private var onProgress: ((Array<out Any>) -> Unit)? = null
    private var isRunning = false
    private var isCancelled = false
    private var next: ((T) -> AbstractTask<*>)? = null
    private var job: Job? = null

    /**
     * Called when the task is successfully completed without error
     */
    override fun onResult(taskResult: ((result: T) -> Unit)?): CoroutineTask<T> {
        this.taskResult = taskResult
        return this
    }

    /**
     * Called when the task is failed
     */
    override fun onError(taskError: ((error: Exception) -> Unit)?): CoroutineTask<T> {
        this.taskError = taskError
        return this
    }

    /**
     * Called before the execution of task
     */
    override fun onStart(onStart: (() -> Unit)?): CoroutineTask<T> {
        this.onStart = onStart
        return this
    }

    /**
     * Called after the execution of task regardless the completion of the task (Whether the task is executed without error or not)
     */
    override fun onEnd(onEnd: (() -> Unit)?): CoroutineTask<T> {
        this.onEnd = onEnd
        return this
    }

    /**
     * Called when the execution of task is cancelled
     */
    override fun onCancel(onCancel: (() -> Unit)?): CoroutineTask<T> {
        this.onCancel = onCancel
        return this
    }

    /**
     * Called to update progress (in UI) by publishProgress function in UI Thread
     */
    override fun onProgress(onProgress: ((Array<out Any>) -> Unit)?): CoroutineTask<T> {
        this.onProgress = onProgress
        return this
    }

    /**
     * Starts the execution of the task
     */
    override fun start() {
        try {
            isCancelled = false
            if (isRunning) throw TaskException("Task already running!", true)
            isRunning = true
            try {
                onStart?.invoke()
                job = scope.launch {
                    try {
                        val o = runnable(this@CoroutineTask)
                        uiThreadHandler.post {
                            taskResult?.invoke(o)
                            onEnd?.invoke()
                            next?.invoke(o)?.start()
                        }
                        isRunning = false
                    } catch (e: CancellationException) {
                        isRunning = false
                        onCancel?.let {
                            uiThreadHandler.post {
                                it.invoke()
                            }
                        }
                    } catch (_: kotlinx.coroutines.CancellationException) {
                        isRunning = false
                    } catch (e: Exception) {
                        isRunning = false
                        uiThreadHandler.post {
                            onEnd?.invoke()
                            taskError?.invoke(e) ?: throw TaskException(e, true)
                        }
                    }
                }
            } catch (e: Exception) {
                taskError?.invoke(e) ?: throw TaskException(e, true)
            }
        } catch (e: Exception) {
            taskError?.invoke(e) ?: throw TaskException(e, true)
        }
    }

    /**
     * Publish the progress in UI Thread
     */
    override fun publishProgress(vararg progress: Any): CoroutineTask<T> {
        onProgress?.let {
            uiThreadHandler.post {
                it.invoke(progress)
            }
        }
        return this
    }

    /**
     * Sleeps thread for given milliseconds
     */
    suspend fun sleep(mills: Long) {
        delay(mills)
    }

    /**
     * Chains next Task
     */
    override fun then(nextTask: ((T) -> AbstractTask<*>)?): CoroutineTask<T> {
        next = nextTask
        return this
    }

    /**
     * Sets the cancellation flag
     */
    override fun cancel(): CoroutineTask<T> {
        if (!isRunning || isCancelled) return this
        isCancelled = true
        job?.cancel()
        onCancel?.let {
            uiThreadHandler.post {
                it.invoke()
            }
        }
        return this
    }

    /**
     * Returns state of task
     */
    override fun isActive(): Boolean {
        return !isCancelled
    }

    /**
     * Ensure task is alive (ie, task is not cancelled)
     */
    override fun ensureActive() {
        job?.ensureActive()
        if (!isActive()) throw CancellationException()
    }

    /**
     * Same as Coroutine.async
     */
    fun async(
        context: CoroutineContext = EmptyCoroutineContext,
        start: CoroutineStart = CoroutineStart.DEFAULT,
        block: suspend CoroutineScope.() -> T
    ) = scope.async(context, start, block)

    /**
     * Same as Coroutine async but Handles exception to task callback
     */
    fun asyncSafely(
        context: CoroutineContext = EmptyCoroutineContext,
        start: CoroutineStart = CoroutineStart.DEFAULT,
        block: suspend CoroutineScope.() -> T
    ) = scope.async(context, start) {
        return@async try {
            block()
        } catch (e: Exception) {
            job?.cancel()
            isRunning = false
            uiThreadHandler.post {
                onEnd?.invoke()
                taskError?.invoke(e) ?: throw TaskException(e, true)
            }
            null
        }
    }

    /**
     * Same as withContext of Coroutine builder but Handles exception to task callback
     */
    suspend fun withContextSafely(
        context: CoroutineContext,
        block: suspend CoroutineScope.() -> T
    ) = withContext(context) {
        return@withContext try {
            block()
        } catch (e: Exception) {
            job?.cancel()
            isRunning = false
            uiThreadHandler.post {
                onEnd?.invoke()
                taskError?.invoke(e) ?: throw TaskException(e, true)
            }
            null
        }
    }
}