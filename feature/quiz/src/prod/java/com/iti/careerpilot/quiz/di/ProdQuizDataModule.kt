package com.iti.careerpilot.quiz.di

import com.iti.careerpilot.quiz.data.remote.QuizRemoteDataSource
import com.iti.careerpilot.quiz.data.remote.QuizRemoteDataSourceImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class ProdQuizDataModule {

    @Binds
    @Singleton
    abstract fun bindQuizRemoteDataSource(
        quizRemoteDataSourceImpl: QuizRemoteDataSourceImpl
    ): QuizRemoteDataSource
}
