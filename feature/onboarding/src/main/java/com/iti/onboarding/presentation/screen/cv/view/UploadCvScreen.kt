package com.iti.onboarding.presentation.screen.cv.view

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.iti.careerpilot.core.designsystem.components.UploadProgressDialog
import com.iti.common.media.pdfpicker.rememberPdfPickerLauncher
import com.iti.common.snackbar.CareerPilotSnackbarController
import com.iti.onboarding.R
import com.iti.onboarding.presentation.screen.cv.state.CvUploadStage
import com.iti.onboarding.presentation.screen.cv.state.UploadCvEffect
import com.iti.onboarding.presentation.screen.cv.state.UploadCvIntent
import com.iti.onboarding.presentation.screen.cv.state.UploadCvUiState
import com.iti.onboarding.presentation.screen.cv.view.components.UploadCvCard
import com.iti.onboarding.presentation.screen.cv.view.components.UploadCvHeader
import com.iti.onboarding.presentation.screen.cv.viewmodel.UploadCvViewModel


@Composable
fun UploadCvScreen(
    onNavigateNext: () -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: UploadCvViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current

    val pdfPicker = rememberPdfPickerLauncher(
        onPdfSelected = { uri ->
            viewModel.onIntent(UploadCvIntent.OnPdfSelected(uri.toString()))
        }
    )

    LaunchedEffect(viewModel, lifecycleOwner) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.effects.collect { effect ->
                when (effect) {
                    UploadCvEffect.OpenPdfPicker -> {
                        pdfPicker.launchPdfPicker()
                    }

                    UploadCvEffect.NavigateNext -> onNavigateNext()
                    UploadCvEffect.Skip -> onSkip()
                    is UploadCvEffect.ShowError -> {
                        CareerPilotSnackbarController.show(
                            message = effect.message,
                        )
                    }
                }
            }
        }
    }

    UploadCvScreenContent(
        state = state,
        onIntent = viewModel::onIntent,
        modifier = modifier,
    )
    if (state.isSubmitting) {
        UploadProgressDialog(
            progress = state.uploadProgress,
            title = stringResource(R.string.uploading)
        )
    }
}

@Composable
fun UploadCvScreenContent(
    state: UploadCvUiState,
    onIntent: (UploadCvIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme

    LazyColumn(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp)
    ) {
        item {
            UploadCvHeader()
        }
        item {
            UploadCvCard(
                selectedFile = state.selectedFile,
                stage = state.stage,
                uploadProgress = state.uploadProgress,
                onClick = {
                    onIntent(UploadCvIntent.OnUploadAreaClick)
                },
            )
        }

        item {
            AnimatedVisibility(
                visible = state.stage == CvUploadStage.UPLOADED,
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.CheckCircle,
                        contentDescription = null,
                        tint = colors.secondary,
                    )

                    Text(
                        text = stringResource(R.string.cv_uploaded_successfully),
                        modifier = Modifier.padding(start = 8.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.secondary,
                    )
                }
            }
        }
        item {
            AnimatedVisibility(
                visible = state.stage != CvUploadStage.UPLOADED,
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                TextButton(
                    onClick = {
                        onIntent(UploadCvIntent.OnSkipClick)
                    },
                    enabled = !state.isSubmitting,
                    modifier = Modifier,
                ) {
                    Text(
                        text = stringResource(R.string.skip_for_now),
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.onSurfaceVariant,
                    )

                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                        contentDescription = null,
                        tint = colors.onSurfaceVariant,
                        modifier = Modifier.padding(start = 4.dp),
                    )
                }
            }
        }
    }
}
