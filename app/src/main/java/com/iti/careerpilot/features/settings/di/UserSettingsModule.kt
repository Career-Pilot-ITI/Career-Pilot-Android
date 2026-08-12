package com.iti.careerpilot.features.settings.di

import com.iti.careerpilot.features.settings.data.UserSettingsRepoImp
import com.iti.careerpilot.features.settings.domain.UserSettingsRepo
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