package com.iti.careerpilot.home.di

import com.iti.careerpilot.home.data.datasource.remote.InterviewRemoteDataSource
import com.iti.careerpilot.home.data.datasource.remote.InterviewRemoteDataSourceImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ProdInterviewDataModule {

    @Binds
    @Singleton
    abstract fun bindInterviewRemoteDataSource(
        interviewRemoteDataSourceImpl: InterviewRemoteDataSourceImpl
    ): InterviewRemoteDataSource
}
