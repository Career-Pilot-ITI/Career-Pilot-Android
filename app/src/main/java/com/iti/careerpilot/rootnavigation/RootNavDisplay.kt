package com.iti.careerpilot.rootnavigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
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
import com.iti.careerpilot.features.paywall.PaywallRoot
import com.iti.careerpilot.features.settings.SettingsRoot
import com.iti.careerpilot.features.splash.SplashRoot
import com.iti.careerpilot.login.presentation.login.screen.LoginRoot
import com.iti.careerpilot.login.presentation.otp.screen.OTPRoot
import com.iti.careerpilot.nestednavigation.NestedNavDisplay
import com.iti.careerpilot.practicesession.presentation.practicescreen.screen.PracticeSessionRoot
import com.iti.careerpilot.practicesession.presentation.resultscreen.ResultRoot
import com.iti.common.snackbar.CareerPilotSnackbarController
import com.iti.common.snackbar.model.CareerPilotSnackbarType
import kotlinx.coroutines.CancellationException


@Composable
fun RootNavDisplay(
    startRoute: Route,
    isOnline: Boolean,
    isLoggedIn: Boolean?,
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

    val hasBottomNavigationBar = currentRootRoute == Route.NestedNav

    LaunchedEffect(snackbarHostState) {
        CareerPilotSnackbarController.requests.collect { request ->
            try {
                val event = request.event
                val materialResult = snackbarHostState.showSnackbar(
                    message = event.message.asString(context),
                    withDismissAction =
                        event.type ==
                                CareerPilotSnackbarType.DISMISSIBLE,
                    duration = event.duration,
                    actionLabel =
                        event.actionLabel?.asString(context),
                )

                request.complete(materialResult)
            } catch (cancellation: CancellationException) {
                request.complete(
                    SnackbarResult.Dismissed,
                )

                throw cancellation
            }
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
                slideIntoContainer(
                    towards =
                        AnimatedContentTransitionScope
                            .SlideDirection.Left,
                    animationSpec = tween(350),
                ) togetherWith slideOutOfContainer(
                    towards =
                        AnimatedContentTransitionScope
                            .SlideDirection.Left,
                    animationSpec = tween(350),
                )
            },
            popTransitionSpec = {
                slideIntoContainer(
                    towards =
                        AnimatedContentTransitionScope
                            .SlideDirection.Right,
                    animationSpec = tween(350),
                ) togetherWith slideOutOfContainer(
                    towards =
                        AnimatedContentTransitionScope
                            .SlideDirection.Right,
                    animationSpec = tween(350),
                )
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
                        logout = {
                            rootBackStack.apply {
                                clear()
                                navigateSingleTop(
                                    Route.Login,
                                )
                            }
                        },
                        openSessionDetails = { trackId, sessionId ->
                            rootBackStack.navigateSingleTop(
                                Route.SessionDetails(
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
                        openPaywall = {
                            rootBackStack.navigateSingleTop(
                                Route.Paywall,
                            )
                        },
                    )
                }
                entry<Route.Onboarding> {
                    com.iti.onboarding.navigation.OnboardingPagerScreen(
                        onOnboardingFinished = {
                            rootBackStack.replaceAll(Route.NestedNav)
                        }
                    )
                }
                entry<Route.SessionDetails> {
                    PracticeSessionRoot(
                        trackId = it.trackId,
                        sessionId = it.sessionId,
                        onNavigateToResult = { sessionId ->
                            rootBackStack.popIfCurrentIs<Route.SessionDetails>()
                            rootBackStack.navigateSingleTop(Route.PracticeResult(sessionId))
                        },
                        onBack = {
                            rootBackStack.popIfCurrentIs<Route.SessionDetails>()
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
                    PaywallRoot()
                }
            },
        )
    }
}
