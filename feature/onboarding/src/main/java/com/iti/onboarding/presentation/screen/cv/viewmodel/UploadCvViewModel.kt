package com.iti.onboarding.presentation.screen.cv.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iti.common.media.pdfpicker.PdfOperations
import com.iti.common.result.CareerPilotResult
import com.iti.common.util.toUIText
import com.iti.core.datastore.CareerPilotPreferencesDataSource
import com.iti.core.model.PdfFile
import com.iti.onboarding.domain.usecase.UploadCvUseCase
import com.iti.onboarding.presentation.screen.cv.state.CvUploadStage
import com.iti.onboarding.presentation.screen.cv.state.SelectedCvUiModel
import com.iti.onboarding.presentation.screen.cv.state.UploadCvEffect
import com.iti.onboarding.presentation.screen.cv.state.UploadCvIntent
import com.iti.onboarding.presentation.screen.cv.state.UploadCvUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicReference
import javax.inject.Inject

@HiltViewModel
class UploadCvViewModel @Inject constructor(
    private val uploadCv: UploadCvUseCase,
    private val pdfOperations: PdfOperations,
    private val datastore: CareerPilotPreferencesDataSource
) : ViewModel() {

    private companion object {
        const val UPLOAD_CHUNK_SIZE_BYTES = 256 * 1024
        const val UPLOAD_PROGRESS_DELAY_MS = 40L
    }

    private val _state = MutableStateFlow(UploadCvUiState())
    val state = _state.asStateFlow()

    private val _effects = MutableSharedFlow<UploadCvEffect>()
    val effects = _effects.asSharedFlow()

    private val uploadJob = AtomicReference<Job?>(null)

    fun onIntent(intent: UploadCvIntent) {
        when (intent) {
            UploadCvIntent.OnUploadAreaClick -> openPdfPicker()
            is UploadCvIntent.OnPdfSelected -> prepareAndUpload(intent.uri)
            UploadCvIntent.OnAnalyzeClick -> navigateAfterAnalysis()
            UploadCvIntent.OnSkipClick -> skipUpload()
        }
    }

    private fun openPdfPicker() {
        if (_state.value.isSubmitting) return

        viewModelScope.launch {
            _effects.emit(UploadCvEffect.OpenPdfPicker)
        }
    }

    private fun prepareAndUpload(uri: String) {
        uploadJob.getAndSet(viewModelScope.launch {
            _state.value = UploadCvUiState(
                stage = CvUploadStage.PREPARING,
            )

            launch {
                when (val result = pdfOperations.readPdf(uri)) {
                    is CareerPilotResult.Error -> {
                        _state.value = UploadCvUiState()

                        _effects.emit(
                            UploadCvEffect.ShowError(
                                result.error.toUIText(),
                            )
                        )
                    }

                    is CareerPilotResult.Success -> {
                        launch {
                            val totalBytes = result.data.bytes.size.coerceAtLeast(1)
                            var uploadedBytes = 0

                            while (uploadedBytes < totalBytes) {
                                delay(UPLOAD_PROGRESS_DELAY_MS)
                                uploadedBytes = minOf(
                                    uploadedBytes + UPLOAD_CHUNK_SIZE_BYTES,
                                    totalBytes,
                                )
                                val progress = uploadedBytes.toFloat() / totalBytes.toFloat()
                                _state.update {
                                    it.copy(
                                        uploadProgress = progress.coerceIn(0f, 1f),
                                    )
                                }
                            }
                        }

                        _state.update {
                            it.copy(
                                selectedFile = SelectedCvUiModel(
                                    name = result.data.name,
                                    sizeBytes = result.data.sizeBytes,
                                ),
                                stage = CvUploadStage.SELECTED,
                                uploadProgress = 0f,
                            )
                        }

                        uploadSelectedDocument(result.data)
                    }
                }
            }

            launch {
                when (
                    val result = pdfOperations.storePdfInternally(uri)
                ) {
                    is CareerPilotResult.Success<String> ->
                        datastore.setPdfInternalFileUri(result.data)

                    is CareerPilotResult.Error<*> -> Unit
                }
            }
        })?.cancel()
    }

    private suspend fun uploadSelectedDocument(document: PdfFile) {
        _state.update {
            it.copy(
                stage = CvUploadStage.UPLOADING,
                uploadProgress = 0f,
            )
        }

        when (
            val result = uploadCv(document)
        ) {
            is CareerPilotResult.Error -> {
                _state.update {
                    it.copy(
                        stage = CvUploadStage.SELECTED,
                        uploadProgress = 0f,
                    )
                }
                _effects.emit(
                    UploadCvEffect.ShowError(
                        result.error.toUIText(),
                    )
                )
            }

            is CareerPilotResult.Success -> {
                _state.update {
                    it.copy(
                        stage = CvUploadStage.UPLOADED,
                        uploadProgress = 1f,
                    )
                }
            }
        }
    }

    private fun navigateAfterAnalysis() {
        if (!_state.value.canAnalyze) return

        viewModelScope.launch {
            datastore.setHasCompletedOnboarding(true)
            _effects.emit(UploadCvEffect.NavigateNext)
        }
    }

    private fun skipUpload() {
        uploadJob.get()?.cancel()

        viewModelScope.launch {
            datastore.setHasCompletedOnboarding(true)
            _effects.emit(UploadCvEffect.Skip)
        }
    }
}
