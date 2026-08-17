package com.iti.careerpilot.createchallenge.presentation.action

import com.iti.careerpilot.challengefirestore.ChallengeType
import com.iti.careerpilot.challengefirestore.ChallengeVisibility
import com.iti.careerpilot.challengefirestore.SeniorityLevel
import com.iti.core.model.Track


sealed interface CreateChallengeAction {
    data class Initial(val challengeId: String? = null) : CreateChallengeAction
    data class OnTrackSelected(val track: Track) : CreateChallengeAction
    data class OnVisibilityChanged(val visibility: ChallengeVisibility) : CreateChallengeAction
    data class OnSeniorityLevelChanged(val level: SeniorityLevel) : CreateChallengeAction
    data class OnChallengeTypeChanged(val type: ChallengeType) : CreateChallengeAction
    data class OnPostureToggle(val enabled: Boolean) : CreateChallengeAction
    data class OnHandsToggle(val enabled: Boolean) : CreateChallengeAction
    data class OnQuestionTextChange(val index: Int, val text: String) : CreateChallengeAction
    data object OnAddQuestion : CreateChallengeAction
    data class OnRemoveQuestion(val index: Int) : CreateChallengeAction
    data object OnConfirmDeleteQuestion : CreateChallengeAction
    data object OnDismissDeleteConfirmation : CreateChallengeAction
    data object OnSubmit : CreateChallengeAction
    data object OnDismissError : CreateChallengeAction
    data object OnDismissSuccess : CreateChallengeAction
    data object OnShareSuccessChallenge : CreateChallengeAction
    data object OnDismissShareDialog : CreateChallengeAction
    data object OnBackClicked : CreateChallengeAction
}

