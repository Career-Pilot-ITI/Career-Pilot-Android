package com.iti.careerpilot.quiz.data.remote

import com.iti.careerpilot.quiz.data.remote.dto.LearningPointResponseDto
import com.iti.careerpilot.quiz.data.remote.dto.QuizResponseDto
import com.iti.careerpilot.quiz.data.remote.dto.TopicsResponseDto

interface QuizRemoteDataSource {
    suspend fun generateTopics(
        track: String,
        seniority: String
    ): TopicsResponseDto

    suspend fun generateNextLearningPoint(
        track: String,
        seniority: String,
        topic: String,
        coveredConcepts: List<String>
    ): LearningPointResponseDto

    suspend fun generateQuiz(
        topic: String,
        learningPointTitle: String,
        learningPointExplanation: String,
        learningPointExample: String
    ): QuizResponseDto
}
