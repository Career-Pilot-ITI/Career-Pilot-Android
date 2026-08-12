package com.iti.careerpilot.quiz.domain.model

data class StudyTopic(
    val id: String,
    val title: String,
    val description: String,
    val progress: Int = 0,
    val isCompleted: Boolean = false
)
