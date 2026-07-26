package com.iti.careerpilot.practicesession.data.datasource.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OldSessionDto(
    @SerialName("sessionId")
    val sessionId: Long?,
    @SerialName("status")
    val status: String?,
    @SerialName("trackName")
    val trackName: String?,
    @SerialName("startedAt")
    val startedAt: String?,
    @SerialName("updatedAt")
    val updatedAt: String?,
    @SerialName("answeredCount")
    val answeredCount: Int?,
    @SerialName("totalCount")
    val totalCount: Int?,
    @SerialName("answeredQuestions")
    val answeredQuestions: List<SessionQuestionResultDto>?,
    @SerialName("currentQuestion")
    val currentQuestion: CurrentQuestionDto?
)
