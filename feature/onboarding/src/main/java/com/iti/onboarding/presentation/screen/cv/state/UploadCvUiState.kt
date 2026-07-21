package com.iti.onboarding.presentation.screen.cv.state

import androidx.compose.runtime.Immutable

@Immutable
data class UploadCvUiState(
    val selectedFile: SelectedCvUiModel? = null,
    val stage: CvUploadStage = CvUploadStage.EMPTY,
    val uploadProgress: Float = 0f,
) {
    val isSubmitting: Boolean
        get() = stage == CvUploadStage.PREPARING ||
            stage == CvUploadStage.UPLOADING

    val canAnalyze: Boolean
        get() = stage == CvUploadStage.UPLOADED

    val isFormValid: Boolean
        get() = canAnalyze
}

@Immutable
data class SelectedCvUiModel(
    val fileId: Long,
    val name: String,
    val sizeBytes: Long,
)

enum class CvUploadStage {
    EMPTY,
    PREPARING,
    SELECTED,
    UPLOADING,
    UPLOADED,
}
