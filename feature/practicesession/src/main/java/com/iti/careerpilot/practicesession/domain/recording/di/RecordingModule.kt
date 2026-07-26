package com.iti.careerpilot.practicesession.domain.recording.di

import com.iti.careerpilot.practicesession.domain.recording.VoiceRecorder
import com.iti.careerpilot.practicesession.data.recording.VoiceRecorderImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RecordingModule {

    @Binds
    abstract fun bindVoiceRecorder(
        voiceRecorderImpl: VoiceRecorderImpl,
    ): VoiceRecorder

}