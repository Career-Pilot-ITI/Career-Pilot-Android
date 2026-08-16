package com.iti.careerpilot.core.access.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import com.iti.careerpilot.core.access.data.AccessRepositoryImpl
import com.iti.careerpilot.core.access.domain.AccessRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AccessModule {

    @Binds
    abstract fun bindAccessRepository(impl: AccessRepositoryImpl): AccessRepository

    companion object {
        @Provides
        @Singleton
        @AccessDataStore
        fun provideAccessDataStore(
            @ApplicationContext context: Context
        ): DataStore<Preferences> = PreferenceDataStoreFactory.create(
            produceFile = { context.preferencesDataStoreFile("access_preferences") }
        )
    }
}
