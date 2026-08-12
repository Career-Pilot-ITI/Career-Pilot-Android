package com.iti.careerpilot.features.settings.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import com.iti.careerpilot.features.settings.data.datastore.DataStoreSerializer
import com.iti.careerpilot.features.settings.domain.models.UserSettings
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.io.File
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {

    @Singleton
    @Provides
    fun provideDataStore(
        @ApplicationContext context: Context,
        dataStoreSerializer: DataStoreSerializer
    ): DataStore<UserSettings> {
        return DataStoreFactory.create(
            serializer = dataStoreSerializer,
        ) {
            File(context.noBackupFilesDir, "datastore/user_settings.pb")
        }
    }
}