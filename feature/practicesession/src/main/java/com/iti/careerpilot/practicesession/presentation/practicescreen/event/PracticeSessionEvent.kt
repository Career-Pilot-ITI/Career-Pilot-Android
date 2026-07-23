package com.iti.careerpilot.practicesession.presentation.practicescreen.event

import com.iti.common.util.UIText

sealed interface PracticeSessionEvent {
    data class NavigateToResult(val sessionId: Long) : PracticeSessionEvent
    data class ShowError(val message: UIText) : PracticeSessionEvent
}