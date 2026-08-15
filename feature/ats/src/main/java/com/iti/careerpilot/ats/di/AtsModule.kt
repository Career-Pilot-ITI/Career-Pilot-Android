package com.iti.careerpilot.ats.di

import com.iti.careerpilot.ats.data.repository.AtsRepositoryImpl
import com.iti.careerpilot.ats.domain.repository.AtsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AtsModule {
    @Binds
    @Singleton
    abstract fun bindAtsRepository(implementation: AtsRepositoryImpl): AtsRepository
}
