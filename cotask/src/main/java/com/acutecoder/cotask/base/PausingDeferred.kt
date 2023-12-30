package com.acutecoder.cotask.base

import kotlinx.coroutines.Deferred

//https://medium.com/mobilepeople/how-to-pause-a-coroutine-31cbd4cf7815

class PausingDeferred<T>(
    private val deferred: Deferred<T>,
    private val pausingHandle: PausingHandle,
) : Deferred<T> by deferred, PausingHandle by pausingHandle
