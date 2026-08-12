package com.iti.careerpilot.quiz.domain.repository

import com.iti.careerpilot.quiz.domain.model.LearningPoint
import com.iti.careerpilot.quiz.domain.model.LearningPointResponse
import com.iti.careerpilot.quiz.domain.model.LearningQuiz
import com.iti.careerpilot.quiz.domain.model.StudyTopic
import com.iti.common.error.NetworkError
import com.iti.common.result.CareerPilotResult

interface QuizRepository {
    suspend fun generateTopics(
        track: String,
        seniority: String
    ): CareerPilotResult<List<StudyTopic>, NetworkError>

    suspend fun generateNextLearningPoint(
        track: String,
        seniority: String,
        topic: String,
        coveredConcepts: List<String>
    ): CareerPilotResult<LearningPointResponse, NetworkError>

    suspend fun generateQuiz(
        topic: String,
        learningPoint: LearningPoint
    ): CareerPilotResult<LearningQuiz, NetworkError>
}
