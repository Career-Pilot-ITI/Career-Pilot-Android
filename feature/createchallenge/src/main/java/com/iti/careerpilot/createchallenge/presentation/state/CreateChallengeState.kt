package com.iti.careerpilot.createchallenge.presentation.state

import com.iti.careerpilot.createchallenge.domain.models.ChallengeVisibility
import com.iti.careerpilot.createchallenge.domain.models.ChallengeType
import com.iti.careerpilot.createchallenge.domain.models.SeniorityLevel
import com.iti.core.model.Track
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class CreateChallengeState(
    val tracks: ImmutableList<Track> = persistentListOf(),
    val selectedTrack: Track? = null,
    val visibility: ChallengeVisibility = ChallengeVisibility.PUBLIC,
    val seniorityLevel: SeniorityLevel = SeniorityLevel.JUNIOR,
    val challengeType: ChallengeType = ChallengeType.AUDIO_ONLY,
    val analyzePosture: Boolean = false,
    val analyzeHands: Boolean = false,
    val questions: ImmutableList<String> = persistentListOf("", "", "", "", "", "", "", "", "", ""),
    
    val isLoadingTracks: Boolean = false,
    val isSubmitting: Boolean = false,
    val invitationCode: String? = null,
    val questionToDeleteIndex: Int? = null
)
