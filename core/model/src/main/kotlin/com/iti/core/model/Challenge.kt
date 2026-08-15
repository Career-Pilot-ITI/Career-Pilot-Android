package com.iti.core.model

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

fun ChallengeVisibility.getTitleRes(): Int {
    return when (this) {
        ChallengeVisibility.PUBLIC -> R.string.challenge_visibility_public
        ChallengeVisibility.PRIVATE -> R.string.challenge_visibility_private
    }
}

@Serializable
enum class SeniorityLevel {
    INTERN, JUNIOR, MID_LEVEL, SENIOR, LEAD
}

fun SeniorityLevel.getTitleRes(): Int {
    return when (this) {
        SeniorityLevel.INTERN -> R.string.seniority_intern
        SeniorityLevel.JUNIOR -> R.string.seniority_junior
        SeniorityLevel.MID_LEVEL -> R.string.seniority_mid_level
        SeniorityLevel.SENIOR -> R.string.seniority_senior
        SeniorityLevel.LEAD -> R.string.seniority_lead
    }
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

fun ChallengeType.getTitleRes(): Int {
    return when (this) {
        ChallengeType.AUDIO_ONLY -> R.string.challenge_type_audio
        ChallengeType.VIDEO_AND_AUDIO -> R.string.challenge_type_video_audio
    }
}

@Serializable
data class VideoAnalysisConfig(
    val analyzeFace: Boolean = true,
    val analyzePosture: Boolean = false,
    val analyzeHands: Boolean = false
)
