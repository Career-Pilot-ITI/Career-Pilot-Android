package com.iti.onboarding.data.mapper

import com.iti.onboarding.data.remote.dto.TracksResponseDto
import com.iti.onboarding.domain.model.Track

fun TracksResponseDto.toDomain(): Track{
    return Track(
        id = id.toString(),
        name = name
    )
}