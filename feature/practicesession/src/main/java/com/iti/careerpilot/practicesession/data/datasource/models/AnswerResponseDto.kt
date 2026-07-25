package com.iti.careerpilot.practicesession.data.datasource.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AnswerResponseDto(
    @SerialName("sessionStatus")
    val sessionStatus: String?,
    @SerialName("score")
    val score: ScoreDto?,
    @SerialName("nextQuestion")
    val nextQuestion: CurrentQuestionDto?
)

@Serializable
data class ScoreDto(
    @SerialName("id")
    val id: Long?,
    @SerialName("sessionQuestionId")
    val sessionQuestionId: Long?,
    @SerialName("contentRelevance")
    val contentRelevance: Int?,
    @SerialName("clarity")
    val clarity: Int?,
    @SerialName("confidence")
    val confidence: Int?,
    @SerialName("pacing")
    val pacing: Int?,
    @SerialName("fillerWords")
    val fillerWords: Int?,
    @SerialName("overallScore")
    val overallScore: Int?,
    @SerialName("coachingTip")
    val coachingTip: String?,
    @SerialName("createdAt")
    val createdAt: String?
)
