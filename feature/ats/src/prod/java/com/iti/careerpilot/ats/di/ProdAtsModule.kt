package com.iti.careerpilot.ats.di

import com.iti.careerpilot.ats.data.remote.AtsRemoteDataSource
import com.iti.careerpilot.ats.data.remote.AtsRemoteDataSourceImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ProdAtsModule {
    @Binds
    @Singleton
    abstract fun bindAtsRemoteDataSource(implementation: AtsRemoteDataSourceImpl): AtsRemoteDataSource
}
