package com.iti.careerpilot.home.di

import com.iti.careerpilot.home.data.datasource.remote.FakeInterviewRemoteDataSource
import com.iti.careerpilot.home.data.datasource.remote.InterviewRemoteDataSource
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class FakeInterviewDataModule {

    @Binds
    @Singleton
    abstract fun bindInterviewRemoteDataSource(
        fakeInterviewRemoteDataSource: FakeInterviewRemoteDataSource
    ): InterviewRemoteDataSource
}
