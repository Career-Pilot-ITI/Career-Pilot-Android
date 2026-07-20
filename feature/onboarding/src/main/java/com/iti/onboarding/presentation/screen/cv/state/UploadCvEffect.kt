package com.iti.onboarding.presentation.screen.cv.state

import com.iti.common.util.UIText

sealed interface UploadCvEffect {
    data object OpenPdfPicker : UploadCvEffect
    data object NavigateNext : UploadCvEffect
    data object Skip : UploadCvEffect
    data class ShowError(val message: UIText) : UploadCvEffect
}
