package com.acutecoder.cotask.base

/**
 * Created by Bhuvaneshwaran
 *
 * 12:34 am 26-12-2023
 * @author AcuteCoder
 */

interface TaskHandler<Result, Progress> : PausingHandle {
    fun cancel()
    fun onPause(callback: () -> Unit): TaskHandler<Result, Progress>
    fun onResume(callback: () -> Unit): TaskHandler<Result, Progress>
    fun onCancelled(callback: () -> Unit): TaskHandler<Result, Progress>
    fun onProgress(callback: (Progress) -> Unit): TaskHandler<Result, Progress>
    fun catch(onError: (Throwable) -> Unit): TaskHandler<Result, Progress>
    fun onEnd(onEnd: () -> Unit): TaskHandler<Result, Progress>
    fun <NextResult> then(runnable: suspend Task<NextResult, Progress>.(Result) -> NextResult): TaskHandler<NextResult, Progress>
    fun onResult(onResult: (Result) -> Unit): TaskHandler<Result, Progress>
}
