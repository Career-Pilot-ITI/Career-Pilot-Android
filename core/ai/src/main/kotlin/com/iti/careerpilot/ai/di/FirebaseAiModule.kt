package com.iti.careerpilot.ai.di

import com.iti.careerpilot.ai.evaluator.AiContentGenerator
import com.iti.careerpilot.ai.evaluator.FirebaseAiContentGenerator
import dagger.Binds
import dagger.Module
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

}
