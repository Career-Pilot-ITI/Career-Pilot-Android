package com.iti.careerpilot.login.di

import com.google.i18n.phonenumbers.PhoneNumberUtil
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object PhoneValidationModule {

    @Provides
    @Singleton
    fun providePhoneNumberUtil(): PhoneNumberUtil = PhoneNumberUtil.getInstance()
}
