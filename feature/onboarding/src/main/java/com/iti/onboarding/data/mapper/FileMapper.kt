package com.iti.onboarding.data.mapper

import com.iti.onboarding.data.dto.UploadFileResponseDto
import com.iti.onboarding.domain.model.UploadedFile

fun UploadFileResponseDto.toDomain(): UploadedFile = UploadedFile(
    id = id,
    url = url,
    type = type,
)
