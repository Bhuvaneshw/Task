package com.acutecoder.cotask

import android.os.Handler
import android.os.Looper
import com.acutecoder.cotask.base.InterfaceProvider
import com.acutecoder.cotask.base.PausingDispatchQueue
import com.acutecoder.cotask.base.PausingDispatcher
import com.acutecoder.cotask.base.Task
import com.acutecoder.cotask.base.TaskHandler
import com.acutecoder.cotask.subtask.SubCoTask
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

open class CoTask<Result, Progress> internal constructor(
    private val context: CoroutineContext = Dispatchers.Default,
    private val runnable: suspend Task<Result, Progress>.() -> Result
) : PausingDispatchQueue(), TaskHandler<Result, Progress>, Task<Result, Progress>,
    InterfaceProvider<Progress> {

    var isCompleted = false
    var isCancelled = false
    private var job: Job? = null
    private var onEnd: (() -> Unit)? = null
    private var onCancelled: (() -> Unit)? = null
    private var onPause: (() -> Unit)? = null
    private var onResume: (() -> Unit)? = null
    private var onProgress: ((Progress) -> Unit)? = null
    private var onResult: ((Result) -> Unit)? = null
    private var onError: ((Throwable) -> Unit)? = null
    private var child: SubCoTask<Result, *, Progress>? = null

    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, e ->
        if (unableToCatch(e)) throw e
    }
    override val coroutineContext: CoroutineContext
        get() = PausingDispatcher(this, context) + coroutineExceptionHandler

    init {
        start()
    }

    private fun start() {
        isCompleted = false
        isCancelled = false
        job = launch {
            val result = runnable()
            withMain {
                onResult?.invoke(result)
                child?.start(result) ?: invokeOnEnd()
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

    override fun <NextResult> then(runnable: suspend Task<NextResult, Progress>.(Result) -> NextResult): TaskHandler<NextResult, Progress> {
        val subCoTask = SubCoTask(this, this, this, runnable)
        child = subCoTask
        return subCoTask
    }

    override fun cancel() {
        isCancelled = true
        if (isPaused)
            cancelQueue()
        job?.cancel()
    }

    override fun onPause(callback: () -> Unit): TaskHandler<Result, Progress> {
        onPause = callback
        return this
    }

    override fun onResume(callback: () -> Unit): TaskHandler<Result, Progress> {
        onResume = callback
        return this
    }

    override fun onCancelled(callback: () -> Unit): TaskHandler<Result, Progress> {
        onCancelled = callback
        return this
    }

    override fun catch(onError: (Throwable) -> Unit): TaskHandler<Result, Progress> {
        this.onError = onError
        return this
    }

    override fun onEnd(onEnd: () -> Unit): TaskHandler<Result, Progress> {
        this.onEnd = onEnd
        return this
    }

    override suspend fun ensureActive(ensurePause: Boolean, ensureCancel: Boolean) {
        if (ensurePause && isPaused)
            delay(1)
        if (ensureCancel && isCancelled)
            throw CancellationException()
    }

    override fun onResult(onResult: (Result) -> Unit): TaskHandler<Result, Progress> {
        this.onResult = onResult
        return this
    }

    override fun invokeOnEnd() {
        isCompleted = true
        onEnd?.invoke()
    }

    override fun onProgress(callback: (Progress) -> Unit): TaskHandler<Result, Progress> {
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
        ): TaskHandler<Result, Nothing> = CoTask(context, runnable)

    }
}
