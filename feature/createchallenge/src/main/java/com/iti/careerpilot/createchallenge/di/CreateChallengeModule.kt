package com.iti.careerpilot.createchallenge.di

import com.iti.careerpilot.createchallenge.data.remote.CreateChallengeRemoteDataSource
import com.iti.careerpilot.createchallenge.data.remote.CreateChallengeRemoteDataSourceImpl
import com.iti.careerpilot.createchallenge.data.repository.CreateChallengeRepositoryImpl
import com.iti.careerpilot.createchallenge.domain.repository.CreateChallengeRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class CreateChallengeModule {

    @Binds
    @Singleton
    abstract fun bindCreateChallengeRemoteDataSource(
        impl: CreateChallengeRemoteDataSourceImpl
    ): CreateChallengeRemoteDataSource

    @Binds
    @Singleton
    abstract fun bindCreateChallengeRepository(
        impl: CreateChallengeRepositoryImpl
    ): CreateChallengeRepository
}
