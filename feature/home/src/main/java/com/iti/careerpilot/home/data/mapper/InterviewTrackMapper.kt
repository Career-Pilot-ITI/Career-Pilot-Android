package com.iti.careerpilot.home.data.mapper

import com.iti.careerpilot.home.data.datasource.remote.dto.TrackDto
import com.iti.careerpilot.home.domain.model.InterviewTrack

fun TrackDto.toDomain(): InterviewTrack? {
    val trackId = id ?: return null
    val trackName = name?.takeIf { it.isNotBlank() } ?: return null

    return InterviewTrack(
        id = trackId,
        name = trackName,
        description = description.orEmpty(),
    )
}

fun List<TrackDto>.toDomain(): List<InterviewTrack> = mapNotNull { it.toDomain() }
