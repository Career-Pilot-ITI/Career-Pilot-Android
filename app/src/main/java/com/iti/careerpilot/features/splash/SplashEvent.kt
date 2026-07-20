package com.iti.careerpilot.features.splash


sealed interface SplashEvent {
    data object NavigateToHome : SplashEvent
    data object NavigateToLogin : SplashEvent
    data object NavigateToOnboarding : SplashEvent
}