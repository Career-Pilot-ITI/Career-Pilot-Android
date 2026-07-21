package com.iti.careerpilot.practicesession.data.datasource.mapper

import com.iti.careerpilot.practicesession.data.datasource.models.SessionDto
import com.iti.careerpilot.practicesession.domain.models.Session

fun SessionDto.toDomain(): Session {
    return Session(
        id = id,
    )
}