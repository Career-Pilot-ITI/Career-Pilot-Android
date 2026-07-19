package com.iti.core.datastore.di

import com.iti.core.datastore.repo.UserProfileRepo
import com.iti.core.datastore.repo.UserProfileRepoImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class UserRepoModule {

    @Binds
    @Singleton
    abstract fun bindUserRepo(
        userRepoImpl: UserProfileRepoImpl
    ): UserProfileRepo

}