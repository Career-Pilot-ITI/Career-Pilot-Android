package com.iti.careerpilot.core.access.di

import com.iti.careerpilot.core.access.data.remote.AccessRemoteDataSource
import com.iti.careerpilot.core.access.data.remote.AccessRemoteDataSourceImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ProdAccessModule {
    @Binds
    @Singleton
    abstract fun bindAccessRemoteDataSource(impl: AccessRemoteDataSourceImpl): AccessRemoteDataSource
}
