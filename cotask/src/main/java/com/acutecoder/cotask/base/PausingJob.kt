package com.acutecoder.cotask.base

import kotlinx.coroutines.Job

//https://medium.com/mobilepeople/how-to-pause-a-coroutine-31cbd4cf7815

class PausingJob(
    private val job: Job,
    private val pausingHandle: PausingHandle,
) : Job by job, PausingHandle by pausingHandle