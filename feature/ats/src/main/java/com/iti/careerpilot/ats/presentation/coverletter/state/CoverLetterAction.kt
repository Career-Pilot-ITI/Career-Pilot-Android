package com.iti.careerpilot.ats.presentation.coverletter.state

sealed interface CoverLetterAction {
    data class Initial(val workspaceId: Long) : CoverLetterAction
    data object Retry : CoverLetterAction
    data object ToggleEditing : CoverLetterAction
    data class EditedValueChanged(val value: String) : CoverLetterAction
    data object Copy : CoverLetterAction
    data object Email : CoverLetterAction
    data object OpenCoins : CoverLetterAction
    data object DismissGateSheet : CoverLetterAction
    data object DismissCoinTopUpSheet : CoverLetterAction
    data object UpgradeFromGate : CoverLetterAction
    data object BuyCoinsClicked : CoverLetterAction
}
