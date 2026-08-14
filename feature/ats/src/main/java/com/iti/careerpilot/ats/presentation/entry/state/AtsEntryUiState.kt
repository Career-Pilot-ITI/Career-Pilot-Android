package com.iti.careerpilot.ats.presentation.entry.state

import androidx.compose.runtime.Immutable

@Immutable
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
