package com.acutecoder.threadtask.subtask

import com.acutecoder.threadtask.ThreadTask
import com.acutecoder.threadtask.base.InterfaceProvider
import com.acutecoder.threadtask.base.Task
import com.acutecoder.threadtask.base.TaskHandler
import java.util.concurrent.CancellationException

/**
 * Created by Bhuvaneshwaran
 *
 * 01:09 am 25-12-2023
 * @author AcuteCoder
 */

internal class SubThreadTask<PreviousResult, Result, Progress>(
    private val parent: TaskHandler<PreviousResult, Progress>,
    private val provider: InterfaceProvider<Progress>,
    private val runnable: Task<Result, Progress>.(PreviousResult) -> Result
) : Task<Result, Progress>, TaskHandler<Result, Progress>, InterfaceProvider<Progress> {

    private var onError: ((Throwable) -> Unit)? = null
    private var onCancelled: (() -> Unit)? = null
    private var onResult: ((Result) -> Unit)? = null
    private var child: SubThreadTask<Result, *, Progress>? = null
    private var cancelled = false

    internal fun start(result: PreviousResult) {
        val thread = Thread {
            val newResult = runnable(result)
            withMain {
                onResult?.invoke(newResult)
                child?.start(newResult) ?: provider.invokeOnEnd()
            }
        }
        thread.uncaughtExceptionHandler = Thread.UncaughtExceptionHandler { _, e ->
            if (e is CancellationException) {
                onCancelled?.let { ThreadTask.onMainThread { it.invoke() } }
                invokeOnEnd()
            } else if (unableToCatch(e)) throw e
        }
        thread.start()
    }

    override fun <NextResult> then(runnable: Task<NextResult, Progress>.(Result) -> NextResult): TaskHandler<NextResult, Progress> {
        val startableSubThreadTask = SubThreadTask(this, this, runnable)
        child = startableSubThreadTask
        return startableSubThreadTask
    }

    override fun cancel() {
        cancelled = true
        parent.cancel()
    }

    override fun publishProgress(progress: Progress) {
        provider.publishProgress(progress)
    }

    override fun onCancelled(callback: () -> Unit): TaskHandler<Result, Progress> {
        parent.onCancelled(callback)
        onCancelled = callback
        return this
    }

    override fun onProgress(callback: (Progress) -> Unit): TaskHandler<Result, Progress> {
        parent.onProgress(callback)
        return this
    }

    override fun catch(onError: (Throwable) -> Unit): TaskHandler<Result, Progress> {
        this.onError = onError
        parent.catch(onError)
        return this
    }

    override fun onEnd(onEnd: () -> Unit): TaskHandler<Result, Progress> {
        parent.onEnd(onEnd)
        return this
    }

    override fun onResult(onResult: (Result) -> Unit): TaskHandler<Result, Progress> {
        this.onResult = onResult
        return this
    }

    override fun ensureActive() {
        if (cancelled) {
            throw CancellationException()
        }
    }

    private fun unableToCatch(e: Throwable): Boolean {
        onError?.let { ThreadTask.onMainThread { it(e) } }
        return onError == null
    }

    override fun invokeOnEnd() {
        provider.invokeOnEnd()
    }

}
