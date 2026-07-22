package com.iti.careerpilot.login.di

import com.iti.careerpilot.login.data.remote.AuthRemoteDataSource
import com.iti.careerpilot.login.data.remote.AuthRemoteDataSourceImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class ProdLoginDataModule {

    @Binds
    abstract fun bindAuthApiService(impl: AuthRemoteDataSourceImpl): AuthRemoteDataSource
}
