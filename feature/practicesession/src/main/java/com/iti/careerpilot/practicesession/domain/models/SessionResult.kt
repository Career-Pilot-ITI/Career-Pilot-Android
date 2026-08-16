package com.iti.careerpilot.practicesession.domain.models

data class SessionResult(
    val id: Long,
    val sessionId: Long,
    val participantName: String? = null,
    val participantEmail: String? = null,
    val overallScore: Int,
    val clarityScore: Int,
    val confidenceScore: Int,
    val pacingScore: Int,
    val fillerWordsScore: Int,
    val contentRelevanceScore: Int,
    val coachingTips: List<String>,
    val generatedAt: String,
    val createdAt: String,
    val questions: List<SessionQuestionResult>
)

data class SessionQuestionResult(
    val id: Long,
    val sessionId: Long,
    val questionText: String,
    val questionOrder: Int,
    val userTranscript: String,
    val durationMs: Long,
    val speechRateWpm: Double,
    val avgPauseMs: Double,
    val silenceRatio: Double,
    val createdAt: String,
    val completedAt: String,
    val score: Score?
)
