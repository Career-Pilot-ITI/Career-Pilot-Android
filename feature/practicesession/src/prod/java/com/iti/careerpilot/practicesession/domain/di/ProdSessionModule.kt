package com.iti.careerpilot.practicesession.domain.di

import com.iti.careerpilot.practicesession.data.datasource.remote.SessionRemoteDataSourceImpl
import com.iti.careerpilot.practicesession.domain.repo.SessionRemoteDataSource
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ProdSessionModule {

    @Binds
    @Singleton
    abstract fun provideSessionRemoteDataSource(
        sessionRemoteDataSourceImpl: SessionRemoteDataSourceImpl
    ): SessionRemoteDataSource
}
