package com.iti.common.util

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlin.time.Duration.Companion.milliseconds

fun countdownFlow(
    seconds: Int,
    intervalMillis: Long = 1_000L,
): Flow<Int> {
    require(seconds >= 0) { "seconds must be >= 0, was $seconds" }
    require(intervalMillis > 0) { "intervalMillis must be > 0, was $intervalMillis" }

    return flow {
        for (remaining in seconds downTo 0) {
            emit(remaining)
            if (remaining > 0) delay(intervalMillis.milliseconds)
        }
    }
}
