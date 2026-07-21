package com.iti.careerpilot.practicesession.presentation.action

sealed interface PracticeSessionAction {
    data class StartPracticeSession(
        val sessionId: String
    ) : PracticeSessionAction
}