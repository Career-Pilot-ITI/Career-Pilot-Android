package com.iti.careerpilot.challenges.di

import com.iti.careerpilot.challenges.data.remote.ChallengesRemoteDataSource
import com.iti.careerpilot.challenges.data.remote.ChallengesRemoteDataSourceImpl
import com.iti.careerpilot.challenges.data.repository.ChallengesRepositoryImpl
import com.iti.careerpilot.challenges.domain.repository.ChallengesRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ChallengesModule {

    @Binds
    @Singleton
    abstract fun bindChallengesRemoteDataSource(
        impl: ChallengesRemoteDataSourceImpl
    ): ChallengesRemoteDataSource

    @Binds
    @Singleton
    abstract fun bindChallengesRepository(
        impl: ChallengesRepositoryImpl
    ): ChallengesRepository
}
