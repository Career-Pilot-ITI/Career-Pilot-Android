package com.iti.careerpilot.practicesession.data.datasource.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SessionResultDto(
    @SerialName("id")
    val id: Long?,
    @SerialName("sessionId")
    val sessionId: Long?,
    @SerialName("overallScore")
    val overallScore: Int?,
    @SerialName("clarityScore")
    val clarityScore: Int?,
    @SerialName("confidenceScore")
    val confidenceScore: Int?,
    @SerialName("pacingScore")
    val pacingScore: Int?,
    @SerialName("fillerWordsScore")
    val fillerWordsScore: Int?,
    @SerialName("contentRelevanceScore")
    val contentRelevanceScore: Int?,
    @SerialName("coachingTips")
    val coachingTips: List<String>?,
    @SerialName("generatedAt")
    val generatedAt: String?,
    @SerialName("createdAt")
    val createdAt: String?,
    @SerialName("questions")
    val questions: List<SessionQuestionResultDto>?
)

@Serializable
data class SessionQuestionResultDto(
    @SerialName("id")
    val id: Long?,
    @SerialName("sessionId")
    val sessionId: Long?,
    @SerialName("questionText")
    val questionText: String?,
    @SerialName("questionOrder")
    val questionOrder: Int?,
    @SerialName("userTranscript")
    val userTranscript: String?,
    @SerialName("durationMs")
    val durationMs: Int?,
    @SerialName("speechRateWpm")
    val speechRateWpm: Double?,
    @SerialName("avgPauseMs")
    val avgPauseMs: Int?,
    @SerialName("silenceRatio")
    val silenceRatio: Double?,
    @SerialName("createdAt")
    val createdAt: String?,
    @SerialName("completedAt")
    val completedAt: String?,
    @SerialName("score")
    val score: ScoreDto?
)
