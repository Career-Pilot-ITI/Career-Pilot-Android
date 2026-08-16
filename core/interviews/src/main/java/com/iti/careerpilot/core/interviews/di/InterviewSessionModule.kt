package com.iti.careerpilot.core.interviews.di

import com.iti.careerpilot.core.interviews.data.repository.InterviewSessionRepositoryImpl
import com.iti.careerpilot.core.interviews.domain.repository.InterviewSessionRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class InterviewSessionModule {

    @Binds
    abstract fun bindInterviewSessionRepository(
        implementation: InterviewSessionRepositoryImpl,
    ): InterviewSessionRepository
}
