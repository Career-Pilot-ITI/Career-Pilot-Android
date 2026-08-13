package com.iti.careerpilot.ats.presentation.coverletter.view

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.iti.careerpilot.ats.R
import com.iti.careerpilot.ats.presentation.components.AtsCenteredTopBar
import com.iti.careerpilot.ats.presentation.coverletter.state.CoverLetterAction
import com.iti.careerpilot.ats.presentation.coverletter.state.CoverLetterEffect
import com.iti.careerpilot.ats.presentation.coverletter.state.CoverLetterUiState
import com.iti.careerpilot.ats.presentation.coverletter.view.components.CoverLetterContent
import com.iti.careerpilot.ats.presentation.coverletter.viewmodel.CoverLetterViewModel
import com.iti.careerpilot.ats.presentation.util.composeEmail
import com.iti.careerpilot.ats.presentation.util.copyText
import com.iti.common.snackbar.CareerPilotSnackbarController
import com.iti.common.util.UIText

@Composable
fun CoverLetterRoot(
    workspaceId: Long,
    onBack: () -> Unit,
    openCoinsPaywall: () -> Unit,
    viewModel: CoverLetterViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val clipboardLabel = stringResource(R.string.cover_letter)

    LaunchedEffect(workspaceId) {
        viewModel.onAction(CoverLetterAction.Initial(workspaceId))
    }

    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.effects.collect { effect ->
                when (effect) {
                    is CoverLetterEffect.CopyText -> {
                        copyText(context, clipboardLabel, effect.value)
                        CareerPilotSnackbarController.show(
                            UIText.StringResource(R.string.ats_copied),
                        )
                    }

                    is CoverLetterEffect.ComposeEmail -> if (!composeEmail(context, effect.draft)) {
                        CareerPilotSnackbarController.show(
                            UIText.StringResource(R.string.ats_no_email_client),
                        )
                    }

                    CoverLetterEffect.OpenCoinsPaywall -> openCoinsPaywall()
                }
            }
        }
    }

    CoverLetterScreen(
        state = state,
        onAction = viewModel::onAction,
        onBack = onBack,
    )
}

@Composable
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
fun CoverLetterScreen(
    state: CoverLetterUiState,
    onAction: (CoverLetterAction) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        AtsCenteredTopBar(
            title = stringResource(R.string.cover_letter),
            onBack = onBack,
        )
        if (state.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                CircularWavyProgressIndicator()
            }
        } else {
            CoverLetterContent(
                state = state,
                onAction = onAction,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}
