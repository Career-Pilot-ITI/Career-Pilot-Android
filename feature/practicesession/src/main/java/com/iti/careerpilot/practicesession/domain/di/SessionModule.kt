package com.iti.careerpilot.practicesession.domain.di

import android.content.Context
import androidx.media3.exoplayer.ExoPlayer
import com.iti.careerpilot.practicesession.data.datasource.SessionRepoImpl
import com.iti.careerpilot.practicesession.data.datasource.remote.SessionRemoteDataSourceImpl
import com.iti.careerpilot.practicesession.domain.repo.SessionRemoteDataSource
import com.iti.careerpilot.practicesession.domain.repo.SessionRepo
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SessionModule {

    @Binds
    abstract fun provideSessionRepo(
        sessionRepoImpl: SessionRepoImpl
    ): SessionRepo

    @Binds
    abstract fun provideSessionRemoteDataSource(
        sessionRemoteDataSourceImpl: SessionRemoteDataSourceImpl
    ): SessionRemoteDataSource

    @Provides
    @Singleton
    fun provideExoPlayer(@ApplicationContext context: Context): ExoPlayer {
        return ExoPlayer.Builder(context).build()
    }
}