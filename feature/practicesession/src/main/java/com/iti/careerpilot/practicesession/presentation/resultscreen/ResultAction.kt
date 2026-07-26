package com.iti.careerpilot.practicesession.presentation.resultscreen

sealed interface ResultAction {
    data class UpdateSessionId(
        val sessionId: Long
    ) : ResultAction
    data object RefreshResult : ResultAction
}