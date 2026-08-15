package com.iti.careerpilot.createchallenge.presentation.action

sealed interface CreateChallengeAction {
    data object OnBackClicked : CreateChallengeAction
}
