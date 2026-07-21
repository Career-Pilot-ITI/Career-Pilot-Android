package com.iti.careerpilot.practicesession.domain.models

data class AudioAttachment(
    val id: Long,
    val type: String,
    val originalName: String,
    val url: String,
    val sizeBytes: Long,
    val createdAt: String
)
