package com.iti.onboarding.presentation.screen.cv.state

sealed interface UploadCvIntent {
    data object OnUploadAreaClick : UploadCvIntent
    data class OnPdfSelected(val uri: String) : UploadCvIntent
    data object OnAnalyzeClick : UploadCvIntent
    data object OnSkipClick : UploadCvIntent
}
