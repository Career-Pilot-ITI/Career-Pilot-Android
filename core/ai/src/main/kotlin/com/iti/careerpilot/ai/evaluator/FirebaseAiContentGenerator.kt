package com.iti.careerpilot.ai.evaluator

import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.generationConfig
import com.iti.careerpilot.ai.prompt.BodyLanguagePromptBuilder
import com.iti.common.dispatcher.CareerPilotDispatchers.IO
import com.iti.common.dispatcher.Dispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * AI Content Generator using the official Firebase AI Logic SDK (`com.google.firebase:firebase-ai`)
 * with the Google AI backend (`GenerativeBackend.googleAI()`).
 */
@Singleton
class FirebaseAiContentGenerator @Inject constructor(
    @Dispatcher(IO) private val ioDispatcher: CoroutineDispatcher,
) : AiContentGenerator {

    private val generativeModel by lazy {
        Firebase.ai(backend = GenerativeBackend.googleAI()).generativeModel(
            modelName = MODEL_NAME,
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                temperature = 0.2f
            },
            systemInstruction = com.google.firebase.ai.type.content {
                text(BodyLanguagePromptBuilder.SYSTEM_INSTRUCTION)
            },
        )
    }

    override suspend fun generateContent(prompt: String): String = withContext(ioDispatcher) {
        val response = generativeModel.generateContent(prompt)
        response.text ?: throw SerializationException("Firebase AI Logic returned empty response text")
    }

    companion object {
        const val MODEL_NAME = "gemini-3.6-flash"
    }
}
