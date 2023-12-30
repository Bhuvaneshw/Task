package com.acutecoder.cotask.base

/**
 * Created by Bhuvaneshwaran
 *
 * 11:40 am 26-12-2023
 * @author AcuteCoder
 */

interface StartableTaskHandler<Result, Progress> : TaskHandler<Result, Progress> {
    fun start()
    fun start(callback: ((Result) -> Unit)?)
    fun onStart(onStart: () -> Unit): StartableTaskHandler<Result, Progress>

    override fun cancel()
    override fun onCancelled(callback: () -> Unit): StartableTaskHandler<Result, Progress>
    override fun onProgress(callback: (Progress) -> Unit): StartableTaskHandler<Result, Progress>
    override fun catch(onError: (Throwable) -> Unit): StartableTaskHandler<Result, Progress>
    override fun onEnd(onEnd: () -> Unit): StartableTaskHandler<Result, Progress>
    override fun <NextResult> then(runnable: suspend Task<NextResult, Progress>.(Result) -> NextResult): StartableTaskHandler<NextResult, Progress>
    override fun onResult(onResult: (Result) -> Unit): StartableTaskHandler<Result, Progress>
}
