package com.acutecoder.threadtask

import android.os.Handler
import android.os.Looper
import com.acutecoder.threadtask.base.InterfaceProvider
import com.acutecoder.threadtask.base.Task
import com.acutecoder.threadtask.base.TaskHandler
import com.acutecoder.threadtask.subtask.SubThreadTask
import java.util.concurrent.CancellationException

/**
 * Created by Bhuvaneshwaran
 *
 * 11:12 pm 24-12-2023
 * @author AcuteCoder
 */

class ThreadTask<Result, Progress> internal constructor(
    private val runnable: Task<Result, Progress>.() -> Result
) : TaskHandler<Result, Progress>, Task<Result, Progress>, InterfaceProvider<Progress> {

    private var onStart: (() -> Unit)? = null
    private var onEnd: (() -> Unit)? = null
    private var onCancelled: (() -> Unit)? = null
    private var onProgress: ((Progress) -> Unit)? = null
    private var onResult: ((Result) -> Unit)? = null
    private var onError: ((Throwable) -> Unit)? = null
    private var cancelled = false
    private var child: SubThreadTask<Result, *, Progress>? = null

    init {
        start()
    }

    private fun start() {
        onStart?.invoke()
        val thread = Thread {
            val result = runnable()
            withMain {
                onResult?.invoke(result)
                child?.start(result) ?: invokeOnEnd()
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

    override fun <NextResult> then(runnable: Task<NextResult, Progress>.(Result) -> NextResult): TaskHandler<NextResult, Progress> {
        val subThreadTask = SubThreadTask(this, this, runnable)
        child = subThreadTask
        return subThreadTask
    }

    override fun cancel() {
        cancelled = true
    }

    override fun onCancelled(callback: () -> Unit): ThreadTask<Result, Progress> {
        onCancelled = callback
        return this
    }

    override fun catch(onError: (Throwable) -> Unit): ThreadTask<Result, Progress> {
        this.onError = onError
        return this
    }

    override fun onEnd(onEnd: () -> Unit): ThreadTask<Result, Progress> {
        this.onEnd = onEnd
        return this
    }

    override fun onResult(onResult: (Result) -> Unit): TaskHandler<Result, Progress> {
        this.onResult = onResult
        return this
    }

    override fun onProgress(callback: (Progress) -> Unit): ThreadTask<Result, Progress> {
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
        ): TaskHandler<Result, Nothing> = ThreadTask(runnable)

    }

}
