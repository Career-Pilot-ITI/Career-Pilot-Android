package com.iti.careerpilot.companyinterview.di

import com.iti.careerpilot.companyinterview.data.datasource.CompanyInterviewRemoteDataSource
import com.iti.careerpilot.companyinterview.data.datasource.CompanyInterviewRemoteDataSourceImpl
import com.iti.careerpilot.companyinterview.data.repository.CompanyInterviewRepositoryImpl
import com.iti.careerpilot.companyinterview.domain.repository.CompanyInterviewRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class CompanyInterviewModule {

    @Binds
    @Singleton
    abstract fun bindCompanyInterviewRemoteDataSource(
        impl: CompanyInterviewRemoteDataSourceImpl
    ): CompanyInterviewRemoteDataSource

    @Binds
    @Singleton
    abstract fun bindCompanyInterviewRepository(
        impl: CompanyInterviewRepositoryImpl
    ): CompanyInterviewRepository
}
