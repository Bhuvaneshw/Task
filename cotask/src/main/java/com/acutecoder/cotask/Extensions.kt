package com.acutecoder.cotask

import android.util.Log
import com.acutecoder.cotask.base.PausingDeferred
import com.acutecoder.cotask.base.PausingDispatchQueue
import com.acutecoder.cotask.base.PausingDispatcher
import com.acutecoder.cotask.base.PausingJob
import com.acutecoder.cotask.base.StartableTaskHandler
import com.acutecoder.cotask.base.Task
import com.acutecoder.cotask.base.TaskHandler
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/*
 * Created by Bhuvaneshwaran
 *
 * 03:04 pm 25-12-2023
 * @author AcuteCoder
 */

fun <Result> CoroutineScope.coTask(
    context: CoroutineContext = Dispatchers.Default,
    runnable: suspend Task<Result, Nothing>.() -> Result
): TaskHandler<Result, Nothing> =
    CoTask(this.coroutineContext + context, runnable)

fun <Result, Progress> CoroutineScope.progressedCoTask(
    context: CoroutineContext = Dispatchers.Default,
    runnable: suspend Task<Result, Progress>.() -> Result
): TaskHandler<Result, Progress> =
    ProgressedCoTask(this.coroutineContext + context, runnable)

fun <Result> CoroutineScope.startableCoTask(
    context: CoroutineContext = Dispatchers.Default,
    runnable: suspend Task<Result, Nothing>.() -> Result
): StartableTaskHandler<Result, Nothing> =
    StartableCoTask(this.coroutineContext + context, runnable)

fun <Result, Progress> CoroutineScope.startableProgressedCoTask(
    context: CoroutineContext = Dispatchers.Default,
    runnable: suspend Task<Result, Progress>.() -> Result
): StartableTaskHandler<Result, Progress> =
    StartableProgressedCoTask(this.coroutineContext + context, runnable)

fun <Result, Progress> TaskHandler<Result, Progress>.logError(tag: String? = null) =
    catch { Log.e(tag ?: "CoTask", "$it") }

fun <Result, Progress> StartableTaskHandler<Result, Progress>.logError(tag: String? = null) =
    catch { Log.e(tag ?: "StartableCoTask", "$it") }

fun CoroutineScope.launchPausing(
    context: CoroutineContext = EmptyCoroutineContext,
    start: CoroutineStart = CoroutineStart.DEFAULT,
    block: suspend CoroutineScope.() -> Unit
): PausingJob {
    val dispatcher = PausingDispatcher(this, context)
    val job = launch(context + dispatcher.queue + dispatcher, start, block)
    return PausingJob(job, dispatcher.queue)
}

fun <T> CoroutineScope.asyncPausing(
    context: CoroutineContext = EmptyCoroutineContext,
    start: CoroutineStart = CoroutineStart.DEFAULT,
    block: suspend CoroutineScope.() -> T
): PausingDeferred<T> {
    val dispatcher = PausingDispatcher(this, context)
    val deferred = async(context + dispatcher.queue + dispatcher, start, block)
    return PausingDeferred(deferred, dispatcher.queue)
}

suspend fun withIO(runnable: suspend CoroutineScope.() -> Unit) {
    withContext(Dispatchers.IO) {
        runnable()
    }
}

suspend fun withDefault(runnable: suspend CoroutineScope.() -> Unit) {
    withContext(Dispatchers.Default) {
        runnable()
    }
}

suspend fun withMain(runnable: suspend CoroutineScope.() -> Unit) {
    withContext(Dispatchers.Main) {
        runnable()
    }
}

suspend fun withUnconfined(runnable: suspend CoroutineScope.() -> Unit) {
    withContext(Dispatchers.Unconfined) {
        runnable()
    }
}

infix fun <Result> CoTask.Companion.withIO(runnable: suspend Task<Result, Nothing>.() -> Result):
        TaskHandler<Result, Nothing> = CoTask(Dispatchers.IO, runnable)

infix fun <Result> CoTask.Companion.withDefault(runnable: suspend Task<Result, Nothing>.() -> Result):
        TaskHandler<Result, Nothing> = CoTask(Dispatchers.Default, runnable)

