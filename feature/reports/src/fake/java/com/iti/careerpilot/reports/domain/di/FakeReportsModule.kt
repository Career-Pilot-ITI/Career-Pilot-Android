package com.iti.careerpilot.reports.domain.di

import com.iti.careerpilot.reports.data.datasource.remote.FakeReportsRemoteDataSource
import com.iti.careerpilot.reports.data.datasource.remote.ReportsRemoteDataSource
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class FakeReportsModule {
    @Binds
    @Singleton
    abstract fun bindReportsRemoteDataSource(
        implementation: FakeReportsRemoteDataSource,
    ): ReportsRemoteDataSource
}
