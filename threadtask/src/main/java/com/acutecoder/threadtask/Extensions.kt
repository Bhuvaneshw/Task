package com.acutecoder.threadtask

import android.util.Log
import com.acutecoder.threadtask.base.StartableTaskHandler
import com.acutecoder.threadtask.base.Task
import com.acutecoder.threadtask.base.TaskHandler

/*
 * Created by Bhuvaneshwaran
 *
 * 10:17 pm 27-12-2023
 * @author AcuteCoder
 */


@Suppress("FunctionName")
fun <Result, Progress> ProgressedThreadTask(
    runnable: Task<Result, Progress>.() -> Result
): TaskHandler<Result, Progress> = ThreadTask(runnable)

@Suppress("FunctionName")
fun <Result, Progress> StartableProgressedThreadTask(
    runnable: Task<Result, Progress>.() -> Result
): StartableTaskHandler<Result, Progress> = StartableThreadTask(runnable)

fun <Result, Progress> TaskHandler<Result, Progress>.logError(tag: String) =
    catch { Log.e(tag, "$it") }

fun <Result, Progress> StartableThreadTask<Result, Progress>.logError(tag: String) =
    catch { Log.e(tag, "$it") }
