package com.iti.careerpilot.reports.data.datasource.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SessionQuestionDto(
    @SerialName("id")
    val id: Long,
    @SerialName("sessionId")
    val sessionId: Long,
    @SerialName("questionText")
    val questionText: String,
    @SerialName("questionOrder")
    val questionOrder: Int,
    @SerialName("userTranscript")
    val userTranscript: String? = null,
    @SerialName("durationMs")
    val durationMs: Long? = null,
    @SerialName("speechRateWpm")
    val speechRateWpm: Double? = null,
    @SerialName("avgPauseMs")
    val avgPauseMs: Double? = null,
    @SerialName("silenceRatio")
    val silenceRatio: Double? = null,
    @SerialName("createdAt")
    val createdAt: String,
    @SerialName("completedAt")
    val completedAt: String? = null,
    @SerialName("score")
    val score: QuestionScoreDto? = null,
)
