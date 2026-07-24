package com.iti.careerpilot.profile.domain.di

import com.iti.careerpilot.profile.data.datasource.remote.ProfileRemoteDataSourceImpl
import com.iti.careerpilot.profile.domain.datasource.remote.ProfileRemoteDataSource
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ProdProfileModule {

    @Binds
    @Singleton
    abstract fun bindProfileRemoteDataSource(
        impl: ProfileRemoteDataSourceImpl,
    ): ProfileRemoteDataSource
}
