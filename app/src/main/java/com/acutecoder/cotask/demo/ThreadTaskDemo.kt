package com.acutecoder.cotask.demo

import com.acutecoder.threadtask.ProgressedThreadTask
import com.acutecoder.threadtask.StartableProgressedThreadTask
import com.acutecoder.threadtask.StartableThreadTask
import com.acutecoder.threadtask.ThreadTask
import com.acutecoder.threadtask.logError

/**
 * Created by Bhuvaneshwaran
 *
 * 10:07 pm 27-12-2023
 * @author AcuteCoder
 */

fun threadTaskExample(mainActivity: MainActivity) = with(mainActivity) {
    fun simpleThreadTask() {
        ThreadTask {
            delay(1000)       // Your expensive task
        }
    }

    fun callbackThreadTask() {
        // Pausing and resuming is not available in thread task
        ThreadTask {    // this: Task<String, Nothing> String => return type
            delay(1000)
            "My valuable result"
        }.onEnd {
            appendStatus("ThreadTask1 completed")
        }.onCancelled {
            appendStatus("ThreadTask1 cancelled")
        }.onResult { result: String ->
            appendStatus("ThreadTask1 result $result")
        }
    }

    @Suppress("DIVISION_BY_ZERO")
    fun errorThreadTask() {
        ThreadTask {
            delay(4000)
            5 / 0// Divide by zero
        }.catch {
            appendStatus("ThreadTask error $it")
        }
        // Or
        ThreadTask {
            delay(4000)
            5 / 0// Divide by zero
        }.logError("ThreadTask")
    }

    fun chainedThreadTask() {
        ThreadTask {    // this: Task<String, Nothing> String => return type
            delay(1000)
            "500"
        }.then { it: String ->     // this: Task<Int, Nothing>, it:String => the previous return value
            delay(2000)
            it.toInt()
        }.then { it: Int ->
            it / 5f
        }.onResult { result: Float ->
            appendStatus("ThreadTask2 result $result")
        }
    }

    fun progressedThreadTask() {
        ProgressedThreadTask {    // this: Task<String, Int> String => return type, Int => Progress Type
            delay(1000)
            publishProgress(50)
            delay(1000)
            publishProgress(99)
            "My valuable result"
        }.onProgress { progress: Int ->
            appendStatus("ThreadTask3 progress $progress")
        }.onResult { result: String ->
            appendStatus("ThreadTask3 result $result")
        }
    }

    fun cancellingThreadTask() {
        val task = ProgressedThreadTask {
            var i = 1
            while (i <= 100) {
                publishProgress(i)
                ensureActive()            // Mandatory for ThreadTask to check for cancellation and calling onCancelled callback
                delay(1000)
                i += 10
            }
        }.onProgress {
            appendStatus("ThreadTask4 progress $it")
        }.onCancelled {
            appendStatus("ThreadTask4 cancelled")
        }

        // Cancelling the task after 1.5 seconds
        ThreadTask {
            delay(1500)
            task.cancel()
        }
    }

    fun startableThreadTask() {
        StartableThreadTask {
            delay(1000)
        }.start()
        StartableProgressedThreadTask {
            publishProgress(10)
            delay(1000)
            publishProgress(100)
        }.start()

        // If you return any data, then
        StartableThreadTask {
            delay(1111)
            "My value"
        }.start { result: String ->        // called before on result callback
            appendStatus("Result $result")
        }
    }

    simpleThreadTask()
    callbackThreadTask()
    errorThreadTask()
    chainedThreadTask()
    progressedThreadTask()
    cancellingThreadTask()
    startableThreadTask()
}