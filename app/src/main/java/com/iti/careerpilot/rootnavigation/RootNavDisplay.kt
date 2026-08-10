package com.iti.careerpilot.rootnavigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.iti.careerpilot.core.designsystem.components.CareerPilotAppScaffold
import com.iti.careerpilot.editprofile.presentation.screen.EditProfileRoot
import com.iti.careerpilot.features.paywall.navigation.PaymentNavDisplay
import com.iti.careerpilot.features.paywall.navigation.PaymentRoute
import com.iti.careerpilot.features.settings.SettingsRoot
import com.iti.careerpilot.features.splash.SplashRoot
import com.iti.careerpilot.home.presentation.interviews.screen.InterviewsRoot
import com.iti.careerpilot.home.presentation.ready.screen.ReadyToPracticeRoot
import com.iti.careerpilot.login.presentation.login.screen.LoginRoot
import com.iti.careerpilot.login.presentation.otp.screen.OTPRoot
import com.iti.careerpilot.nestednavigation.NestedNavDisplay
import com.iti.careerpilot.practicesession.presentation.practicescreen.screen.PracticeSessionRoot
import com.iti.careerpilot.practicesession.presentation.resultscreen.ResultRoot
import com.iti.careerpilot.reports.presentation.screen.breakdown.view.QuestionBreakdownRoot
import com.iti.careerpilot.reports.presentation.screen.details.view.ReportDetailsRoot
import com.iti.common.snackbar.CareerPilotSnackbarController
import com.iti.common.snackbar.model.CareerPilotSnackbarType
import com.iti.onboarding.navigation.OnboardingPagerScreen
import kotlinx.coroutines.flow.collectLatest


