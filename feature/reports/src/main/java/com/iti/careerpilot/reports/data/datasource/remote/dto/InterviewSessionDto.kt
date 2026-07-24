package com.iti.careerpilot.reports.data.datasource.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class InterviewSessionDto(
    @SerialName("id")
    val id: Long,
    @SerialName("trackId")
    val trackId: Long? = null,
    @SerialName("trackName")
    val trackName: String? = null,
    @SerialName("status")
    val status: String,
    @SerialName("overallScore")
    val overallScore: Int? = null,
    @SerialName("durationSeconds")
    val durationSeconds: Int? = null,
    @SerialName("targetDurationMinutes")
    val targetDurationMinutes: Int? = null,
    @SerialName("maxQuestions")
    val maxQuestions: Int? = null,
    @SerialName("startedAt")
    val startedAt: String? = null,
    @SerialName("completedAt")
    val completedAt: String? = null,
    @SerialName("createdAt")
    val createdAt: String,
)
