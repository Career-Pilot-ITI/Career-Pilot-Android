package com.iti.careerpilot.quiz.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class TopicsResponseDto(
    val topics: List<StudyTopicDto>
)

@Serializable
data class StudyTopicDto(
    val id: String,
    val title: String,
    val description: String
)

@Serializable
data class LearningPointResponseDto(
    val topicCompleted: Boolean,
    val coveredConcept: String? = null,
    val learningPoint: LearningPointDto? = null
)

@Serializable
data class LearningPointDto(
    val title: String,
    val explanation: String,
    val example: String
)

@Serializable
data class QuizResponseDto(
    val questions: List<QuizQuestionDto>
)

@Serializable
data class QuizQuestionDto(
    val id: String,
    val question: String,
    val options: List<String>,
    val correctAnswerIndex: Int,
    val explanation: String
)
