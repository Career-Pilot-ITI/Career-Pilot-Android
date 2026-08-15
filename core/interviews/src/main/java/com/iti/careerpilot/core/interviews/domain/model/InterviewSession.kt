package com.iti.careerpilot.core.interviews.domain.model

import java.time.Instant

data class InterviewSession(
    val id: Long,
    val trackId: Long?,
    val trackName: String,
    val status: SessionStatus,
    val score: Int?,
    val occurredAt: Instant?,
    val durationMinutes: Int,
    val questionCount: Int,
) {
    val isCompleted: Boolean get() = status == SessionStatus.COMPLETED

    val isResumable: Boolean
        get() = status == SessionStatus.IN_PROGRESS || status == SessionStatus.PAUSED
}

enum class SessionStatus {
    IN_PROGRESS,
    PAUSED,
    COMPLETED,
    ABANDONED,
    UNKNOWN;

    companion object {
        fun fromApi(raw: String?): SessionStatus =
            entries.firstOrNull { it.name.equals(raw, ignoreCase = true) } ?: UNKNOWN
    }
}
