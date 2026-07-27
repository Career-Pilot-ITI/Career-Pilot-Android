package com.iti.careerpilot.home.di

import com.iti.careerpilot.home.data.datasource.remote.InterviewRemoteDataSourceImpl
import com.iti.careerpilot.home.data.repository.InterviewRepositoryImpl
import com.iti.careerpilot.home.data.datasource.remote.InterviewRemoteDataSource
import com.iti.careerpilot.home.domain.repository.InterviewRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class InterviewDataModule {

    @Binds
    abstract fun bindInterviewRemoteDataSource(
        interviewRemoteDataSourceImpl: InterviewRemoteDataSourceImpl
    ): InterviewRemoteDataSource

    @Binds
    abstract fun bindInterviewRepository(
        interviewRepositoryImpl: InterviewRepositoryImpl
    ): InterviewRepository
}
