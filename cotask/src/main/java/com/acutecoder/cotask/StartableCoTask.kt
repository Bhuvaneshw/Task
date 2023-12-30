package com.acutecoder.cotask

import android.os.Handler
import android.os.Looper
import com.acutecoder.cotask.base.InterfaceProvider
import com.acutecoder.cotask.base.PausingDispatchQueue
import com.acutecoder.cotask.base.StartableTaskHandler
import com.acutecoder.cotask.base.Task
import com.acutecoder.cotask.base.TaskHandler
import com.acutecoder.cotask.subtask.StartableSubCoTask
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

/**
 * Created by Bhuvaneshwaran
 *
 * 11:12 pm 24-12-2023
 * @author AcuteCoder
 */

open class StartableCoTask<Result, Progress> internal constructor(
    private val context: CoroutineContext = Dispatchers.Default,
    private val runnable: suspend Task<Result, Progress>.() -> Result
) : PausingDispatchQueue(), Task<Result, Progress>, StartableTaskHandler<Result, Progress>,
    InterfaceProvider<Progress> {

    var isCompleted = false
    var isCancelled = false
    private var job: Job? = null
    private var onStart: (() -> Unit)? = null
    private var onEnd: (() -> Unit)? = null
    private var onCancelled: (() -> Unit)? = null
    private var onPause: (() -> Unit)? = null
    private var onResume: (() -> Unit)? = null
    private var onProgress: ((Progress) -> Unit)? = null
    private var onResult: ((Result) -> Unit)? = null
    private var onError: ((Throwable) -> Unit)? = null
    private var child: StartableSubCoTask<Result, *, Progress>? = null

    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, e ->
        if (unableToCatch(e)) throw e
    }
    override val coroutineContext: CoroutineContext
        get() = context + coroutineExceptionHandler

    override fun start() {
        start(null)
    }

    override fun start(callback: ((Result) -> Unit)?) {
        isCompleted = false
        isCancelled = false
        onStart?.invoke()
        job = launch {
            val result = runnable()
            withMain {
                callback?.invoke(result)
                onResult?.invoke(result)
                child ?: onEnd?.invoke()
            }
        }.apply {
            invokeOnCompletion {
                if (it is CancellationException) {
                    coroutineContext.cancelChildren()
                    onCancelled?.let { onCancelled -> onMainThread { onCancelled.invoke() } }
                    invokeOnEnd()
                }
            }
        }
    }

    override fun <NextResult> then(runnable: suspend Task<NextResult, Progress>.(Result) -> NextResult): StartableTaskHandler<NextResult, Progress> {
        val startableSubCoTask = StartableSubCoTask(this, this, this, runnable)
        child = startableSubCoTask
        return startableSubCoTask
    }

    override fun cancel() {
        isCancelled = true
        if (isPaused)
            if (isPaused)
                cancelQueue()
        job?.cancel()
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

    override fun onPause(callback: () -> Unit): TaskHandler<Result, Progress> {
        onPause = callback
        return this
    }

    override fun onResume(callback: () -> Unit): TaskHandler<Result, Progress> {
        onResume = callback
        return this
    }

    override suspend fun ensureActive(ensurePause: Boolean, ensureCancel: Boolean) {
        if (ensurePause && isPaused)
            delay(1)
        if (ensureCancel && isCancelled)
            throw CancellationException()
    }

    override fun onResult(onResult: (Result) -> Unit): StartableTaskHandler<Result, Progress> {
        this.onResult = onResult
        return this
    }

    override fun invokeOnEnd() {
        isCompleted = true
        onEnd?.invoke()
    }

    override fun onProgress(callback: (Progress) -> Unit): StartableTaskHandler<Result, Progress> {
        this.onProgress = callback
        return this
    }

    override fun publishProgress(progress: Progress) {
        onProgress?.let { onMainThread { it.invoke(progress) } }
    }

    override fun pause() {
        if (isCompleted) return
        super.pause()
        onMainThread {
            onPause?.invoke()
        }
    }

    override fun resume() {
        if (isCompleted) return
        onMainThread {
            onResume?.invoke()
        }
        super.resume()
    }

    private fun unableToCatch(e: Throwable): Boolean {
        onError?.let { onMainThread { it(e) } }
        return onError == null
    }

    companion object {
        private val mainHandler = Handler(Looper.getMainLooper())

        internal fun onMainThread(runnable: () -> Unit) {
            mainHandler.post(runnable)
        }

        operator fun <Result> invoke(
            context: CoroutineContext = Dispatchers.Default,
            runnable: suspend Task<Result, Nothing>.() -> Result
        ): StartableTaskHandler<Result, Nothing> = StartableCoTask(context, runnable)

    }
}