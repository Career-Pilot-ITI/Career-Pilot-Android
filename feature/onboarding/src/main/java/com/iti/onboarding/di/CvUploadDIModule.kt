package com.iti.onboarding.di

import com.iti.onboarding.data.local.datasource.CvFileLocalDataSource
import com.iti.onboarding.data.local.datasource.CvFileLocalDataSourceImpl
import com.iti.onboarding.data.remote.datasource.CvUploadRemoteDataSource
import com.iti.onboarding.data.remote.datasource.CvUploadRemoteDataSourceImpl
import com.iti.onboarding.data.repository.CvRepositoryImpl
import com.iti.onboarding.domain.repository.CvRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class CvUploadDIModule {

    @Binds
    @Singleton
    abstract fun bindCvFileLocalDataSource(
        implementation: CvFileLocalDataSourceImpl,
    ): CvFileLocalDataSource

    @Binds
    @Singleton
    abstract fun bindCvUploadRemoteDataSource(
        implementation: CvUploadRemoteDataSourceImpl,
    ): CvUploadRemoteDataSource

    @Binds
    @Singleton
    abstract fun bindCvRepository(
        implementation: CvRepositoryImpl,
    ): CvRepository
}
