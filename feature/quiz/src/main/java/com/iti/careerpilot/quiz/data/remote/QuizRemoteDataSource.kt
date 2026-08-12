package com.iti.careerpilot.quiz.data.remote

import com.iti.careerpilot.quiz.data.remote.dto.LearningPointResponseDto
import com.iti.careerpilot.quiz.data.remote.dto.QuizResponseDto
import com.iti.careerpilot.quiz.data.remote.dto.TopicsResponseDto
import com.iti.common.error.FirebaseError
import com.iti.common.result.CareerPilotResult

interface QuizRemoteDataSource {
    suspend fun generateTopics(
        track: String,
        seniority: String
    ): CareerPilotResult<TopicsResponseDto, FirebaseError>

    suspend fun generateNextLearningPoint(
        track: String,
        seniority: String,
        topic: String,
        coveredConcepts: List<String>
    ): CareerPilotResult<LearningPointResponseDto, FirebaseError>

    suspend fun generateQuiz(
        topic: String,
        learningPointTitle: String,
        learningPointExplanation: String,
        learningPointExample: String
    ): CareerPilotResult<QuizResponseDto, FirebaseError>
}
