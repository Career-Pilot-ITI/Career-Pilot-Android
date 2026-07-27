package com.iti.careerpilot.features.paywall.di

import com.iti.careerpilot.features.paywall.data.remote.UserSyncManager
import com.iti.careerpilot.features.paywall.data.repository.PaymentRepositoryImpl
import com.iti.careerpilot.features.paywall.domain.repository.PaymentRepository
import com.iti.core.datastore.sync.UserProfileSync
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class PaymentModule {
    @Binds
    @Singleton
    abstract fun bindPaymentRepository(impl: PaymentRepositoryImpl): PaymentRepository

    @Binds
    @Singleton
    abstract fun bindUserProfileSync(impl: UserSyncManager): UserProfileSync
}


