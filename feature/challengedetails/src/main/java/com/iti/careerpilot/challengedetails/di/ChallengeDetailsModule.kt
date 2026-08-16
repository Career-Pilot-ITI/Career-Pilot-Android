package com.iti.careerpilot.challengedetails.di

import com.iti.careerpilot.challengedetails.data.remote.ChallengeDetailsRemoteDataSource
import com.iti.careerpilot.challengedetails.data.remote.ChallengeDetailsRemoteDataSourceImpl
import com.iti.careerpilot.challengedetails.data.repository.ChallengeDetailsRepositoryImpl
import com.iti.careerpilot.challengedetails.domain.repository.ChallengeDetailsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ChallengeDetailsModule {

    @Binds
    @Singleton
    abstract fun bindChallengeDetailsRemoteDataSource(
        impl: ChallengeDetailsRemoteDataSourceImpl
    ): ChallengeDetailsRemoteDataSource

    @Binds
    @Singleton
    abstract fun bindChallengeDetailsRepository(
        impl: ChallengeDetailsRepositoryImpl
    ): ChallengeDetailsRepository
}
