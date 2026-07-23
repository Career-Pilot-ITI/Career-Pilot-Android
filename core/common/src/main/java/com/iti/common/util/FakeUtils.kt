package com.iti.common.util

import kotlinx.coroutines.delay
import kotlin.random.Random
import kotlin.time.Duration.Companion.milliseconds

suspend fun fakeDelay(min: Long = 500, max: Long = 1500) {
    delay(Random.nextLong(min, max).milliseconds)
}

fun shouldFail(probability: Double = 0.02): Boolean {
    return Random.nextDouble() < probability
}
