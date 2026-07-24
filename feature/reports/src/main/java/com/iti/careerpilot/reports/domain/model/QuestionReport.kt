package com.iti.careerpilot.reports.domain.model

data class QuestionReport(
    val id: String,
    val index: Int,
    val question: String,
    val score: Int,
    val fillerWordCount: Int,
    val durationSeconds: Int,
    val coachFeedback: String,
    val transcript: String,
    val fillerWords: List<String>,
)