infix fun <Result> CoTask.Companion.withMain(runnable: suspend Task<Result, Nothing>.() -> Result):
        TaskHandler<Result, Nothing> = CoTask(Dispatchers.Main, runnable)

infix fun <Result> CoTask.Companion.withUnconfined(runnable: suspend Task<Result, Nothing>.() -> Result):
        TaskHandler<Result, Nothing> = CoTask(Dispatchers.Unconfined, runnable)

infix fun <Result, Progress> ProgressedCoTask.Companion.withIO(runnable: suspend Task<Result, Progress>.() -> Result):
        TaskHandler<Result, Progress> = ProgressedCoTask(Dispatchers.IO, runnable)

infix fun <Result, Progress> ProgressedCoTask.Companion.withDefault(runnable: suspend Task<Result, Progress>.() -> Result):
        TaskHandler<Result, Progress> = ProgressedCoTask(Dispatchers.Default, runnable)

infix fun <Result, Progress> ProgressedCoTask.Companion.withMain(runnable: suspend Task<Result, Progress>.() -> Result):
        TaskHandler<Result, Progress> = ProgressedCoTask(Dispatchers.Main, runnable)

infix fun <Result, Progress> ProgressedCoTask.Companion.withUnconfined(runnable: suspend Task<Result, Progress>.() -> Result):
        TaskHandler<Result, Progress> = ProgressedCoTask(Dispatchers.Unconfined, runnable)

infix fun <Result> StartableCoTask.Companion.withIO(runnable: suspend Task<Result, Nothing>.() -> Result): StartableTaskHandler<Result, Nothing> =
    StartableCoTask(Dispatchers.IO, runnable)

infix fun <Result> StartableCoTask.Companion.withDefault(runnable: suspend Task<Result, Nothing>.() -> Result):
        StartableTaskHandler<Result, Nothing> =
    StartableCoTask(Dispatchers.Default, runnable)

infix fun <Result> StartableCoTask.Companion.withMain(runnable: suspend Task<Result, Nothing>.() -> Result):
        StartableTaskHandler<Result, Nothing> =
    StartableCoTask(Dispatchers.Main, runnable)

infix fun <Result> StartableCoTask.Companion.withUnconfined(runnable: suspend Task<Result, Nothing>.() -> Result):
        StartableTaskHandler<Result, Nothing> =
    StartableCoTask(Dispatchers.Unconfined, runnable)

infix fun <Result, Progress> StartableProgressedCoTask.Companion.withIO(runnable: suspend Task<Result, Progress>.() -> Result):
        StartableTaskHandler<Result, Progress> = StartableProgressedCoTask(Dispatchers.IO, runnable)

infix fun <Result, Progress> StartableProgressedCoTask.Companion.withDefault(runnable: suspend Task<Result, Progress>.() -> Result):
        StartableTaskHandler<Result, Progress> =
    StartableProgressedCoTask(Dispatchers.Default, runnable)

infix fun <Result, Progress> StartableProgressedCoTask.Companion.withMain(runnable: suspend Task<Result, Progress>.() -> Result):
        StartableTaskHandler<Result, Progress> =
    StartableProgressedCoTask(Dispatchers.Main, runnable)

infix fun <Result, Progress> StartableProgressedCoTask.Companion.withUnconfined(runnable: suspend Task<Result, Progress>.() -> Result):
        StartableTaskHandler<Result, Progress> =
    StartableProgressedCoTask(Dispatchers.Unconfined, runnable)

@OptIn(ExperimentalStdlibApi::class)
private fun PausingDispatcher(
    scope: CoroutineScope,
    newContext: CoroutineContext
): PausingDispatcher {
    val dispatcher = newContext[CoroutineDispatcher]
        ?: scope.coroutineContext[CoroutineDispatcher]
        ?: Dispatchers.Default
    return PausingDispatcher(
        queue = PausingDispatchQueue(),
        coroutineContext = dispatcher
    )
}

@OptIn(ExperimentalStdlibApi::class)
internal fun CoroutineContext.getDispatcher(defaultDispatcher: CoroutineDispatcher = Dispatchers.Default) =
    (this[CoroutineDispatcher]
        ?: defaultDispatcher)