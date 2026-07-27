package com.iti.careerpilot.home.presentation.interviews

import com.iti.careerpilot.home.domain.model.InterviewTrack
import com.iti.common.util.UIText
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class InterviewsState(
    val isLoading: Boolean = false,
    val query: String = "",

    val tracks: ImmutableList<InterviewTrack> = persistentListOf(),
    val hasLoadedTracks: Boolean = false,
    val error: UIText? = null,
)