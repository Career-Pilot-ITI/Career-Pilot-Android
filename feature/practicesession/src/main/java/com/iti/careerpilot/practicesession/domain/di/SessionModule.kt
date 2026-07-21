package com.iti.careerpilot.practicesession.domain.di

import com.iti.careerpilot.practicesession.data.datasource.SessionRepoImpl
import com.iti.careerpilot.practicesession.data.datasource.remote.SessionRemoteDataSourceImpl
import com.iti.careerpilot.practicesession.domain.repo.SessionRemoteDataSource
import com.iti.careerpilot.practicesession.domain.repo.SessionRepo
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class SessionModule {

    @Binds
    abstract fun provideSessionRepo(
        sessionRepoImpl: SessionRepoImpl
    ): SessionRepo

    @Binds
    abstract fun provideSessionRemoteDataSource(
        sessionRemoteDataSourceImpl: SessionRemoteDataSourceImpl
    ): SessionRemoteDataSource


}