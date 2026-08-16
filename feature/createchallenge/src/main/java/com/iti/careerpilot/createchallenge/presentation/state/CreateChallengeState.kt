package com.iti.careerpilot.createchallenge.presentation.state

import com.iti.core.model.ChallengeType
import com.iti.core.model.ChallengeVisibility
import com.iti.core.model.SeniorityLevel
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

    val isLoading: Boolean = false,
    val isLoadingTracks: Boolean = false,
    val isSubmitting: Boolean = false,
    val error: String? = null,
    val invitationCode: String? = null,
    val isSuccessDialogVisible: Boolean = false,
    val questionToDeleteIndex: Int? = null,
    val isEditMode: Boolean = false,
    val existingChallengeId: String? = null
)