@Composable
fun RootNavDisplay(
    startRoute: Route,
    isOnline: Boolean,
    isLoggedIn: Boolean?,
    pendingSharedText: String?,
    onSharedTextConsumed: () -> Unit,
) {

    val rootBackStack = rememberNavBackStack(startRoute)

    LaunchedEffect(isLoggedIn) {
        if (isLoggedIn == false && rootBackStack.lastOrNull() != Route.Login && rootBackStack.lastOrNull() != Route.Splash) {
            rootBackStack.apply {
                clear()
                add(Route.Login)
            }
        }
    }
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val currentRootRoute = rootBackStack.lastOrNull()

    LaunchedEffect(pendingSharedText, isLoggedIn, currentRootRoute) {
        val authOrSetupRoute = currentRootRoute == Route.Splash ||
            currentRootRoute == Route.Login ||
            currentRootRoute is Route.OTP ||
            currentRootRoute == Route.Onboarding
        if (
            pendingSharedText != null &&
            isLoggedIn == true &&
            currentRootRoute != Route.NestedNav &&
            !authOrSetupRoute
        ) {
            rootBackStack.replaceAll(Route.NestedNav)
        }
    }

    val hasBottomNavigationBar = currentRootRoute == Route.NestedNav

    LaunchedEffect(snackbarHostState) {
        CareerPilotSnackbarController.requests.collectLatest { request ->
            snackbarHostState.currentSnackbarData?.dismiss()

            val event = request.event
            val result = snackbarHostState.showSnackbar(
                message = event.message.asString(context),
                withDismissAction =
                    event.type ==
                            CareerPilotSnackbarType.DISMISSIBLE,
                duration = event.duration,
                actionLabel =
                    event.actionLabel?.asString(context),
            )

            request.complete(result)
        }
    }

    CareerPilotAppScaffold(
        modifier = Modifier.fillMaxSize(),
        isOnline = isOnline,
        snackbarHostState = snackbarHostState,
        hasBottomNavigationBar = hasBottomNavigationBar,
    ) { innerPadding ->

        NavDisplay(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            backStack = rootBackStack,
            entryDecorators = listOf(
                rememberSaveableStateHolderNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator(),
            ),
            transitionSpec = {
                (slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(
                        400,
                        easing = FastOutSlowInEasing
                    ),
                ) + fadeIn(
                    animationSpec = tween(
                        400,
                        easing = FastOutSlowInEasing
                    )
                )) togetherWith (slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(
                        400,
                        easing = FastOutSlowInEasing
                    ),
                ) + fadeOut(
                    animationSpec = tween(
                        400,
                        easing = FastOutSlowInEasing
                    )
                ))
            },
            popTransitionSpec = {
                (slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(
                        400,
                        easing = FastOutSlowInEasing
                    ),
                ) + fadeIn(
                    animationSpec = tween(
                        400,
                        easing = FastOutSlowInEasing
                    )
                )) togetherWith (slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(
                        400,
                        easing = FastOutSlowInEasing
                    ),
                ) + fadeOut(
                    animationSpec = tween(
                        400,
                        easing = FastOutSlowInEasing
                    )
                ))
            },
            entryProvider = entryProvider {
                entry<Route.Splash> {
                    SplashRoot(
                        onNavigateToLogin = {
                            rootBackStack.replaceAll(Route.Login)
                        },
                        onNavigateToHome = {
                            rootBackStack.replaceAll(Route.NestedNav)
                        },
                        onNavigateToOnboarding = {
                            rootBackStack.replaceAll(Route.Onboarding)
                        }
                    )
                }
                entry<Route.Login> {
                    LoginRoot(
                        openOTP = { phoneNumber ->
                            rootBackStack.navigateSingleTop(Route.OTP(phoneNumber = phoneNumber))
                        },
                    )
                }

                entry<Route.OTP> {
                    OTPRoot(
                        phoneNumber = it.phoneNumber,
                        openHome = {
                            rootBackStack.replaceAll(Route.NestedNav)
                        },
                        openOnboarding = {
                            rootBackStack.replaceAll(Route.Onboarding)
                        },
                        onBack = {
                            rootBackStack.popIfCurrentIs<Route.OTP>()
                        },
                    )
                }

                entry<Route.NestedNav> {
                    NestedNavDisplay(
                        currentRootRoute = currentRootRoute,
                        navigateBack = {
                            rootBackStack.removeLastOrNull()
                        },
                        openReadyToPractice = { trackId, trackName ->
                            rootBackStack.navigateSingleTop(
                                Route.ReadyToPractice(
                                    trackId = trackId,
                                    trackName = trackName,
                                ),
                            )
                        },
                        openInterviews = {
                            rootBackStack.navigateSingleTop(Route.Interviews)
                        },
                        logout = {
                            rootBackStack.apply {
                                clear()
                                navigateSingleTop(
                                    Route.Login,
                                )
                            }
                        },
                        openSessionDetails = { sessionId ->
                            rootBackStack.navigateSingleTop(
                                Route.SessionDetails(sessionId),
                            )
                        },
                        openPracticeSession = { trackId, sessionId ->
                            rootBackStack.navigateSingleTop(
                                Route.PracticeSession(
                                    trackId = trackId,
                                    sessionId = sessionId,
                                ),
                            )
                        },
                        openSettings = {
                            rootBackStack.navigateSingleTop(
                                Route.Settings,
                            )
                        },
                        openEditProfile = { section ->
                            rootBackStack.navigateSingleTop(Route.EditProfile(section = section))
                        },
                        openPaywall = { showGetCoins ->
                            rootBackStack.navigateSingleTop(
                                Route.Paywall(showGetCoins = showGetCoins)
                            )
                        },
                        pendingSharedText = pendingSharedText,
                        onSharedTextConsumed = onSharedTextConsumed,
                    )
                }
                entry<Route.Onboarding> {
                    OnboardingPagerScreen(
                        onOnboardingFinished = {
                            rootBackStack.replaceAll(Route.NestedNav)
                        }
                    )
                }

                entry<Route.PracticeSession> {
                    PracticeSessionRoot(
                        trackId = it.trackId,
                        sessionId = it.sessionId,
                        onNavigateToResult = { sessionId ->
                            rootBackStack.popIfCurrentIs<Route.PracticeSession>()
                            rootBackStack.navigateSingleTop(Route.PracticeResult(sessionId))
                        },
                        onBack = {
                            rootBackStack.popIfCurrentIs<Route.PracticeSession>()
                        }
                    )
                }

                entry<Route.PracticeResult> {
                    ResultRoot(
                        sessionId = it.sessionId,
                        onBack = {
                            rootBackStack.popIfCurrentIs<Route.PracticeResult>()
                        }
                    )
                }
                entry<Route.SessionDetails> {
                    ReportDetailsRoot(
                        sessionId = it.id,
                        navigateBack = {
                            rootBackStack.popIfCurrentIs<Route.SessionDetails>()
                        },
                        openQuestionBreakdown = { sessionId ->
                            rootBackStack.navigateSingleTop(
                                Route.QuestionBreakdown(sessionId),
                            )
                        },
                    )
                }

                entry<Route.QuestionBreakdown> {
                    QuestionBreakdownRoot(
                        sessionId = it.sessionId,
                        navigateBack = {
                            rootBackStack.popIfCurrentIs<Route.QuestionBreakdown>()
                        },
                    )
                }

                entry<Route.Settings> {
                    SettingsRoot()
                }

                entry<Route.EditProfile> {
                    EditProfileRoot(
                        section = it.section,
                        navigateBack = {
                            rootBackStack.popIfCurrentIs<Route.EditProfile>()
                        }
                    )
                }
                entry<Route.Paywall> {
                    PaymentNavDisplay(
                        startRoute = if (it.showGetCoins) PaymentRoute.GetCoins else PaymentRoute.ChoosePlan,
                        onNavigateBack = {
                            rootBackStack.popIfCurrentIs<Route.Paywall>()
                        }
                    )
                }

                entry<Route.Interviews> {
                    InterviewsRoot(
                        openReadyToPractice = { trackId, trackName ->
                            rootBackStack.navigateSingleTop(
                                Route.ReadyToPractice(
                                    trackId = trackId,
                                    trackName = trackName,
                                ),
                            )
                        },
                        onBack = {
                            rootBackStack.popIfCurrentIs<Route.Interviews>()
                        },
                    )
                }

                entry<Route.ReadyToPractice> { route ->
                    ReadyToPracticeRoot(
                        trackId = route.trackId,
                        trackName = route.trackName,
                        onBack = {
                            rootBackStack.popIfCurrentIs<Route.ReadyToPractice>()
                        },
                        openPractice = { trackId ->
                            rootBackStack.apply {
                                popIfCurrentIs<Route.ReadyToPractice>()
                                navigateSingleTop(
                                    Route.PracticeSession(
                                        trackId = trackId,
                                        sessionId = null,
                                    ),
                                )
                            }
                        },
                    )
                }
            },
        )
    }
}
