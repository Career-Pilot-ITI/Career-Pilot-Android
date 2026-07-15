package com.iti.core.database.di

import android.content.Context
import androidx.room.Room
import com.iti.core.database.CareerPilotDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): CareerPilotDatabase =
        Room.databaseBuilder(
            context,
            CareerPilotDatabase::class.java,
            "career-pilot-database",
        ).build()

    // DAO providers are added here once a feature registers an entity — not part of this task.
}
