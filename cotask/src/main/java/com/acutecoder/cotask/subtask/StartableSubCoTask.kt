package com.acutecoder.cotask.subtask

import com.acutecoder.cotask.ProgressedCoTask
import com.acutecoder.cotask.base.InterfaceProvider
import com.acutecoder.cotask.base.StartableTaskHandler
import com.acutecoder.cotask.base.Task
import com.acutecoder.cotask.base.TaskHandler
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

/**
 * Created by Bhuvaneshwaran
 *
 * 01:09 am 25-12-2023
 * @author AcuteCoder
 */

internal class StartableSubCoTask<PreviousResult, Result, Progress>(
    private val parent: StartableTaskHandler<PreviousResult, Progress>,
    private val provider: InterfaceProvider<Progress>,
    private val scope: CoroutineScope,
    private val runnable: suspend Task<Result, Progress>.(PreviousResult) -> Result
) : StartableTaskHandler<Result, Progress>, Task<Result, Progress>, InterfaceProvider<Progress> {

    private var job: Job? = null
    private var onStart: (() -> Unit)? = null
    private var onCancelled: (() -> Unit)? = null
    private var onResult: ((Result) -> Unit)? = null
    private var child: StartableSubCoTask<Result, *, Progress>? = null
    override val coroutineContext: CoroutineContext
        get() = scope.coroutineContext

    override fun start() {
        start(null)
    }

    override fun start(callback: ((Result) -> Unit)?) {
        parent.start {
            start(it, callback)
        }
    }

    private fun start(result: PreviousResult, callback: ((Result) -> Unit)?) {
        onStart?.invoke()
        job = launch {
            val newResult = runnable(result)
            withMain {
                callback?.invoke(newResult)
                onResult?.invoke(newResult)
                child ?: provider.invokeOnEnd()
            }
        }.apply {
            invokeOnCompletion {
                if (it is CancellationException) {
                    coroutineContext.cancelChildren()
                    onCancelled?.let { onCancelled ->
                        ProgressedCoTask.onMainThread { onCancelled.invoke() }
                    }
                    invokeOnEnd()
                }
            }
        }
    }

    override fun <NextResult> then(runnable: suspend Task<NextResult, Progress>.(Result) -> NextResult): StartableTaskHandler<NextResult, Progress> {
        val startableSubCoTask = StartableSubCoTask(this, this, scope, runnable)
        child = startableSubCoTask
        return startableSubCoTask
    }

    override fun cancel() {
        parent.cancel()
        job?.cancel()
    }

    override suspend fun ensureActive(ensurePause: Boolean, ensureCancel: Boolean) {
        provider.ensureActive(ensurePause, ensureCancel)
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

    override val isPaused: Boolean
        get() = parent.isPaused

    override fun pause() {
        parent.pause()
    }

    override fun resume() {
        parent.resume()
    }

    override fun onPause(callback: () -> Unit): TaskHandler<Result, Progress> {
        parent.onPause(callback)
        return this
    }

    override fun onResume(callback: () -> Unit): TaskHandler<Result, Progress> {
        parent.onResume(callback)
        return this
    }

    override fun onResult(onResult: (Result) -> Unit): StartableTaskHandler<Result, Progress> {
        this.onResult = onResult
        return this
    }

    override fun invokeOnEnd() {
        provider.invokeOnEnd()
    }

}
