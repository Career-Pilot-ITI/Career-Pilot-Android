package com.iti.careerpilot.reports.data.datasource.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FeedbackReportDto(
    @SerialName("id")
    val id: Long,
    @SerialName("sessionId")
    val sessionId: Long,
    @SerialName("overallScore")
    val overallScore: Int,
    @SerialName("clarityScore")
    val clarityScore: Int,
    @SerialName("confidenceScore")
    val confidenceScore: Int,
    @SerialName("pacingScore")
    val pacingScore: Int,
    @SerialName("fillerWordsScore")
    val fillerWordsScore: Int,
    @SerialName("contentRelevanceScore")
    val contentRelevanceScore: Int,
    @SerialName("coachingTips")
    val coachingTips: List<String> = emptyList(),
    @SerialName("generatedAt")
    val generatedAt: String? = null,
    @SerialName("createdAt")
    val createdAt: String,
    @SerialName("questions")
    val questions: List<SessionQuestionDto> = emptyList(),
)
