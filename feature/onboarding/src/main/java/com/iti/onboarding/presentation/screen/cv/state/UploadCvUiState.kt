package com.iti.onboarding.presentation.screen.cv.state

import androidx.compose.runtime.Immutable
import com.iti.careerpilot.core.designsystem.components.CvUploadCardStage

@Immutable
data class UploadCvUiState(
    val selectedFile: SelectedCvUiModel? = null,
    val stage: CvUploadCardStage = CvUploadCardStage.EMPTY,
    val uploadProgress: Int = 0,
) {
    val isSubmitting: Boolean
        get() = stage == CvUploadCardStage.PREPARING ||
            stage == CvUploadCardStage.UPLOADING ||
            stage == CvUploadCardStage.PARSING

    val canAnalyze: Boolean
        get() = stage == CvUploadCardStage.UPLOADED

    val isFormValid: Boolean
        get() = canAnalyze
}

@Immutable
data class SelectedCvUiModel(
    val fileId: Long,
    val name: String,
    val sizeBytes: Long,
)
