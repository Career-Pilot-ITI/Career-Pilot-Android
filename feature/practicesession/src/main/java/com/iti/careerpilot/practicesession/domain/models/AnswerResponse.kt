package com.iti.careerpilot.practicesession.domain.models

data class AnswerResponse(
    val sessionStatus: String,
    val score: Score?,
    val nextQuestion: CurrentQuestion?
)

data class Score(
    val id: Long,
    val sessionQuestionId: Long,
    val contentRelevance: Int,
    val clarity: Int,
    val confidence: Int,
    val pacing: Int,
    val fillerWords: Int,
    val overallScore: Int,
    val coachingTip: String,
    val createdAt: String
)
