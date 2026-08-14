package com.iti.careerpilot.ats.presentation.entry.view

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.iti.careerpilot.ats.R
import com.iti.careerpilot.ats.presentation.entry.state.AtsEntryAction
import com.iti.careerpilot.ats.presentation.entry.state.AtsEntryEffect
import com.iti.careerpilot.ats.presentation.entry.state.AtsEntryUiState
import com.iti.careerpilot.ats.presentation.entry.view.components.AtsScreenHeader
import com.iti.careerpilot.ats.presentation.entry.view.components.FasterShareHintCard
import com.iti.careerpilot.ats.presentation.entry.view.components.JobUrlTextField
import com.iti.careerpilot.ats.presentation.entry.viewmodel.AtsEntryViewModel
import com.iti.careerpilot.ats.presentation.util.UiStateProvider
import com.iti.careerpilot.ats.presentation.util.rememberUiStateProvider
import com.iti.careerpilot.ats.presentation.util.rememberUiStateValue
import com.iti.careerpilot.core.designsystem.components.CareerPilotButton
import com.iti.careerpilot.core.designsystem.components.CvUploadCard
import com.iti.careerpilot.core.designsystem.components.CvUploadCardStage
import com.iti.common.snackbar.CareerPilotSnackbarController
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.delay

private const val PDF_MIME_TYPE = "application/pdf"


@Composable
fun AtsEntryRoot(
    initialSharedText: String?,
    onSharedTextConsumed: () -> Unit,
    onJobDetailsRequested: (Long) -> Unit,
    viewModel: AtsEntryViewModel = hiltViewModel(),
) {
    val state = viewModel.state.collectAsStateWithLifecycle()
    val stateProvider = rememberUiStateProvider(state)
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { viewModel.onAction(AtsEntryAction.PdfSelected(it.toString())) }
    }

    LaunchedEffect(viewModel) {
        viewModel.onAction(AtsEntryAction.Initial)
    }

    LaunchedEffect(initialSharedText) {
        initialSharedText?.let {
            viewModel.onAction(AtsEntryAction.SharedTextReceived(it))
            onSharedTextConsumed()
        }
    }

    val lifecycle = LocalLifecycleOwner.current
    LaunchedEffect(lifecycle) {
        lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.effects.collect { effect ->
                when (effect) {
                    AtsEntryEffect.OpenPdfPicker -> launcher.launch(PDF_MIME_TYPE)
                    is AtsEntryEffect.NavigateToJobDetails -> onJobDetailsRequested(effect.workspaceId)
                    is AtsEntryEffect.ShowMessage -> CareerPilotSnackbarController.show(effect.message)
                }
            }
        }
    }

    AtsEntryScreen(
        stateProvider = stateProvider,
        onAction = viewModel::onAction,
        modifier = Modifier.windowInsetsPadding(WindowInsets.systemBars),
    )
}

@Composable
fun AtsEntryScreen(
    stateProvider: UiStateProvider<AtsEntryUiState>,
    onAction: (AtsEntryAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val jobUrlDescription = stringResource(R.string.ats_job_posting_link)
    val importMessages = persistentListOf(
        stringResource(R.string.ats_importing_job),
        stringResource(R.string.ats_importing_details),
        stringResource(R.string.ats_preparing_workspace),
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
            .padding(bottom = 12.dp)
            .imePadding(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(top = 24.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            item {
                AtsScreenHeader()
                Text(
                    text = stringResource(R.string.ats_compare_title),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 10.dp),
                )
                Text(
                    text = stringResource(R.string.ats_compare_subtitle),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 6.dp),
                )
            }

            item { Spacer(Modifier.height(12.dp)) }

            item {
                Text(
                    text = stringResource(R.string.ats_job_posting_link),
                    style = MaterialTheme.typography.labelLarge,
                )

                JobUrlTextField(
                    stateProvider = stateProvider,
                    onAction = onAction,
                    jobUrlDescription = jobUrlDescription,
                )

                Spacer(Modifier.height(4.dp))

                Text(
                    text = stringResource(R.string.ats_supported_job_sites),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 6.dp),
                )
            }

            item {
                Text(
                    text = stringResource(R.string.ats_cv_on_file),
                    style = MaterialTheme.typography.labelLarge,
                )

                Spacer(Modifier.height(8.dp))

                CvUploadSection(
                    stateProvider = stateProvider,
                    onAction = onAction,
                )
            }

            item {
                FasterShareHintCard()
            }
        }

        CompareButton(
            stateProvider = stateProvider,
            importMessages = importMessages,
            onAction = onAction,
        )
    }
}

@Composable
private fun CvUploadSection(
    stateProvider: UiStateProvider<AtsEntryUiState>,
    onAction: (AtsEntryAction) -> Unit,
) {
    val fileName by rememberUiStateValue(stateProvider) { it.cvFileName }
    val fileSizeBytes by rememberUiStateValue(stateProvider) { it.cvSizeBytes }
    val isUploadingCv by rememberUiStateValue(stateProvider) { it.isUploadingCv }
    val hasSynchronizedCv by rememberUiStateValue(stateProvider) { it.hasSynchronizedCv }
    val uploadProgress by rememberUiStateValue(stateProvider) { it.uploadProgress }

    CvUploadCard(
        fileName = fileName.takeIf(String::isNotBlank),
        fileSizeBytes = fileSizeBytes,
        stage = when {
            isUploadingCv -> CvUploadCardStage.UPLOADING
            hasSynchronizedCv -> CvUploadCardStage.UPLOADED
            else -> CvUploadCardStage.EMPTY
        },
        uploadProgress = uploadProgress,
        onClick = { onAction(AtsEntryAction.SelectCvClicked) },
    )
}

@Composable
private fun CompareButton(
    stateProvider: UiStateProvider<AtsEntryUiState>,
    importMessages: ImmutableList<String>,
    onAction: (AtsEntryAction) -> Unit,
) {
    val isImporting by rememberUiStateValue(stateProvider) { it.isImporting }
    val canCompare by rememberUiStateValue(stateProvider) { it.canCompare }
    var importMessageIndex by rememberSaveable { mutableIntStateOf(0) }

    LaunchedEffect(isImporting) {
        if (!isImporting) {
            importMessageIndex = 0
            return@LaunchedEffect
        }
        while (true) {
            delay(1_600)
            importMessageIndex = (importMessageIndex + 1) % importMessages.size
        }
    }

    CareerPilotButton(
        text = if (isImporting) {
            importMessages[importMessageIndex]
        } else {
            stringResource(R.string.ats_compare_now)
        },
        onClick = { onAction(AtsEntryAction.CompareClicked) },
        enabled = canCompare,
    )
}
