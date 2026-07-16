package com.iti.careerpilot.profile.domain.di

import com.iti.careerpilot.profile.data.datasource.local.ProfileLocalDataSourceImpl
import com.iti.careerpilot.profile.data.datasource.remote.ProfileRemoteDataSourceImpl
import com.iti.careerpilot.profile.data.repo.ProfileRepoImpl
import com.iti.careerpilot.profile.domain.datasource.local.ProfileLocalDataSource
import com.iti.careerpilot.profile.domain.datasource.remote.ProfileRemoteDataSource
import com.iti.careerpilot.profile.domain.repo.ProfileRepo
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class ProfileModule {

    @Binds
    abstract fun bindProfileLocalDataSource(
        profileLocalDataSourceImpl: ProfileLocalDataSourceImpl
    ): ProfileLocalDataSource

    @Binds
    abstract fun bindProfileRemoteDataSource(
        profileRemoteDataSourceImpl: ProfileRemoteDataSourceImpl
    ): ProfileRemoteDataSource

    @Binds
    abstract fun bindProfileRepo(
        profileRepoImpl: ProfileRepoImpl
    ): ProfileRepo

}