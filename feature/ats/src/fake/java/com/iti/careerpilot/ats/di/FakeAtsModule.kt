package com.iti.careerpilot.ats.di

import com.iti.careerpilot.ats.data.remote.AtsRemoteDataSource
import com.iti.careerpilot.ats.data.remote.FakeAtsRemoteDataSource
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class FakeAtsModule {
    @Binds
    @Singleton
    abstract fun bindAtsRemoteDataSource(implementation: FakeAtsRemoteDataSource): AtsRemoteDataSource
}
