package com.iti.careerpilot.quiz.domain.model

data class LearningPoint(
    val title: String,
    val explanation: String,
    val example: String
)


data class LearningPointResponse(
    val topicCompleted: Boolean,
    val coveredConcept: String?,
    val learningPoint: LearningPoint?
)
