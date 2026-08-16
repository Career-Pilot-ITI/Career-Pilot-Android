package com.iti.careerpilot.ats.presentation.entry.state

sealed interface AtsEntryIntent {
    data object Initial : AtsEntryIntent
    data class JobUrlChanged(val value: String) : AtsEntryIntent
    data class SharedTextReceived(val value: String) : AtsEntryIntent
    data object SelectCvClicked : AtsEntryIntent
    data class PdfSelected(val uri: String) : AtsEntryIntent
    data object CompareClicked : AtsEntryIntent
    data object DismissGateSheet : AtsEntryIntent
    data object DismissCoinTopUpSheet : AtsEntryIntent
}
