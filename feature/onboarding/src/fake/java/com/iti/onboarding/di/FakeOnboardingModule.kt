package com.iti.onboarding.di

import com.iti.onboarding.data.remote.datasource.OnboardingRemoteDataSource
import com.iti.onboarding.data.remote.datasource.FakeOnboardingRemoteDataSource
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class FakeOnboardingModule {

    @Binds
    abstract fun bindOnboardingRemoteDataSource(
        impl: FakeOnboardingRemoteDataSource,
    ): OnboardingRemoteDataSource
}
