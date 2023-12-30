package com.acutecoder.threadtask

import android.os.Handler
import android.os.Looper
import com.acutecoder.threadtask.base.InterfaceProvider
import com.acutecoder.threadtask.base.StartableTaskHandler
import com.acutecoder.threadtask.base.Task
import com.acutecoder.threadtask.subtask.StartableSubThreadTask
import java.util.concurrent.CancellationException

/**
 * Created by Bhuvaneshwaran
 *
 * 11:12 pm 24-12-2023
 * @author AcuteCoder
 */

class StartableThreadTask<Result, Progress> internal constructor(
    private val runnable: Task<Result, Progress>.() -> Result
) : StartableTaskHandler<Result, Progress>, Task<Result, Progress>, InterfaceProvider<Progress> {

    private var onStart: (() -> Unit)? = null
    private var onEnd: (() -> Unit)? = null
    private var onCancelled: (() -> Unit)? = null
    private var onProgress: ((Progress) -> Unit)? = null
    private var onResult: ((Result) -> Unit)? = null
    private var onError: ((Throwable) -> Unit)? = null
    private var cancelled = false
    private var child: StartableSubThreadTask<Result, *, Progress>? = null

    override fun start() {
        start(null)
    }

    override fun start(callback: ((Result) -> Unit)?) {
        onStart?.invoke()
        val thread = Thread {
            val result = runnable()
            withMain {
                callback?.invoke(result)
                onResult?.invoke(result)
                child ?: onEnd?.invoke()
            }
        }
        thread.uncaughtExceptionHandler = Thread.UncaughtExceptionHandler { _, e ->
            if (e is CancellationException) {
                onCancelled?.let { onMainThread { it.invoke() } }
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
        cancelled = true
    }

    override fun onCancelled(callback: () -> Unit): StartableTaskHandler<Result, Progress> {
        onCancelled = callback
        return this
    }

    override fun catch(onError: (Throwable) -> Unit): StartableTaskHandler<Result, Progress> {
        this.onError = onError
        return this
    }

    override fun onStart(onStart: () -> Unit): StartableTaskHandler<Result, Progress> {
        this.onStart = onStart
        return this
    }

    override fun onEnd(onEnd: () -> Unit): StartableTaskHandler<Result, Progress> {
        this.onEnd = onEnd
        return this
    }

    override fun onResult(onResult: (Result) -> Unit): StartableTaskHandler<Result, Progress> {
        this.onResult = onResult
        return this
    }

    override fun onProgress(callback: (Progress) -> Unit): StartableTaskHandler<Result, Progress> {
        this.onProgress = callback
        return this
    }

    override fun publishProgress(progress: Progress) {
        onProgress?.let { onMainThread { it.invoke(progress) } }
    }

    override fun withMain(runnable: () -> Unit) {
        onMainThread {
            runnable()
        }
    }

    override fun ensureActive() {
        if (cancelled) {
            throw CancellationException()
        }
    }

    private fun unableToCatch(e: Throwable): Boolean {
        onError?.let { onMainThread { it(e) } }
        return onError == null
    }

    override fun invokeOnEnd() {
        onEnd?.invoke()
    }

    companion object {
        private val mainHandler = Handler(Looper.getMainLooper())

        internal fun onMainThread(runnable: () -> Unit) {
            mainHandler.post(runnable)
        }

        operator fun <Result> invoke(
            runnable: Task<Result, Nothing>.() -> Result
        ): StartableTaskHandler<Result, Nothing> = StartableThreadTask(runnable)

    }
}