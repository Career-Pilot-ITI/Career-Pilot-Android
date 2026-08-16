package com.iti.careerpilot.core.interviews.di

import com.iti.careerpilot.core.interviews.data.remote.FakeInterviewSessionRemoteDataSource
import com.iti.careerpilot.core.interviews.data.remote.InterviewSessionRemoteDataSource
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class FakeInterviewSessionModule {

    @Binds
    @Singleton
    abstract fun bindInterviewSessionRemoteDataSource(
        implementation: FakeInterviewSessionRemoteDataSource,
    ): InterviewSessionRemoteDataSource
}
