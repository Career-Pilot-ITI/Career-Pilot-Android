package com.iti.careerpilot.practicesession.data.datasource.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AnswerRequestDto(
    @SerialName("transcript")
    val transcript: String?,
    @SerialName("sessionElapsedSeconds")
    val sessionElapsedSeconds: Int?,
    @SerialName("durationMs")
    val durationMs: Long?,
    @SerialName("audioUrl")
    val audioUrl: String?,
    @SerialName("words")
    val words: List<WordDto>?
)

@Serializable
data class WordDto(
    @SerialName("word")
    val word: String?,
    @SerialName("startMs")
    val startMs: Int?,
    @SerialName("endMs")
    val endMs: Int?
)
