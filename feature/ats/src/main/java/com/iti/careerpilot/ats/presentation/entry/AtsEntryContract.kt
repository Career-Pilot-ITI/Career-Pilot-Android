package com.iti.careerpilot.ats.presentation.entry

import com.iti.common.util.UIText

data class AtsEntryUiState(
    val jobUrl: String = "",
    val isUrlValid: Boolean = false,
    val cvFileName: String = "",
    val cvSizeBytes: Long = 0L,
    val hasSynchronizedCv: Boolean = false,
    val isUploadingCv: Boolean = false,
    val uploadProgress: Int = 0,
    val isImporting: Boolean = false,
) {
    val isBusy: Boolean get() = isUploadingCv || isImporting
    val canCompare: Boolean get() = isUrlValid && hasSynchronizedCv && !isBusy
}

sealed interface AtsEntryAction {
    data class JobUrlChanged(val value: String) : AtsEntryAction
    data class SharedTextReceived(val value: String) : AtsEntryAction
    data object SelectCvClicked : AtsEntryAction
    data class PdfSelected(val uri: String) : AtsEntryAction
    data object CompareClicked : AtsEntryAction
}

sealed interface AtsEntryEffect {
    data object OpenPdfPicker : AtsEntryEffect
    data class NavigateToJobDetails(val workspaceId: Long) : AtsEntryEffect
    data class ShowMessage(val message: UIText) : AtsEntryEffect
}
