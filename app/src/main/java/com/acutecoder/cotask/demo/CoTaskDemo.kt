package com.acutecoder.cotask.demo

import android.annotation.SuppressLint
import com.acutecoder.cotask.CoTask
import com.acutecoder.cotask.ProgressedCoTask
import com.acutecoder.cotask.StartableCoTask
import com.acutecoder.cotask.StartableProgressedCoTask
import com.acutecoder.cotask.coTask
import com.acutecoder.cotask.logError
import com.acutecoder.cotask.progressedCoTask
import com.acutecoder.cotask.startableCoTask
import com.acutecoder.cotask.startableProgressedCoTask
import com.acutecoder.cotask.withIO
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Created by Bhuvaneshwaran
 *
 * 10:11 pm 27-12-2023
 * @author AcuteCoder
 */

@SuppressLint("SetTextI18n")
fun coTaskExample(mainActivity: MainActivity) = with(mainActivity) {
    fun simpleCoTask() {
        CoTask {   // Default dispatcher will be Dispatchers.Default
            delay(1000)       // Your expensive task
        }
    }

    fun usingCoroutineFunctions() {
        CoTask {     // Task<Unit,Nothing>:CoroutineScope
            delay(1000)
            launch {    // from coroutine library
                delay(1000)
            }
            val job = async {// from coroutine library
                delay(1000)
            }
            job.await()
        }
    }

    fun usingDispatchers() {
        CoTask(Dispatchers.IO) {
            delay(1000L)
        }

        // Extensions
        CoTask withIO {
        }
        // or
        CoTask.withIO {
        }
    }

    fun callbackCoTask() {
        CoTask {    // this: Task<String, Nothing> String => return type
            delay(1000)
            "My valuable result"
        }.onPause {
            appendStatus("CoTask1 Paused")
        }.onResume {
            appendStatus("CoTask1 Resumed")
        }.onEnd {
            appendStatus("CoTask1 completed")
        }.onCancelled {
            appendStatus("CoTask1 cancelled")
        }.onResult { result: String ->
            appendStatus("CoTask1 result $result")
        }
    }

    @Suppress("DIVISION_BY_ZERO")
    fun errorCoTask() {
        CoTask {
            delay(4000)
            5 / 0// Divide by zero
        }.catch {
            appendStatus("CoTask error $it")
        }
        // Or
        CoTask {
            delay(4000)
            5 / 0// Divide by zero
        }.logError("CoTask")
    }

    fun chainedCoTask() {
        CoTask {    // this: Task<String, Nothing> String => return type
            delay(1000)
            "500"
        }.then { it: String ->     // this: Task<Int, Nothing>, it:String => the previous return value
            delay(2000)
            it.toInt()
        }.then { it: Int ->
            it / 5f
        }.onResult { result: Float ->
            appendStatus("CoTask2 result $result")
        }
    }

    fun progressedCoTask() {
        ProgressedCoTask {    // this: Task<String, Int> String => return type, Int => Progress Type
            delay(1000)
            publishProgress(50)
            delay(1000)
            publishProgress(99)
            "My valuable result"
        }.onProgress { progress: Int ->
            appendStatus("CoTask3 progress $progress")
        }.onResult { result: String ->
            appendStatus("CoTask3 result $result")
        }
    }

    fun cancellingCoTask() {
        val task = ProgressedCoTask {
            var i = 10
            while (i-- > 0) {
                ensureActive()            // enabling that the task can be paused/cancelled here
                publishProgress(10 - i)
            }
        }.onProgress {
            appendStatus("CoTask4 progress $it")
        }.onCancelled {
            appendStatus("CoTask4 cancelled")
        }

        // Cancelling the task after 1.5 seconds
        CoTask {
            delay(1500)
            task.cancel()
        }
    }

    fun pausingCoTask() {
        val task = ProgressedCoTask {
            var i = 10
            while (i-- > 0) {
                ensureActive()            // enabling that the task can be paused/cancelled here
                publishProgress(10 - i)
            }
//            launchPausing {  }.pause()
//            asyncPausing {  }.pause()
        }.onProgress {
            appendStatus("CoTask5 progress $it")
        }.onPause {
            appendStatus("CoTask5 paused")
        }.onResume {
            appendStatus("CoTask5 resumed")
        }

        // Pausing and Resuming the task after 1.5 seconds of break
        CoTask {
            delay(1500)
            task.pause()
            delay(1500)
            task.resume()
        }
    }

    fun startableCoTask() {
        StartableCoTask {
            delay(1000)
        }.start()
        StartableProgressedCoTask {
            publishProgress(10)
            delay(1000)
            publishProgress(100)
        }.start()

        // If you return any data, then
        StartableCoTask {
            delay(1111)
            "My value"
        }.start { result: String ->        // called before on result callback
            appendStatus("Result $result")
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun extensionFunctions() {
        // You can use these extensions functions with coroutine scope
        GlobalScope.coTask { }
        GlobalScope.progressedCoTask { publishProgress(0) }
        GlobalScope.startableCoTask { }
        GlobalScope.startableProgressedCoTask { publishProgress(0) }

        // Specifying Dispatcher
        GlobalScope.coTask(Dispatchers.IO) { }
        GlobalScope.progressedCoTask(Dispatchers.IO) { publishProgress(0) }
        GlobalScope.startableCoTask(Dispatchers.IO) { }
        GlobalScope.startableProgressedCoTask(Dispatchers.IO) { publishProgress(0) }

        // "with" infix notation
        CoTask withIO {
        }
        ProgressedCoTask withIO {
            publishProgress(1)
        }

        // "with" can't be used as infix notation if you are accessing other functions like start, onCancel, logError, etc
        StartableCoTask.withIO {
        }.start()
        CoTask.withIO {
        }.logError()
    }

    simpleCoTask()
    usingCoroutineFunctions()
    usingDispatchers()
    callbackCoTask()
    errorCoTask()
    chainedCoTask()
    progressedCoTask()
    cancellingCoTask()
    pausingCoTask()
    startableCoTask()
    extensionFunctions()
}