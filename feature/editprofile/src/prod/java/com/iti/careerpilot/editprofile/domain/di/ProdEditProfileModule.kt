package com.iti.careerpilot.editprofile.domain.di

import com.iti.careerpilot.editprofile.data.datasource.remote.EditProfileRemoteDataSourceImpl
import com.iti.careerpilot.editprofile.domain.datasource.remote.EditProfileRemoteDataSource
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ProdEditProfileModule {

    @Binds
    @Singleton
    abstract fun bindEditProfileRemoteDataSource(
        impl: EditProfileRemoteDataSourceImpl
    ): EditProfileRemoteDataSource
}
