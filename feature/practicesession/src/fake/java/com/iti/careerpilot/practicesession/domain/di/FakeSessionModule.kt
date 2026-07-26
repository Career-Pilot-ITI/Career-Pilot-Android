package com.iti.careerpilot.practicesession.domain.di

import com.iti.careerpilot.practicesession.data.datasource.remote.FakeSessionRemoteDataSource
import com.iti.careerpilot.practicesession.domain.repo.SessionRemoteDataSource
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class FakeSessionModule {

    @Binds
    @Singleton
    abstract fun provideSessionRemoteDataSource(
        fakeSessionRemoteDataSource: FakeSessionRemoteDataSource
    ): SessionRemoteDataSource
}
