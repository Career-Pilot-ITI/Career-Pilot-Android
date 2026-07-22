package com.iti.onboarding.di

import com.iti.onboarding.data.local.datasource.OnboardingLocalDataSource
import com.iti.onboarding.data.local.datasource.OnboardingLocalDataSourceImpl
import com.iti.onboarding.data.remote.datasource.OnboardingRemoteDataSource
import com.iti.onboarding.data.remote.datasource.OnboardingRemoteDataSourceImpl
import com.iti.onboarding.data.repository.OnboardingRepositoryImpl
import com.iti.onboarding.domain.repository.OnboardingRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class OnboardingModule {

    @Binds
    @Singleton
    abstract fun bindOnboardingRepository(
        impl: OnboardingRepositoryImpl,
    ): OnboardingRepository

    @Binds
    @Singleton
    abstract fun bindOnboardingLocalDataSource(
        impl: OnboardingLocalDataSourceImpl,
    ): OnboardingLocalDataSource
}
