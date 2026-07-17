package com.iti.onboarding.di

import com.iti.onboarding.data.local.datasource.OnboardingLocalDataSource
import com.iti.onboarding.data.local.datasource.OnboardingLocalDataSourceImpl
import com.iti.onboarding.data.remote.datasource.OnboardingRemoteDataSource
import com.iti.onboarding.data.remote.datasource.OnboardingRemoteDataSourceImpl
import com.iti.onboarding.data.repository.OnboardingRepositoryImpl
import com.iti.onboarding.domain.repository.OnboardingRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object TracksDataDIModule {

    @Provides
    @Singleton
    fun provideRemoteDataSource(): OnboardingRemoteDataSource =
        OnboardingRemoteDataSourceImpl()

    @Provides
    @Singleton
    fun provideLocalDataSource(): OnboardingLocalDataSource =
        OnboardingLocalDataSourceImpl()

    @Provides
    @Singleton
    fun bindsRepository(
        local: OnboardingLocalDataSource,
        remote: OnboardingRemoteDataSource,
    ): OnboardingRepository {
        return OnboardingRepositoryImpl(
            remote,
            local
        )
    }
}