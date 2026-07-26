package com.iti.careerpilot.features.paywall.di

import com.iti.careerpilot.features.paywall.data.remote.PaymentRemoteDataSource
import com.iti.careerpilot.features.paywall.data.remote.PaymentRemoteDataSourceImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ProdPaymentModule {
    @Binds
    @Singleton
    abstract fun bindPaymentRemoteDataSource(
        impl: PaymentRemoteDataSourceImpl,
    ): PaymentRemoteDataSource
}
