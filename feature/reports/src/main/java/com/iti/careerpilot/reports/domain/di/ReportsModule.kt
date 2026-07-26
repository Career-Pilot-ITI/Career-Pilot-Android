package com.iti.careerpilot.reports.domain.di

import com.iti.careerpilot.reports.data.repository.ReportsRepositoryImpl
import com.iti.careerpilot.reports.domain.repository.ReportsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class ReportsModule {
    @Binds
    abstract fun bindReportsRepository(
        implementation: ReportsRepositoryImpl,
    ): ReportsRepository
}
