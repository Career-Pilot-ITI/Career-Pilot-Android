package com.iti.careerpilot.ats.presentation.coverletter.state

sealed interface CoverLetterIntent {
    data class Initial(val workspaceId: Long) : CoverLetterIntent
    data object Retry : CoverLetterIntent
    data object ToggleEditing : CoverLetterIntent
    data class EditedValueChanged(val value: String) : CoverLetterIntent
    data object Copy : CoverLetterIntent
    data object Email : CoverLetterIntent
    data object OpenCoins : CoverLetterIntent
    data object DismissGateSheet : CoverLetterIntent
    data object DismissCoinTopUpSheet : CoverLetterIntent
    data object UpgradeFromGate : CoverLetterIntent
    data object BuyCoinsClicked : CoverLetterIntent
}
