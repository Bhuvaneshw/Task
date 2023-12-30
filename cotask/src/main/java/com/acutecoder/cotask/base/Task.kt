package com.acutecoder.cotask.base

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Created by Bhuvaneshwaran
 *
 * 12:43 am 26-12-2023
 * @author AcuteCoder
 */

interface Task<Result, Progress> : CoroutineScope, PausingHandle {

    fun cancel()
    fun publishProgress(progress: Progress)
    suspend fun ensureActive(ensurePause: Boolean = true, ensureCancel: Boolean = true)

    suspend fun withMain(runnable: suspend CoroutineScope.() -> Unit) {
        withContext(Dispatchers.Main) {
            runnable()
        }
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

    suspend fun withUnconfined(runnable: suspend CoroutineScope.() -> Unit) {
        withContext(Dispatchers.Unconfined) {
            runnable()
        }
    }

}