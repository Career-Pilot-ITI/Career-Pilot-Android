package com.iti.careerpilot.createchallenge.domain.models

import kotlinx.serialization.Serializable

@Serializable
data class Challenge(
    val id: String = "",
    val creatorId: String = "",
    val creatorName: String = "",
    val trackId: Long = 0,
    val trackName: String = "",
    val visibility: ChallengeVisibility = ChallengeVisibility.PUBLIC,
    val seniorityLevel: SeniorityLevel = SeniorityLevel.JUNIOR,
    val creationDate: Long = 0,
    val invitationCode: String = "",
    val questions: List<ChallengeQuestion> = emptyList(),
    val type: ChallengeType = ChallengeType.AUDIO_ONLY,
    val videoAnalysisConfig: VideoAnalysisConfig? = null
)

@Serializable
enum class ChallengeVisibility {
    PUBLIC, PRIVATE
}

@Serializable
enum class SeniorityLevel {
    INTERN, JUNIOR, MID_LEVEL, SENIOR, LEAD
}

@Serializable
data class ChallengeQuestion(
    val id: String = "",
    val text: String = ""
)

@Serializable
enum class ChallengeType {
    AUDIO_ONLY, VIDEO_AND_AUDIO
}

@Serializable
data class VideoAnalysisConfig(
    val analyzeFace: Boolean = true,
    val analyzePosture: Boolean = false,
    val analyzeHands: Boolean = false
)
