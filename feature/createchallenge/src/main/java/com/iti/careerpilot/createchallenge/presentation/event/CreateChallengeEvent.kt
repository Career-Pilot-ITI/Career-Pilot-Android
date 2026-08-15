package com.iti.careerpilot.createchallenge.presentation.event

sealed interface CreateChallengeEvent {
    data object NavigateBack : CreateChallengeEvent
    data object NavigateToDashboard : CreateChallengeEvent
}
