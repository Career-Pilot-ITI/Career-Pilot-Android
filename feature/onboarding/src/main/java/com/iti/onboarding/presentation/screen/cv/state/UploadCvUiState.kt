package com.iti.onboarding.presentation.screen.cv.state

import androidx.compose.runtime.Immutable

@Immutable
data class UploadCvUiState(
    val selectedFile: SelectedCvUiModel? = null,
    val stage: CvUploadStage = CvUploadStage.EMPTY,
    val uploadProgress: Float = 0f,
) {
    val isBusy: Boolean
        get() = stage == CvUploadStage.PREPARING ||
            stage == CvUploadStage.UPLOADING

    val canAnalyze: Boolean
        get() = stage == CvUploadStage.UPLOADED
}

@Immutable
data class SelectedCvUiModel(
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
