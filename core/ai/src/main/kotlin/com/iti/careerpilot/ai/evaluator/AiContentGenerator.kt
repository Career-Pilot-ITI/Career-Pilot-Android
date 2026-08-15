package com.iti.careerpilot.ai.evaluator

/**
 * Functional interface abstracting generative AI text generation.
 * Enables clean inversion of control and fast JVM unit testing without mocking Firebase classes.
 */
fun interface AiContentGenerator {
    suspend fun generateContent(prompt: String): String
}
