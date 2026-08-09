package com.iti.careerpilot.ai.testing

import kotlinx.coroutines.delay

/**
 * Test double for generative AI calls.
 * Configurable response, error, and latency.
 */
class FakeGenerativeModel(
    var responseText: String = FakeBodyLanguageData.sampleEvaluationJson,
    var shouldThrow: Exception? = null,
    var delayMs: Long = 0L,
) {
    suspend fun generateContent(): String {
        if (delayMs > 0) delay(delayMs)
        shouldThrow?.let { throw it }
        return responseText
    }
}
