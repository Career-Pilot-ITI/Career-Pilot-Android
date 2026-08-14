package com.iti.core.datastore.settings.di

import com.iti.core.datastore.settings.data.UserSettingsRepoImp
import com.iti.core.datastore.settings.domain.UserSettingsRepo
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class UserSettingsModule {

    @Binds
    @Singleton
    abstract fun bindUserSettingsRepo(
        userSettingsRepoImp: UserSettingsRepoImp
    ): UserSettingsRepo

}