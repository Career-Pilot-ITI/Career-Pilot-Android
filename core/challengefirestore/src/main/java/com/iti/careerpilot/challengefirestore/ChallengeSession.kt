package com.iti.careerpilot.challengefirestore

import kotlinx.serialization.Serializable

@Serializable
data class ChallengeSession(
    val sessionId: String = "",
    val challengeId: String = "",
    val challengeTitle: String = "",
    val participantId: Long = 0,
    val participantEmail: String = "",
    val participantName: String = "",
    val trackName: String = "",
    val status: String = "IN_PROGRESS",
    val targetDurationMinutes: Int = 0,
    val maxQuestions: Int = 0,
    val answeredCount: Int = 0,
    val startedAt: String = "",
    val updatedAt: String = "",
    val currentQuestion: ChallengeCurrentQuestion? = null,
    val overallScore: Int? = null,
    val clarityScore: Int? = null,
    val confidenceScore: Int? = null,
    val pacingScore: Int? = null,
    val fillerWordsScore: Int? = null,
    val contentRelevanceScore: Int? = null,
    val coachingTips: List<String> = emptyList(),
    val feedback: String? = null,
    val timestamp: Long = 0,
    val results: List<ChallengeQuestionResult> = emptyList()
)

@Serializable
data class ChallengeCurrentQuestion(
    val id: String = "",
    val questionText: String = "",
    val questionOrder: Int = 0,
    val createdAt: String = ""
)

@Serializable
data class ChallengeQuestionResult(
    val id: String = "",
    val questionId: String = "",
    val questionText: String = "",
    val questionOrder: Int = 0,
    val userTranscript: String = "",
    val answerAudioUrl: String = "",
    val durationMs: Long = 0,
    val speechRateWpm: Double = 0.0,
    val avgPauseMs: Double = 0.0,
    val silenceRatio: Double = 0.0,
    val createdAt: String = "",
    val completedAt: String = "",
    val score: ChallengeScore? = null
)

@Serializable
data class ChallengeScore(
    val contentRelevance: Int = 0,
    val clarity: Int = 0,
    val confidence: Int = 0,
    val pacing: Int = 0,
    val fillerWords: Int = 0,
    val overallScore: Int = 0,
    val coachingTip: String = "",
    val createdAt: String = ""
)
