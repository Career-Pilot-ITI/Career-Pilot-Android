package com.iti.careerpilot.ai.di

import com.google.firebase.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.remoteConfig
import com.google.firebase.remoteconfig.remoteConfigSettings
import com.iti.careerpilot.ai.domain.BodyLanguageAiFeatureToggle
import com.iti.careerpilot.ai.evaluator.AiContentGenerator
import com.iti.careerpilot.ai.evaluator.FirebaseAiContentGenerator
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class FirebaseAiModule {

    @Binds
    @Singleton
    abstract fun bindAiContentGenerator(
        generator: FirebaseAiContentGenerator
    ): AiContentGenerator

    companion object {
        @Provides
        @Singleton
        fun provideFirebaseRemoteConfig(): FirebaseRemoteConfig {
            val config = Firebase.remoteConfig
            val settings = remoteConfigSettings {
                minimumFetchIntervalInSeconds = 3600L
            }
            config.setConfigSettingsAsync(settings)
            config.setDefaultsAsync(mapOf("body_language_ai_enabled" to true))
            config.fetchAndActivate()
            return config
        }

        @Provides
        @Singleton
        fun provideBodyLanguageAiFeatureToggle(remoteConfig: FirebaseRemoteConfig): BodyLanguageAiFeatureToggle {
            return BodyLanguageAiFeatureToggle {
                try {
                    val value = remoteConfig.getValue("body_language_ai_enabled")
                    if (value.source == FirebaseRemoteConfig.VALUE_SOURCE_STATIC) {
                        true
                    } else {
                        remoteConfig.getBoolean("body_language_ai_enabled")
                    }
                } catch (e: Exception) {
                    true
                }
            }
        }
    }
}

