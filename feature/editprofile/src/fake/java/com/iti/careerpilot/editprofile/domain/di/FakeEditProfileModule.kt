package com.iti.careerpilot.editprofile.domain.di

import com.iti.careerpilot.editprofile.data.datasource.remote.FakeEditProfileRemoteDataSource
import com.iti.careerpilot.editprofile.domain.datasource.remote.EditProfileRemoteDataSource
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class FakeEditProfileModule {

    @Binds
    @Singleton
    abstract fun bindEditProfileRemoteDataSource(
        impl: FakeEditProfileRemoteDataSource
    ): EditProfileRemoteDataSource
}
