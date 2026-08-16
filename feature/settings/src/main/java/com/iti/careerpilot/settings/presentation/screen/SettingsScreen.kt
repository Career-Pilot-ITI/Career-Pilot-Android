package com.iti.careerpilot.settings.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.iti.careerpilot.core.designsystem.components.BackIconButton
import com.iti.careerpilot.settings.R
import com.iti.careerpilot.settings.presentation.action.SettingsIntent
import com.iti.careerpilot.settings.presentation.componnents.AccountPlanSettingsCard
import com.iti.careerpilot.settings.presentation.componnents.LanguageDialog
import com.iti.careerpilot.settings.presentation.componnents.OpenDialogSettingsCard
import com.iti.careerpilot.settings.presentation.componnents.ThemeDialog
import com.iti.careerpilot.settings.presentation.event.SettingsEffect
import com.iti.careerpilot.settings.presentation.state.SettingsState
import com.iti.careerpilot.settings.presentation.viewmodel.LocalSettingsUser
import com.iti.careerpilot.settings.presentation.viewmodel.SettingsViewModel
import com.iti.careerpilot.settings.presentation.viewmodel.getTitleId

@Composable
fun SettingsRoot(
    onBack: () -> Unit,
    openPaywall: (showGetCoins: Boolean, showMySubscription: Boolean) -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel.events) {
        viewModel.events.collect { event ->
            when (event) {
                is SettingsEffect.NavigateToPaywall -> {
                    openPaywall(event.showGetCoins, event.showMySubscription)
                }
            }
        }
    }

    SettingsScreen(
        onBack = onBack,
        state = state,
        onIntent = viewModel::onIntent
    )
}

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    state: SettingsState,
    onIntent: (SettingsIntent) -> Unit,
) {
    val userSettings = LocalSettingsUser.current
    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    BackIconButton(
                        onBack = onBack
                    )
                },
                title = {
                    Text(
                        text = stringResource(R.string.settings),
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.Companion
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    navigationIconContentColor = MaterialTheme.colorScheme.primary,
                ),
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(top = innerPadding.calculateTopPadding())
                .fillMaxSize(),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                AccountPlanSettingsCard(
                    planDisplayName = state.planDisplayName,
                    isMaxPlan = state.isMaxPlan,
                    coinBalance = state.coinBalance,
                    onManageSubscriptionClick = { onIntent(SettingsIntent.ManageSubscriptionClicked) },
                    onCoinsClick = { onIntent(SettingsIntent.CoinsClicked) },
                )
            }
            item {
                OpenDialogSettingsCard(
                    onAction = { onIntent(SettingsIntent.LanguageDialogToggle(true)) },
                    iconId = R.drawable.ic_language,
                    titleId = R.string.language,
                    valueId = userSettings.language.getTitleId(),
                )
            }
            item {
                OpenDialogSettingsCard(
                    onAction = { onIntent(SettingsIntent.ThemeDialogToggle(true)) },
                    iconId = R.drawable.ic_theme,
                    titleId = R.string.theme,
                    valueId = userSettings.theme.getTitleId(),
                )
            }
        }
    }

    if (state.showLanguageDialog) {
        LanguageDialog(
            onDismissRequest = { onIntent(SettingsIntent.LanguageDialogToggle(false)) },
            initial = userSettings.language
        )
    }
    if (state.showThemeDialog) {
        ThemeDialog(
            onDismissRequest = { onIntent(SettingsIntent.ThemeDialogToggle(false)) },
            setTheme = { onIntent(SettingsIntent.UpdateTheme(it)) },
            selectedTheme = userSettings.theme
        )
    }
}
