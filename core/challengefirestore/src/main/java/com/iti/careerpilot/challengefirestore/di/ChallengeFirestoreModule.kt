package com.iti.careerpilot.challengefirestore.di

import com.iti.careerpilot.challengefirestore.ChallengeFirestoreDataSource
import com.iti.careerpilot.challengefirestore.ChallengeFirestoreDataSourceImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ChallengeFirestoreModule {

    @Binds
    @Singleton
    abstract fun bindChallengeFirestoreDataSource(
        impl: ChallengeFirestoreDataSourceImpl
    ): ChallengeFirestoreDataSource
}
