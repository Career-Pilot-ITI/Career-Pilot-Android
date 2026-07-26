package com.iti.careerpilot.whisper.di

import com.iti.careerpilot.whisper.data.SherpaOnnxWhisperEngine
import com.iti.careerpilot.whisper.domain.WhisperEngine
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class WhisperModule {

    @Binds
    @Singleton
    abstract fun bindWhisperEngine(
        sherpaOnnxWhisperEngine: SherpaOnnxWhisperEngine
    ): WhisperEngine
}
