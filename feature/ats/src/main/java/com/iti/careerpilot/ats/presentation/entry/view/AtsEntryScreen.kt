package com.iti.careerpilot.ats.presentation.entry.view

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.platform.LocalContext
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
import com.iti.careerpilot.ats.presentation.entry.view.components.CurrentCvCard
import com.iti.careerpilot.ats.presentation.entry.view.components.FasterShareHintCard
import com.iti.careerpilot.ats.presentation.entry.view.components.JobUrlTextField
import com.iti.careerpilot.ats.presentation.entry.view.components.MissingCvCard
import com.iti.careerpilot.ats.presentation.entry.viewmodel.AtsEntryViewModel
import com.iti.careerpilot.core.designsystem.components.CareerPilotButton
import com.iti.common.snackbar.CareerPilotSnackbarController
import kotlinx.coroutines.delay

private const val PDF_MIME_TYPE = "application/pdf"


@Composable
fun AtsEntryRoot(
    initialSharedText: String?,
    onSharedTextConsumed: () -> Unit,
    onWorkspaceImported: (Long) -> Unit,
    viewModel: AtsEntryViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LocalContext.current
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { viewModel.onAction(AtsEntryAction.PdfSelected(it.toString())) }
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
                    is AtsEntryEffect.NavigateToJobDetails -> onWorkspaceImported(effect.workspaceId)
                    is AtsEntryEffect.ShowMessage -> CareerPilotSnackbarController.show(effect.message)
                }
            }
        }
    }

    AtsEntryScreen(
        state = state,
        onAction = viewModel::onAction,
    )
}

@Composable
fun AtsEntryScreen(
    state: AtsEntryUiState,
    onAction: (AtsEntryAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val jobUrlDescription = stringResource(R.string.ats_job_posting_link)
    val importMessages = listOf(
        stringResource(R.string.ats_importing_job),
        stringResource(R.string.ats_importing_details),
        stringResource(R.string.ats_preparing_workspace),
    )
    var importMessageIndex by rememberSaveable { mutableIntStateOf(0) }

    LaunchedEffect(state.isImporting) {
        if (!state.isImporting) {
            importMessageIndex = 0
            return@LaunchedEffect
        }
        while (true) {
            delay(1_600)
            importMessageIndex = (importMessageIndex + 1) % importMessages.size
        }
    }

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

                JobUrlTextField(state, onAction, jobUrlDescription)

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

                if (state.hasSynchronizedCv) {
                    CurrentCvCard(state = state)
                } else {
                    MissingCvCard(
                        enabled = !state.isBusy,
                        onUpload = { onAction(AtsEntryAction.SelectCvClicked) },
                    )
                }
            }

            item {
                FasterShareHintCard()
            }
        }

        CareerPilotButton(
            text = if (state.isImporting) importMessages[importMessageIndex]
            else stringResource(R.string.ats_compare_now),
            onClick = { onAction(AtsEntryAction.CompareClicked) },
            enabled = state.canCompare,
            modifier = Modifier.padding(bottom = 16.dp),
        )
    }
}