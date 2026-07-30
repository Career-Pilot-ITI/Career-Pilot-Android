package com.iti.careerpilot.features.paywall.presentation.view.screens

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.iti.careerpilot.core.designsystem.CareerPilotTheme
import com.iti.careerpilot.core.designsystem.Dimens
import com.iti.careerpilot.features.paywall.presentation.view.components.FloatingScannerCardAnimation
import com.iti.careerpilot.features.paywall.presentation.view.components.LoadingDots
import com.iti.careerpilot.features.paywall.presentation.view.components.ScreenTitle
import com.iti.careerpilot.features.paywall.presentation.viewmodel.PaywallIntent
import com.iti.careerpilot.features.paywall.presentation.viewmodel.PaywallViewModel
import com.iti.careerpilot.payment.R

@Composable
fun PaymentProcessingScreen(
    modifier: Modifier = Modifier
) {
    val viewModel: PaywallViewModel = hiltViewModel()
    PaymentProcessingContent(
        onIntent = viewModel::onIntent,
        modifier = modifier
    )
}

@Composable
fun PaymentProcessingContent(
    onIntent: (PaywallIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                onIntent(PaywallIntent.PollPaymentStatus)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .systemBarsPadding()
            .padding(Dimens.SpaceL),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.weight(1f))

        FloatingScannerCardAnimation()

        Spacer(modifier = Modifier.height(Dimens.SpaceXXL))
        Spacer(modifier = Modifier.height(Dimens.SpaceL))

        ScreenTitle(
            title = stringResource(R.string.paywall_processing_title),
            modifier = Modifier.padding(bottom = Dimens.SpaceS),
            color = MaterialTheme.colorScheme.onBackground
        )

        Text(
            text = stringResource(R.string.paywall_processing_subtitle),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.weight(1f))

        LoadingDots()

        Spacer(modifier = Modifier.height(Dimens.SpaceXXL))
    }
}

@Preview(name = "Light Mode", showBackground = true)
@Preview(name = "Dark Mode", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PaymentProcessingScreenPreview() {
    CareerPilotTheme {
        PaymentProcessingContent(
            onIntent = {}
        )
    }
}
