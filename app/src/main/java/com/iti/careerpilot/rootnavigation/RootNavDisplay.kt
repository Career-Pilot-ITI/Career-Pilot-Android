package com.iti.careerpilot.rootnavigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.iti.careerpilot.editprofile.presentation.screen.EditProfileRoot
import com.iti.careerpilot.features.login.LoginRoot
import com.iti.careerpilot.features.otp.OTPRoot
import com.iti.careerpilot.features.paywall.PaywallRoot
import com.iti.careerpilot.features.register.RegisterRoot
import com.iti.careerpilot.features.sessiondetails.SessionDetailsRoot
import com.iti.careerpilot.features.settings.SettingsRoot
import com.iti.careerpilot.nestednavigation.NestedNavDisplay

@Composable
fun RootNavDisplay() {

    val rootBackStack = rememberNavBackStack(Route.Login)

    NavDisplay(
        modifier = Modifier.fillMaxSize(),
        backStack = rootBackStack,
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator()
        ),
        transitionSpec = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(350)
            ) togetherWith slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(350)
            )
        },
        popTransitionSpec = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(350)
            ) togetherWith slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(350)
            )
        },
        entryProvider = entryProvider {
            entry<Route.Login> {
                LoginRoot(
                    openOTP = {
                        rootBackStack.apply {
                            navigateSingleTop(Route.OTP)
                        }
                    },
                )
            }
            entry<Route.OTP> {
                OTPRoot(
                    openHome = {
                        rootBackStack.apply {
                            clear()
                            navigateSingleTop(Route.NestedNav)
                        }
                    },
                    openRegister = {
                        rootBackStack.apply {
                            clear()
                            navigateSingleTop(Route.Register)
                        }
                    }
                )
            }
            entry<Route.Register> {
                RegisterRoot(
                    openHome = {
                        rootBackStack.apply {
                            clear()
                            navigateSingleTop(Route.NestedNav)
                        }
                    },
                    openLogin = {
                        rootBackStack.apply {
                            clear()
                            navigateSingleTop(Route.Login)
                        }
                    }
                )
            }
            entry<Route.NestedNav> {
                NestedNavDisplay(
                    currentRootRoute = rootBackStack.lastOrNull(),
                    navigateBack = {
                        rootBackStack.removeLastOrNull()
                    },
                    logout = {
                        rootBackStack.apply {
                            clear()
                            navigateSingleTop(Route.Login)
                        }
                    },
                    openSessionDetails = { id ->
                        rootBackStack.navigateSingleTop(Route.SessionDetails(id = id))
                    },
                    openSettings = {
                        rootBackStack.navigateSingleTop(Route.Settings)
                    },
                    openEditProfile = {
                        rootBackStack.navigateSingleTop(Route.EditProfile)
                    },
                    openPaywall = {
                        rootBackStack.navigateSingleTop(Route.Paywall)
                    },
                )
            }
            entry<Route.SessionDetails> {
                SessionDetailsRoot(
                    sessionId = it.id
                )
            }
            entry<Route.Settings> {
                SettingsRoot()
            }
            entry<Route.EditProfile> {
                EditProfileRoot(
                    navigateBack = {
                        rootBackStack.popIfCurrentIs<Route.EditProfile>()
                    }
                )
            }
            entry<Route.Paywall> {
                PaywallRoot()
            }
        }
    )
}