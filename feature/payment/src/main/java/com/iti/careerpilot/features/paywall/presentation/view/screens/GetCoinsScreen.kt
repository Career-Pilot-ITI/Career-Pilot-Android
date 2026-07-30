package com.iti.careerpilot.features.paywall.presentation.view.screens

import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.iti.careerpilot.core.designsystem.CareerPilotTheme
import com.iti.careerpilot.core.designsystem.Dimens
import com.iti.careerpilot.core.designsystem.components.ButtonVariant
import com.iti.careerpilot.core.designsystem.components.CareerPilotButton
import com.iti.careerpilot.core.designsystem.components.shimmerLoading
import com.iti.careerpilot.features.paywall.presentation.view.components.CoinPackCard
import com.iti.careerpilot.features.paywall.presentation.view.components.GetCoinsHeader
import com.iti.careerpilot.features.paywall.presentation.viewmodel.PaywallIntent
import com.iti.careerpilot.features.paywall.presentation.viewmodel.PaywallState
import com.iti.careerpilot.features.paywall.presentation.viewmodel.PaywallViewModel
import com.iti.careerpilot.payment.R

@Composable
fun GetCoinsScreen(
    onNavigateBack: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: PaywallViewModel = hiltViewModel()
) {
    LaunchedEffect(Unit) {
        viewModel.onIntent(PaywallIntent.LoadCoinPacks)
    }

    val state by viewModel.state.collectAsStateWithLifecycle()

    GetCoinsContent(
        state = state,
        onIntent = viewModel::onIntent,
        onBack = onNavigateBack,
        modifier = modifier
    )
}

@Composable
fun GetCoinsContent(
    state: PaywallState,
    onIntent: (PaywallIntent) -> Unit,
    onBack: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val selectedPack = state.selectedCoinPack
    val actionLabel = if (selectedPack != null) {
        stringResource(R.string.paywall_buy_coins_action, selectedPack.coins, selectedPack.priceEgp)
    } else {
        stringResource(R.string.paywall_get_coins_title)
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            @OptIn(ExperimentalMaterial3Api::class)
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.paywall_get_coins_title),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.paywall_back),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(start = Dimens.SpaceL, end = Dimens.SpaceL, top = Dimens.SpaceM, bottom = Dimens.SpaceL)
            ) {
                CareerPilotButton(
                    text = actionLabel,
                    onClick = { onIntent(PaywallIntent.BuyCoinsRequested) },
                    enabled = selectedPack != null && !state.isLoadingCoinPacks && !state.isCheckoutInProgress,
                    variant = ButtonVariant.PRIMARY,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(Dimens.OtpCellHeight)
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = Dimens.SpaceL)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(Dimens.SpaceM))

            GetCoinsHeader()

            Spacer(modifier = Modifier.height(Dimens.SpaceXXL))

            if (state.isLoadingCoinPacks && state.coinPacks.isEmpty()) {
                repeat(3) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(Dimens.QuestionMetricHeight)
                            .padding(bottom = Dimens.SpaceM)
                            .shimmerLoading(isLoading = true, shape = RoundedCornerShape(Dimens.SpaceL))
                    )
                }
            } else {
                state.coinPacks.forEach { pack ->
                    key(pack.id) {
                        CoinPackCard(
                            pack = pack,
                            isSelected = pack.id == state.selectedCoinPackId,
                            onSelect = { onIntent(PaywallIntent.SelectCoinPack(pack.id)) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(Dimens.SpaceL))
        }
    }
}

@Preview(name = "Light Mode", showBackground = true)
@Preview(name = "Dark Mode", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun GetCoinsScreenPreview() {
    CareerPilotTheme {
        GetCoinsContent(
            state = PaywallState(selectedCoinPackId = "coins_500"),
            onIntent = {},
        )
    }
}
