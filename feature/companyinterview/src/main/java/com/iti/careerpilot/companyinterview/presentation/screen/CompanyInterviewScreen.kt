package com.iti.careerpilot.companyinterview.presentation.screen

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.iti.careerpilot.companyinterview.presentation.mvi.CompanyInterviewStep
import com.iti.careerpilot.companyinterview.presentation.mvi.CompanyInterviewUiEffect
import com.iti.careerpilot.companyinterview.presentation.mvi.CompanyInterviewUiIntent
import com.iti.careerpilot.companyinterview.presentation.viewmodel.CompanyInterviewViewModel

@Composable
fun CompanyInterviewScreen(
    token: String,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CompanyInterviewViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(token) {
        if (token.isNotBlank()) {
            viewModel.onIntent(CompanyInterviewUiIntent.LoadMetadata(token))
        }
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is CompanyInterviewUiEffect.NavigateHome -> onNavigateBack()
                is CompanyInterviewUiEffect.ShowToast -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                }
                is CompanyInterviewUiEffect.VibrateHapticCountdown -> {
                    // Haptic feedback triggered at 5s remaining
                }
                is CompanyInterviewUiEffect.RequestCameraAndMicPermissions -> {}
            }
        }
    }

    // Intercept back button when inside active interview
    BackHandler(enabled = state.step is CompanyInterviewStep.ActiveInterview) {
        // Prevent accidental back exit during timed test
    }

    CompanyInterviewContent(
        state = state,
        onIntent = viewModel::onIntent,
        onFrame = { imageProxy ->
            viewModel.bodyLanguageAnalyzer.processImage(imageProxy)
        },
        modifier = modifier
    )
}
