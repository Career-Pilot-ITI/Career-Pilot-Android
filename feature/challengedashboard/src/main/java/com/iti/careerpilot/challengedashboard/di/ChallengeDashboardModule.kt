package com.iti.careerpilot.challengedashboard.di

import com.iti.careerpilot.challengedashboard.data.remote.ChallengeDashboardRemoteDataSource
import com.iti.careerpilot.challengedashboard.data.remote.ChallengeDashboardRemoteDataSourceImpl
import com.iti.careerpilot.challengedashboard.data.repository.ChallengeDashboardRepositoryImpl
import com.iti.careerpilot.challengedashboard.domain.repository.ChallengeDashboardRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ChallengeDashboardModule {

    @Binds
    @Singleton
    abstract fun bindChallengeDashboardRemoteDataSource(
        impl: ChallengeDashboardRemoteDataSourceImpl
    ): ChallengeDashboardRemoteDataSource

    @Binds
    @Singleton
    abstract fun bindChallengeDashboardRepository(
        impl: ChallengeDashboardRepositoryImpl
    ): ChallengeDashboardRepository
}
