package com.iti.careerpilot.practicesession.domain.audio.di

import com.iti.careerpilot.practicesession.data.audio.AudioPlayerImpl
import com.iti.careerpilot.practicesession.domain.audio.AudioPlayer
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class AudioModule {

    @Binds
    abstract fun bindAudioPlayer(
        audioPlayerImpl: AudioPlayerImpl
    ): AudioPlayer

}