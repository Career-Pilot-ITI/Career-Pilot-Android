package com.iti.careerpilot.features.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iti.core.datastore.UserTokensRepo
import com.iti.core.datastore.repo.UserProfileRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val userProfileRepo: UserProfileRepo,
    private val userTokensRepo: UserTokensRepo
) : ViewModel() {

    private val _navigationEvent = Channel<SplashEvent>()
    val navigationEvent = _navigationEvent.receiveAsFlow()

    init {
        decideNextScreen()
    }

    private fun decideNextScreen() {
        viewModelScope.launch {
            delay(2.seconds)
            val tokens = userTokensRepo.tokens.first()
            val isLoggedIn = !tokens.accessToken.isNullOrBlank()

            if (!isLoggedIn) {
                _navigationEvent.send(SplashEvent.NavigateToLogin)
                return@launch
            }

            val userProfile = userProfileRepo.userProfile.first()
            val hasCompletedOnboarding = userProfile.personal.displayName.isNotBlank()

            if (hasCompletedOnboarding) {
                _navigationEvent.send(SplashEvent.NavigateToHome)
            } else {
                _navigationEvent.send(SplashEvent.NavigateToOnboarding)
            }
        }
    }
}

