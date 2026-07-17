package com.iti.careerpilot.login.di

import com.iti.careerpilot.login.data.remote.AuthDataSource
import com.iti.careerpilot.login.data.remote.AuthDataSourceImpl
import com.iti.careerpilot.login.data.repository.AuthRepositoryImpl
import com.iti.careerpilot.login.domain.repository.AuthRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class LoginDataModule {

    @Binds
    @Singleton
    abstract fun bindAuthApiService(impl: AuthDataSourceImpl): AuthDataSource

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository
}
