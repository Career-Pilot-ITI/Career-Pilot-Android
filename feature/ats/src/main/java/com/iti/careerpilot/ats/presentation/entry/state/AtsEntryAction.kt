package com.iti.careerpilot.ats.presentation.entry.state

sealed interface AtsEntryAction {
    data object Initial : AtsEntryAction
    data class JobUrlChanged(val value: String) : AtsEntryAction
    data class SharedTextReceived(val value: String) : AtsEntryAction
    data object EditProfileClicked : AtsEntryAction
    data object CompareClicked : AtsEntryAction
}
