package com.iti.careerpilot.ats.presentation.entry.state

sealed interface AtsEntryIntent {
    data object Initial : AtsEntryIntent
    data class JobUrlChanged(val value: String) : AtsEntryIntent
    data class SharedTextReceived(val value: String) : AtsEntryIntent
    data object EditProfileClicked : AtsEntryIntent
    data object CompareClicked : AtsEntryIntent
    data object DismissGateSheet : AtsEntryIntent
    data object DismissCoinTopUpSheet : AtsEntryIntent
}
