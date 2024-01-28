package com.acutecoder.cotask.subtask

import com.acutecoder.cotask.ProgressedCoTask
import com.acutecoder.cotask.base.InterfaceProvider
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

internal class SubCoTask<PreviousResult, Result, Progress>(
    private val parent: TaskHandler<PreviousResult, Progress>,
    private val provider: InterfaceProvider<Progress>,
    private val scope: CoroutineScope,
    private val runnable: suspend Task<Result, Progress>.(PreviousResult) -> Result
) : TaskHandler<Result, Progress>, Task<Result, Progress>, InterfaceProvider<Progress> {

    private var job: Job? = null
    private var onCancelled: (() -> Unit)? = null
    private var onResult: ((Result) -> Unit)? = null
    private var child: SubCoTask<Result, *, Progress>? = null
    override val coroutineContext: CoroutineContext
        get() = scope.coroutineContext

    internal fun start(result: PreviousResult) {
        job = launch {
            val newResult = runnable(result)
            withMain {
                onResult?.invoke(newResult)
                child?.start(newResult) ?: provider.invokeOnEnd()
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

    override fun <NextResult> then(runnable: suspend Task<NextResult, Progress>.(Result) -> NextResult): TaskHandler<NextResult, Progress> {
        val subCoTask = SubCoTask(this, this, scope, runnable)
        child = subCoTask
        return subCoTask
    }

    override val isPaused: Boolean
        get() = parent.isPaused

    override fun cancel() {
        parent.cancel()
        job?.cancel()
    }

    override suspend fun ensureActive(ensurePause: Boolean, ensureCancel: Boolean) {
        provider.ensureActive(ensurePause, ensureCancel)
    }

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

    override fun invokeOnEnd() {
        provider.invokeOnEnd()
    }

}
