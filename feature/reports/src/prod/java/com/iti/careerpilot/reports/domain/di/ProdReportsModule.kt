package com.iti.careerpilot.reports.domain.di

import com.iti.careerpilot.reports.data.datasource.remote.ReportsRemoteDataSource
import com.iti.careerpilot.reports.data.datasource.remote.ReportsRemoteDataSourceImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ProdReportsModule {
    @Binds
    @Singleton
    abstract fun bindReportsRemoteDataSource(
        implementation: ReportsRemoteDataSourceImpl,
    ): ReportsRemoteDataSource
}
