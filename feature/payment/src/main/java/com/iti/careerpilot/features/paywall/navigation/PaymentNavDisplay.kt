package com.iti.careerpilot.features.paywall.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.iti.careerpilot.core.designsystem.Dimens
import com.iti.careerpilot.core.designsystem.components.CareerPilotCard
import com.iti.careerpilot.features.paywall.presentation.view.screens.GetCoinsContent
import com.iti.careerpilot.features.paywall.presentation.view.screens.MonthlyLimitContent
import com.iti.careerpilot.features.paywall.presentation.view.screens.MySubscriptionScreen
import com.iti.careerpilot.features.paywall.presentation.view.screens.PaymentFailedContent
import com.iti.careerpilot.features.paywall.presentation.view.screens.PaymentProcessingContent
import com.iti.careerpilot.features.paywall.presentation.view.screens.PaymentSuccessfulContent
import com.iti.careerpilot.features.paywall.presentation.view.screens.SubscriptionPlansContent
import com.iti.careerpilot.features.paywall.presentation.viewmodel.PaywallEffect
import com.iti.careerpilot.features.paywall.presentation.viewmodel.PaywallIntent
import com.iti.careerpilot.features.paywall.presentation.viewmodel.PaywallViewModel
import com.iti.careerpilot.features.paywall.util.CustomTabManager

@Composable
fun PaymentNavDisplay(
    modifier: Modifier = Modifier,
    startRoute: PaymentRoute = PaymentRoute.ChoosePlan,
    onNavigateBack: () -> Unit = {}
) {
    val paymentBackStack = rememberNavBackStack(startRoute)
    val viewModel: PaywallViewModel = hiltViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val context = LocalContext.current
    val primaryColor = MaterialTheme.colorScheme.primary.toArgb()

    LaunchedEffect(viewModel.effectFlow, lifecycle) {
        lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.effectFlow.collect { effect ->
                when (effect) {
                    is PaywallEffect.NavigateToWebView -> {
                        CustomTabManager.launch(
                            context = context,
                            url = effect.checkoutUrl,
                            toolbarColor = primaryColor
                        )
                        paymentBackStack.add(PaymentRoute.PaymentProcessing)
                    }
                    PaywallEffect.NavigateToChoosePlan ->
                        paymentBackStack.add(PaymentRoute.ChoosePlan)
                    PaywallEffect.NavigateToGetCoins ->
                        paymentBackStack.add(PaymentRoute.GetCoins)
                    PaywallEffect.NavigateToPaymentProcessing ->
                        paymentBackStack.add(PaymentRoute.PaymentProcessing)
                    PaywallEffect.NavigateToPaymentSuccessful -> {
                        paymentBackStack.apply {
                            clear()
                            add(PaymentRoute.PaymentSuccessful)
                        }
                    }
                    is PaywallEffect.NavigateToPaymentFailed -> {
                        paymentBackStack.apply {
                            clear()
                            add(PaymentRoute.PaymentFailed)
                        }
                    }
                    PaywallEffect.NavigateBack,
                    PaywallEffect.NavigateToHome ->
                        onNavigateBack()
                    PaywallEffect.NavigateToCheckout -> {
                        paymentBackStack.apply {
                            clear()
                            add(PaymentRoute.ChoosePlan)
                        }
                    }
                    is PaywallEffect.ShowSnackbar -> {
                        com.iti.common.snackbar.CareerPilotSnackbarController.show(effect.message)
                    }
                }
            }
        }
    }

    val safePopBackStack: () -> Unit = {
        if (paymentBackStack.size > 1) {
            paymentBackStack.removeLastOrNull()
        } else {
            onNavigateBack()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        NavDisplay(
            backStack = paymentBackStack,
            modifier = Modifier.fillMaxSize(),
            onBack = safePopBackStack,
            entryDecorators = listOf(
                rememberSaveableStateHolderNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator(),
            ),
            transitionSpec = {
                (slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(400, easing = FastOutSlowInEasing),
                ) + fadeIn(animationSpec = tween(400, easing = FastOutSlowInEasing))) togetherWith (slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(400, easing = FastOutSlowInEasing),
                ) + fadeOut(animationSpec = tween(400, easing = FastOutSlowInEasing)))
            },
            popTransitionSpec = {
                (slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(400, easing = FastOutSlowInEasing),
                ) + fadeIn(animationSpec = tween(400, easing = FastOutSlowInEasing))) togetherWith (slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(400, easing = FastOutSlowInEasing),
                ) + fadeOut(animationSpec = tween(400, easing = FastOutSlowInEasing)))
            },
            entryProvider = entryProvider {
                entry<PaymentRoute.ChoosePlan> {
                    LaunchedEffect(Unit) {
                        viewModel.onIntent(PaywallIntent.LoadSubscriptionPlans)
                    }
                    SubscriptionPlansContent(
                        state = state,
                        onIntent = viewModel::onIntent,
                        onNavigateToMonthlyLimit = { paymentBackStack.add(PaymentRoute.MonthlyLimit) },
                        onBack = safePopBackStack,
                    )
                    if (state.isCheckoutInProgress) {
                        LoadingOverlay()
                    }
                }
                entry<PaymentRoute.GetCoins> {
                    LaunchedEffect(Unit) {
                        viewModel.onIntent(PaywallIntent.LoadCoinPacks)
                    }
                    GetCoinsContent(
                        state = state,
                        onIntent = viewModel::onIntent,
                        onBack = safePopBackStack,
                    )
                    if (state.isCheckoutInProgress) {
                        LoadingOverlay()
                    }
                }
                entry<PaymentRoute.MonthlyLimit> {
                    MonthlyLimitContent(
                        state = state,
                        onIntent = viewModel::onIntent,
                    )
                }
                entry<PaymentRoute.PaymentProcessing> {
                    PaymentProcessingContent(
                        onIntent = viewModel::onIntent
                    )
                }
                entry<PaymentRoute.PaymentSuccessful> {
                    PaymentSuccessfulContent(
                        state = state,
                        onIntent = viewModel::onIntent
                    )
                }
                entry<PaymentRoute.PaymentFailed> {
                    PaymentFailedContent(
                        state = state,
                        onIntent = viewModel::onIntent
                    )
                }
                entry<PaymentRoute.MySubscription> {
                    MySubscriptionScreen(
                        onNavigateToChoosePlan = { paymentBackStack.add(PaymentRoute.ChoosePlan) },
                        onNavigateToGetCoins = { paymentBackStack.add(PaymentRoute.GetCoins) },
                        onNavigateBack = safePopBackStack,
                    )
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun LoadingOverlay() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.4f)),
        contentAlignment = Alignment.Center
    ) {
        CareerPilotCard(useShadow = false) {
            Box(modifier = Modifier.padding(Dimens.SpaceL)) {
                CircularWavyProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}
