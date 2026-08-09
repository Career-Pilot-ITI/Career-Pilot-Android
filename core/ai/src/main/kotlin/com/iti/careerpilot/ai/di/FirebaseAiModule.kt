package com.iti.careerpilot.ai.di

import com.google.firebase.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.remoteConfig
import com.google.firebase.remoteconfig.remoteConfigSettings
import com.google.firebase.vertexai.GenerativeModel
import com.google.firebase.vertexai.type.generationConfig
import com.google.firebase.vertexai.vertexAI
import com.iti.careerpilot.ai.evaluator.AiContentGenerator
import com.iti.careerpilot.ai.prompt.BodyLanguagePromptBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.SerializationException
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FirebaseAiModule {

    private const val MODEL_NAME = "gemini-2.0-flash"
    private const val LOCATION = "us-central1"

    @Provides
    @Singleton
    fun provideGenerativeModel(): GenerativeModel {
        val vertexAI = Firebase.vertexAI(location = LOCATION)
        return vertexAI.generativeModel(
            modelName = MODEL_NAME,
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                temperature = 0.2f
            },
            systemInstruction = com.google.firebase.vertexai.type.content {
                text(BodyLanguagePromptBuilder.SYSTEM_INSTRUCTION)
            },
        )
    }

    @Provides
    @Singleton
    fun provideAiContentGenerator(generativeModel: GenerativeModel): AiContentGenerator {
        return AiContentGenerator { prompt ->
            val response = generativeModel.generateContent(prompt)
            response.text ?: throw SerializationException("Gemini returned empty response text")
        }
    }

    @Provides
    @Singleton
    fun provideFirebaseRemoteConfig(): FirebaseRemoteConfig {
        val config = Firebase.remoteConfig
        val settings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = 3600L
        }
        config.setConfigSettingsAsync(settings)
        return config
    }
}
