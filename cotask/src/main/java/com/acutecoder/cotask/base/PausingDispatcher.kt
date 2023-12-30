package com.acutecoder.cotask.base

import com.acutecoder.cotask.getDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlin.coroutines.CoroutineContext

//https://medium.com/mobilepeople/how-to-pause-a-coroutine-31cbd4cf7815

class PausingDispatcher(
    internal val queue: PausingDispatchQueue,
    private val coroutineContext: CoroutineContext,
) : CoroutineDispatcher() {

    override fun dispatch(context: CoroutineContext, block: Runnable) {
        val dispatcher = coroutineContext.getDispatcher()
        if (queue.isPaused) {
            queue.queue(context, block, dispatcher)
        } else {
            dispatcher.dispatch(context, block)
        }
    }

}