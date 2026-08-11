package com.iti.careerpilot.quiz.di

import com.iti.careerpilot.quiz.data.remote.QuizRemoteDataSource
import com.iti.careerpilot.quiz.data.remote.FakeQuizRemoteDataSource
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class FakeQuizDataModule {

    @Binds
    @Singleton
    abstract fun bindQuizRemoteDataSource(
        fakeQuizRemoteDataSource: FakeQuizRemoteDataSource
    ): QuizRemoteDataSource
}
