package com.iti.onboarding.presentation.screen.cv.view

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.iti.onboarding.R
import com.iti.onboarding.presentation.screen.cv.state.CvUploadStage
import com.iti.onboarding.presentation.screen.cv.state.UploadCvEffect
import com.iti.onboarding.presentation.screen.cv.state.UploadCvIntent
import com.iti.onboarding.presentation.screen.cv.state.UploadCvUiState
import com.iti.onboarding.presentation.screen.cv.view.components.UploadCvCard
import com.iti.onboarding.presentation.screen.cv.view.components.UploadCvHeader
import com.iti.onboarding.presentation.screen.cv.viewmodel.UploadCvViewModel
import com.iti.onboarding.presentation.screen.track.view.components.ActionButton

private const val PDF_MIME_TYPE = "application/pdf"

@Composable
fun UploadCvScreen(
    onNavigateToNext: () -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: UploadCvViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current

    val pdfPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri ->
        uri ?: return@rememberLauncherForActivityResult

        runCatching {
            context.contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION,
            )
        }

        viewModel.onIntent(
            UploadCvIntent.OnPdfSelected(uri.toString())
        )
    }

    LaunchedEffect(viewModel, lifecycleOwner) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.effects.collect { effect ->
                when (effect) {
                    UploadCvEffect.OpenPdfPicker -> {
                        pdfPicker.launch(arrayOf(PDF_MIME_TYPE))
                    }

                    UploadCvEffect.NavigateNext -> onNavigateToNext()
                    UploadCvEffect.Skip -> onSkip()
                    is UploadCvEffect.ShowError -> {
                        // TODO: Show the localized message with the base snackbar.
                    }
                }
            }
        }
    }

    UploadCvScreenContent(
        state = state,
        onIntent = viewModel::onIntent,
        modifier = modifier.safeContentPadding(),
    )
}

@Composable
fun UploadCvScreenContent(
    state: UploadCvUiState,
    onIntent: (UploadCvIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        UploadCvHeader()

        Column {
            UploadCvCard(
                selectedFile = state.selectedFile,
                stage = state.stage,
                uploadProgress = state.uploadProgress,
                onClick = {
                    onIntent(UploadCvIntent.OnUploadAreaClick)
                },
            )

            AnimatedVisibility(
                visible = state.stage == CvUploadStage.UPLOADED,
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

            TextButton(
                onClick = {
                    onIntent(UploadCvIntent.OnSkipClick)
                },
                enabled = !state.isBusy,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(top = 12.dp),
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

        ActionButton(
            label = stringResource(R.string.analyze_my_cv),
            enabled = state.canAnalyze,
            onClick = {
                onIntent(UploadCvIntent.OnAnalyzeClick)
            },
        )
    }
}
