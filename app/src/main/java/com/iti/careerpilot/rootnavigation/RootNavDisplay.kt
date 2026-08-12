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
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.iti.careerpilot.ats.presentation.coverletter.view.CoverLetterRoot
import com.iti.careerpilot.ats.presentation.entry.view.AtsEntryRoot
import com.iti.careerpilot.ats.presentation.optimizedcv.view.OptimizedCvRoot
import com.iti.careerpilot.ats.presentation.scoring.view.ScoringRoot
import com.iti.careerpilot.core.designsystem.components.CareerPilotAppScaffold
import com.iti.careerpilot.editprofile.presentation.screen.EditProfileRoot
import com.iti.careerpilot.features.paywall.navigation.PaymentNavDisplay
import com.iti.careerpilot.features.paywall.navigation.PaymentRoute
import com.iti.careerpilot.features.settings.SettingsRoot
import com.iti.careerpilot.features.splash.SplashRoot
import com.iti.careerpilot.home.presentation.home.screen.HomeRoot
import com.iti.careerpilot.home.presentation.interviews.screen.InterviewsRoot
import com.iti.careerpilot.home.presentation.ready.screen.ReadyToPracticeRoot
import com.iti.careerpilot.login.presentation.login.screen.LoginRoot
import com.iti.careerpilot.login.presentation.otp.screen.OTPRoot
import com.iti.careerpilot.practicesession.presentation.practicescreen.screen.PracticeSessionRoot
import com.iti.careerpilot.practicesession.presentation.resultscreen.ResultRoot
import com.iti.careerpilot.profile.presentation.screen.ProfileRoot
import com.iti.careerpilot.reports.presentation.screen.breakdown.view.QuestionBreakdownRoot
import com.iti.careerpilot.reports.presentation.screen.details.view.ReportDetailsRoot
import com.iti.careerpilot.reports.presentation.screen.history.view.SessionHistoryRoot
import com.iti.careerpilot.rootnavigation.components.BottomBarDestination
import com.iti.careerpilot.rootnavigation.components.BottomNavBarItem
import com.iti.careerpilot.rootnavigation.components.CareerPilotBottomNavBar
import com.iti.common.snackbar.CareerPilotSnackbarController
import com.iti.common.snackbar.model.CareerPilotSnackbarType
import com.iti.onboarding.navigation.OnboardingPagerScreen
import kotlinx.coroutines.CancellationException
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
    val currentRoute = rootBackStack.lastOrNull()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val hasBottomNavigationBar = currentRoute.isBottomDestination()

    LaunchedEffect(isLoggedIn) {
        if (
            isLoggedIn == false &&
            rootBackStack.lastOrNull() != Route.Login &&
            rootBackStack.lastOrNull() != Route.Splash
        ) {
            rootBackStack.replaceAll(Route.Login)
        }
    }

    LaunchedEffect(pendingSharedText, isLoggedIn, currentRoute) {
        val isAuthOrSetupRoute = currentRoute == Route.Splash ||
            currentRoute == Route.Login ||
            currentRoute is Route.OTP ||
            currentRoute == Route.Onboarding
        if (
            pendingSharedText != null &&
            isLoggedIn == true &&
            currentRoute != Route.Ats &&
            !isAuthOrSetupRoute
        ) {
            rootBackStack.navigateToBottomDestination(Route.Ats)
        }
    }

    LaunchedEffect(snackbarHostState) {
        CareerPilotSnackbarController.requests.collectLatest { request ->
            snackbarHostState.currentSnackbarData?.dismiss()
            val event = request.event
            try {
                val result = snackbarHostState.showSnackbar(
                    message = event.message.asString(context),
                    withDismissAction = event.type == CareerPilotSnackbarType.DISMISSIBLE,
                    duration = event.duration,
                    actionLabel = event.actionLabel?.asString(context),
                )
                request.complete(result)
            } catch (cancellation: CancellationException) {
                request.complete(androidx.compose.material3.SnackbarResult.Dismissed)
                throw cancellation
            }
        }
    }

    CareerPilotAppScaffold(
        modifier = Modifier.fillMaxSize(),
        isOnline = isOnline,
        snackbarHostState = snackbarHostState,
        hasBottomNavigationBar = hasBottomNavigationBar,
        bottomBar = {
            if (hasBottomNavigationBar) {
                CareerPilotBottomNavBar(
                    selectedIndex = BottomBarDestination.entries.indexOfFirst {
                        it.route == currentRoute
                    }.coerceAtLeast(0),
                ) {
                    BottomBarDestination.entries.forEach { destination ->
                        BottomNavBarItem(
                            onClick = {
                                rootBackStack.navigateToBottomDestination(destination.route)
                            },
                            isSelected = destination.route == currentRoute,
                            icon = destination.icon,
                            label = stringResource(destination.title),
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                }
            }
        },
    ) { innerPadding ->
        NavDisplay(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            backStack = rootBackStack,
            onBack = { rootBackStack.removeLastOrNull() },
            entryDecorators = listOf(
                rememberSaveableStateHolderNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator(),
            ),
            transitionSpec = {
                (slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(400, easing = FastOutSlowInEasing),
                ) + fadeIn(
                    animationSpec = tween(400, easing = FastOutSlowInEasing),
                )) togetherWith (slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(400, easing = FastOutSlowInEasing),
                ) + fadeOut(
                    animationSpec = tween(400, easing = FastOutSlowInEasing),
                ))
            },
            popTransitionSpec = {
                (slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(400, easing = FastOutSlowInEasing),
                ) + fadeIn(
                    animationSpec = tween(400, easing = FastOutSlowInEasing),
                )) togetherWith (slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(400, easing = FastOutSlowInEasing),
                ) + fadeOut(
                    animationSpec = tween(400, easing = FastOutSlowInEasing),
                ))
            },
            entryProvider = entryProvider {
                entry<Route.Splash> {
                    SplashRoot(
                        onNavigateToLogin = { rootBackStack.replaceAll(Route.Login) },
                        onNavigateToHome = { rootBackStack.replaceAll(Route.Home) },
                        onNavigateToOnboarding = { rootBackStack.replaceAll(Route.Onboarding) },
                    )
                }
                entry<Route.Login> {
                    LoginRoot(
                        openOTP = { phoneNumber ->
                            rootBackStack.navigateSingleTop(Route.OTP(phoneNumber))
                        },
                    )
                }
                entry<Route.OTP> { route ->
                    OTPRoot(
                        phoneNumber = route.phoneNumber,
                        openHome = { rootBackStack.replaceAll(Route.Home) },
                        openOnboarding = { rootBackStack.replaceAll(Route.Onboarding) },
                        onBack = { rootBackStack.popIfCurrentIs<Route.OTP>() },
                    )
                }
                entry<Route.Home> {
                    HomeRoot(
                        openReadyToPractice = { trackId, trackName ->
                            rootBackStack.navigateSingleTop(
                                Route.ReadyToPractice(trackId = trackId, trackName = trackName),
                            )
                        },
                        openSessionDetails = { sessionId ->
                            rootBackStack.navigateSingleTop(Route.SessionDetails(sessionId))
                        },
                        openPracticeSession = { trackId, sessionId ->
                            rootBackStack.navigateSingleTop(
                                Route.PracticeSession(trackId = trackId, sessionId = sessionId),
                            )
                        },
                        openInterviews = {
                            rootBackStack.navigateSingleTop(Route.Interviews)
                        },
                        openPlansPaywall = {
                            rootBackStack.navigateSingleTop(Route.Paywall(showGetCoins = false))
                        },
                        openCoinsPaywall = {
                            rootBackStack.navigateSingleTop(Route.Paywall(showGetCoins = true))
                        },
                        openReports = {
                            rootBackStack.navigateToBottomDestination(Route.SessionHistory)
                        },
                    )
                }
                entry<Route.SessionHistory> {
                    SessionHistoryRoot(
                        openSessionDetails = { sessionId ->
                            rootBackStack.navigateSingleTop(Route.SessionDetails(sessionId))
                        },
                    )
                }
                entry<Route.Ats> {
                    AtsEntryRoot(
                        initialSharedText = pendingSharedText,
                        onSharedTextConsumed = onSharedTextConsumed,
                        onScoreRequested = { workspaceId ->
                            rootBackStack.navigateSingleTop(Route.AtsScore(workspaceId))
                        },
                    )
                }
                entry<Route.Profile> {
                    ProfileRoot(
                        openSettings = {
                            rootBackStack.navigateSingleTop(Route.Settings)
                        },
                        openEditProfile = { section ->
                            rootBackStack.navigateSingleTop(Route.EditProfile(section))
                        },
                        logout = {
                            rootBackStack.replaceAll(Route.Login)
                        },
                    )
                }
                entry<Route.Onboarding> {
                    OnboardingPagerScreen(
                        onOnboardingFinished = { rootBackStack.replaceAll(Route.Home) },
                    )
                }
                entry<Route.AtsScore> { route ->
                    ScoringRoot(
                        workspaceId = route.workspaceId,
                        onBack = { rootBackStack.popIfCurrentIs<Route.AtsScore>() },
                        openCoinsPaywall = {
                            rootBackStack.navigateSingleTop(Route.Paywall(showGetCoins = true))
                        },
                        openCoverLetter = { workspaceId ->
                            rootBackStack.navigateSingleTop(Route.AtsCoverLetter(workspaceId))
                        },
                        openOptimizedCv = { workspaceId ->
                            rootBackStack.navigateSingleTop(Route.AtsOptimizedCv(workspaceId))
                        },
                        openPracticeSession = { trackId ->
                            rootBackStack.navigateSingleTop(Route.PracticeSession(trackId))
                        },
                    )
                }
                entry<Route.AtsCoverLetter> { route ->
                    CoverLetterRoot(
                        workspaceId = route.workspaceId,
                        onBack = { rootBackStack.popIfCurrentIs<Route.AtsCoverLetter>() },
                        openCoinsPaywall = {
                            rootBackStack.navigateSingleTop(Route.Paywall(showGetCoins = true))
                        },
                    )
                }
                entry<Route.AtsOptimizedCv> { route ->
                    OptimizedCvRoot(
                        workspaceId = route.workspaceId,
                        onBack = { rootBackStack.popIfCurrentIs<Route.AtsOptimizedCv>() },
                        openCoinsPaywall = {
                            rootBackStack.navigateSingleTop(Route.Paywall(showGetCoins = true))
                        },
                    )
                }
                entry<Route.PracticeSession> { route ->
                    PracticeSessionRoot(
                        trackId = route.trackId,
                        sessionId = route.sessionId,
                        onNavigateToResult = { sessionId ->
                            rootBackStack.popIfCurrentIs<Route.PracticeSession>()
                            rootBackStack.navigateSingleTop(Route.PracticeResult(sessionId))
                        },
                        onBack = {
                            rootBackStack.popIfCurrentIs<Route.PracticeSession>()
                        },
                    )
                }
                entry<Route.PracticeResult> { route ->
                    ResultRoot(
                        sessionId = route.sessionId,
                        onBack = { rootBackStack.popIfCurrentIs<Route.PracticeResult>() },
                    )
                }
                entry<Route.SessionDetails> { route ->
                    ReportDetailsRoot(
                        sessionId = route.id,
                        navigateBack = {
                            rootBackStack.popIfCurrentIs<Route.SessionDetails>()
                        },
                        openQuestionBreakdown = { sessionId ->
                            rootBackStack.navigateSingleTop(Route.QuestionBreakdown(sessionId))
                        },
                    )
                }
                entry<Route.QuestionBreakdown> { route ->
                    QuestionBreakdownRoot(
                        sessionId = route.sessionId,
                        navigateBack = {
                            rootBackStack.popIfCurrentIs<Route.QuestionBreakdown>()
                        },
                    )
                }
                entry<Route.Settings> {
                    SettingsRoot()
                }
                entry<Route.EditProfile> { route ->
                    EditProfileRoot(
                        section = route.section,
                        navigateBack = {
                            rootBackStack.popIfCurrentIs<Route.EditProfile>()
                        },
                    )
                }
                entry<Route.Paywall> { route ->
                    PaymentNavDisplay(
                        startRoute = if (route.showGetCoins) {
                            PaymentRoute.GetCoins
                        } else {
                            PaymentRoute.ChoosePlan
                        },
                        onNavigateBack = {
                            rootBackStack.popIfCurrentIs<Route.Paywall>()
                        },
                    )
                }
                entry<Route.Interviews> {
                    InterviewsRoot(
                        openReadyToPractice = { trackId, trackName ->
                            rootBackStack.navigateSingleTop(
                                Route.ReadyToPractice(trackId = trackId, trackName = trackName),
                            )
                        },
                        onBack = { rootBackStack.popIfCurrentIs<Route.Interviews>() },
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
                            rootBackStack.popIfCurrentIs<Route.ReadyToPractice>()
                            rootBackStack.navigateSingleTop(Route.PracticeSession(trackId))
                        },
                    )
                }
            },
        )
    }
}

private fun NavBackStack<NavKey>.navigateToBottomDestination(route: Route) {
    replaceAll(Route.Home)
    if (route != Route.Home) {
        add(route)
    }
}

private fun NavKey?.isBottomDestination(): Boolean = when (this) {
    Route.Home,
    Route.SessionHistory,
    Route.Ats,
    Route.Profile -> true
    else -> false
}
