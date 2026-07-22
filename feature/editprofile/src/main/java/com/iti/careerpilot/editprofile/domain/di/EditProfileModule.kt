package com.iti.careerpilot.editprofile.domain.di


import com.iti.careerpilot.editprofile.data.datasource.local.EditProfileLocalDataSourceImpl
import com.iti.careerpilot.editprofile.data.datasource.local.ImageCompressorImpl
import com.iti.careerpilot.editprofile.data.datasource.remote.EditProfileRemoteDataSourceImpl
import com.iti.careerpilot.editprofile.data.repo.EditProfileRepoImpl
import com.iti.careerpilot.editprofile.domain.datasource.local.EditProfileLocalDataSource
import com.iti.careerpilot.editprofile.domain.datasource.local.ImageCompressor
import com.iti.careerpilot.editprofile.domain.datasource.remote.EditProfileRemoteDataSource
import com.iti.careerpilot.editprofile.domain.repo.EditProfileRepo
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class EditProfileModule {

    @Binds
    abstract fun bindEditProfileLocalDataSource(
        editProfileLocalDataSourceImpl: EditProfileLocalDataSourceImpl
    ): EditProfileLocalDataSource

    @Binds
    abstract fun bindEditProfileRepo(
        profileRepoImpl: EditProfileRepoImpl
    ): EditProfileRepo

    @Binds
    abstract fun bindImageCompressor(
        imageCompressorImpl: ImageCompressorImpl
    ): ImageCompressor

}
