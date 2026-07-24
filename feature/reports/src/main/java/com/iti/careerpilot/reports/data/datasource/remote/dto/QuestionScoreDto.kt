package com.iti.careerpilot.reports.data.datasource.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class QuestionScoreDto(
    @SerialName("id")
    val id: Long,
    @SerialName("sessionQuestionId")
    val sessionQuestionId: Long,
    @SerialName("contentRelevance")
    val contentRelevance: Int,
    @SerialName("clarity")
    val clarity: Int,
    @SerialName("confidence")
    val confidence: Int,
    @SerialName("pacing")
    val pacing: Int,
    @SerialName("fillerWords")
    val fillerWords: Int,
    @SerialName("overallScore")
    val overallScore: Int,
    @SerialName("coachingTip")
    val coachingTip: String? = null,
    @SerialName("createdAt")
    val createdAt: String,
)
