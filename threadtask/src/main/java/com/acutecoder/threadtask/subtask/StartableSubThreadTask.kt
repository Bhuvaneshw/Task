package com.acutecoder.threadtask.subtask

import com.acutecoder.threadtask.ThreadTask
import com.acutecoder.threadtask.base.InterfaceProvider
import com.acutecoder.threadtask.base.StartableTaskHandler
import com.acutecoder.threadtask.base.Task
import java.util.concurrent.CancellationException

/**
 * Created by Bhuvaneshwaran
 *
 * 01:09 am 25-12-2023
 * @author AcuteCoder
 */

internal class StartableSubThreadTask<PreviousResult, Result, Progress>(
    private val parent: StartableTaskHandler<PreviousResult, Progress>,
    private val provider: InterfaceProvider<Progress>,
    private val runnable: Task<Result, Progress>.(PreviousResult) -> Result
) : Task<Result, Progress>, StartableTaskHandler<Result, Progress>, InterfaceProvider<Progress> {

    private var onStart: (() -> Unit)? = null
    private var onError: ((Throwable) -> Unit)? = null
    private var onCancelled: (() -> Unit)? = null
    private var onResult: ((Result) -> Unit)? = null
    private var child: StartableSubThreadTask<Result, *, Progress>? = null
    private var cancelled = false

    override fun start() {
        start(null)
    }

    override fun start(callback: ((Result) -> Unit)?) {
        parent.start {
            run(it, callback)
        }
    }

    private fun run(result: PreviousResult, callback: ((Result) -> Unit)?) {
        onStart?.invoke()
        val thread = Thread {
            val newResult = runnable(result)
            withMain {
                callback?.invoke(newResult)
                onResult?.invoke(newResult)
                child ?: provider.invokeOnEnd()
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

    override fun <NextResult> then(runnable: Task<NextResult, Progress>.(Result) -> NextResult): StartableTaskHandler<NextResult, Progress> {
        val startableSubThreadTask = StartableSubThreadTask(this, this, runnable)
        child = startableSubThreadTask
        return startableSubThreadTask
    }

    override fun cancel() {
        parent.cancel()
    }

    override fun publishProgress(progress: Progress) {
        provider.publishProgress(progress)
    }

    override fun onCancelled(callback: () -> Unit): StartableTaskHandler<Result, Progress> {
        parent.onCancelled(callback)
        onCancelled = callback
        return this
    }

    override fun onProgress(callback: (Progress) -> Unit): StartableTaskHandler<Result, Progress> {
        parent.onProgress(callback)
        return this
    }

    override fun catch(onError: (Throwable) -> Unit): StartableTaskHandler<Result, Progress> {
        this.onError = onError
        parent.catch(onError)
        return this
    }

    override fun onStart(onStart: () -> Unit): StartableTaskHandler<Result, Progress> {
        parent.onStart(onStart)
        return this
    }

    override fun onEnd(onEnd: () -> Unit): StartableTaskHandler<Result, Progress> {
        parent.onEnd(onEnd)
        return this
    }

    override fun onResult(onResult: (Result) -> Unit): StartableTaskHandler<Result, Progress> {
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
