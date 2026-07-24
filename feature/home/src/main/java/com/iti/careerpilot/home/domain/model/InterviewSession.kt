package com.iti.careerpilot.home.domain.model

import java.time.Instant

data class InterviewSession(
    val id: Long,
    val trackName: String,
    val overallScore: Int?,
    val durationMinutes: Int,
    val occurredAt: Instant?,
    val status: SessionStatus,
) {
    val isCompleted: Boolean get() = status == SessionStatus.COMPLETED
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
