package com.iti.onboarding.presentation.screen.track.mapper

import com.iti.onboarding.domain.model.Track
import com.iti.onboarding.presentation.screen.track.uimodel.TrackUiModel

fun Track.toUiModel(): TrackUiModel {
    return TrackUiModel(
        name = name,
        id = id,
        selected = false
    )
}