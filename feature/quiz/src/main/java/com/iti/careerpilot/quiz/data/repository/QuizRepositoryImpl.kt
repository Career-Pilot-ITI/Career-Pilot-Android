package com.iti.careerpilot.quiz.data.repository

import com.iti.careerpilot.quiz.data.remote.QuizRemoteDataSource
import com.iti.careerpilot.quiz.domain.repository.QuizRepository
import javax.inject.Inject

class QuizRepositoryImpl @Inject constructor(
    private val remoteDataSource: QuizRemoteDataSource
) : QuizRepository {
}
