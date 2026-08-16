package com.iti.careerpilot.ats.presentation.entry.state

sealed interface AtsEntryAction {
    data object Initial : AtsEntryAction
    data class JobUrlChanged(val value: String) : AtsEntryAction
    data class SharedTextReceived(val value: String) : AtsEntryAction
    data object SelectCvClicked : AtsEntryAction
    data class PdfSelected(val uri: String) : AtsEntryAction
    data object CompareClicked : AtsEntryAction
    data object DismissGateSheet : AtsEntryAction
    data object DismissCoinTopUpSheet : AtsEntryAction
}
