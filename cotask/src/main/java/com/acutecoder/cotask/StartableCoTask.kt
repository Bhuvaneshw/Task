package com.acutecoder.cotask

import com.acutecoder.cotask.base.Task
import kotlinx.coroutines.Dispatchers
import kotlin.coroutines.CoroutineContext

/**
 * Created by Bhuvaneshwaran
 *
 * 11:12 pm 24-12-2023
 * @author AcuteCoder
 */

open class StartableCoTask<Result>(
    context: CoroutineContext = Dispatchers.Default,
    runnable: suspend Task<Result, Nothing>.() -> Result
) : StartableProgressedCoTask<Result, Nothing>(context, runnable) {

    companion object
}