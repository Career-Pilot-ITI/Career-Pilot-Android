package com.iti.careerpilot.practicesession.data.datasource.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SessionDto(
    @SerialName("sessionId")
    val sessionId: Int?,

    @SerialName("trackName")
    val trackName: String?,

    @SerialName("targetDurationMinutes")
    val targetDurationMinutes: Int?,

    @SerialName("maxQuestions")
    val maxQuestions: Int?,

    @SerialName("startedAt")
    val startedAt: String?,

    @SerialName("currentQuestion")
    val firstQuestion: CurrentQuestionDto?
)

@Serializable
data class CurrentQuestionDto(
    @SerialName("id")
    val id: Int?,

    @SerialName("sessionId")
    val sessionId: Int?,

    @SerialName("questionText")
    val questionText: String?,

    @SerialName("questionOrder")
    val questionOrder: Int?,

    @SerialName("createdAt")
    val createdAt: String?
)
