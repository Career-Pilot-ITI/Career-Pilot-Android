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
    val score: Int? = null,
    val feedback: String? = null,
    val timestamp: Long = 0,
    val results: List<ChallengeQuestionResult> = emptyList()
)

@Serializable
data class ChallengeQuestionResult(
    val questionId: String = "",
    val questionText: String = "",
    val answerTranscription: String = "",
    val answerAudioUrl: String = "",
    val score: Int = 0,
    val feedback: String = ""
)
