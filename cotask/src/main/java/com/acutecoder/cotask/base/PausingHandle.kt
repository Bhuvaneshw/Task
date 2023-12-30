package com.acutecoder.cotask.base

//https://medium.com/mobilepeople/how-to-pause-a-coroutine-31cbd4cf7815

interface PausingHandle {
    val isPaused: Boolean
    fun pause()
    fun resume()
}
