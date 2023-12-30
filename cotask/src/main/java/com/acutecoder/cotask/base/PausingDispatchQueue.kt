package com.acutecoder.cotask.base

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.cancel
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext

//https://medium.com/mobilepeople/how-to-pause-a-coroutine-31cbd4cf7815

open class PausingDispatchQueue : AbstractCoroutineContextElement(Key), PausingHandle {

    private val paused = AtomicBoolean(false)
    private val queue = ArrayDeque<Resumer>()

    override val isPaused: Boolean
        get() = paused.get()

    override fun pause() {
        paused.set(true)
    }

    override fun resume() {
        if (paused.compareAndSet(true, false)) {
            dispatchNext()
        }
    }

    protected fun cancelQueue() {
        var resumer = queue.removeFirstOrNull()
        while (resumer != null) {
            resumer.cancel()
            resumer=queue.removeFirstOrNull()
        }
    }

    internal fun queue(
        context: CoroutineContext,
        block: Runnable,
        dispatcher: CoroutineDispatcher
    ) {
        queue.addLast(Resumer(dispatcher, context, block))
    }

    private fun dispatchNext() {
        val resumer = queue.removeFirstOrNull() ?: return
        resumer.dispatch()
    }

    private inner class Resumer(
        private val dispatcher: CoroutineDispatcher,
        private val context: CoroutineContext,
        private val block: Runnable,
    ) : Runnable {

        override fun run() {
            block.run()
            if (!paused.get()) {
                dispatchNext()
            }
        }

        fun dispatch() {
            dispatcher.dispatch(context, this)
        }

        fun cancel() {
            dispatch()
            dispatcher.cancel()
        }
    }

    companion object Key : CoroutineContext.Key<PausingDispatchQueue>
}