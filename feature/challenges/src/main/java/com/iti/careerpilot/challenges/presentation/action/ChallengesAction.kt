package com.iti.careerpilot.challenges.presentation.action

sealed interface ChallengesAction {
    data object Initial : ChallengesAction
    data object Refresh : ChallengesAction
    data class OnSearchQueryChange(val query: String) : ChallengesAction
    data object TogglePrivateCodeDialog : ChallengesAction
    data class OnPrivateCodeChange(val code: String) : ChallengesAction
    data object SubmitPrivateCode : ChallengesAction
    data class OnChallengeClicked(val challengeId: String) : ChallengesAction
    data object CreateChallengeClicked : ChallengesAction
    data object ChallengeDashboardClicked : ChallengesAction
}
