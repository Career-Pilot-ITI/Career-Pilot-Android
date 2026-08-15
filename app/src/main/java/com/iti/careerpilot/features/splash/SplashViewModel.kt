package com.iti.careerpilot.features.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iti.careerpilot.core.access.domain.usecase.RefreshAccessUseCase
import com.iti.core.datastore.UserTokensRepo
import com.iti.core.datastore.repo.UserProfileRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val userProfileRepo: UserProfileRepo,
    private val userTokensRepo: UserTokensRepo,
    private val refreshAccessUseCase: RefreshAccessUseCase,
) : ViewModel() {

    private val _navigationEvent = Channel<SplashEvent>()
    val navigationEvent = _navigationEvent.receiveAsFlow()


    init {
        decideNextScreen()
    }

    private fun decideNextScreen() {
        viewModelScope.launch {
            val destination = async { determineNextDestination() }
            delay(2.seconds)
            _navigationEvent.send(destination.await())
        }
    }

    private suspend fun determineNextDestination(): SplashEvent {
        val tokens = userTokensRepo.readTokens()
        if (tokens.accessToken.isNullOrBlank()) return SplashEvent.NavigateToLogin

        refreshAccessUseCase()

        val userProfile = userProfileRepo.readUserProfile()
        return if (userProfile.hasCompletedOnboarding) {
            SplashEvent.NavigateToHome
        } else {
            SplashEvent.NavigateToOnboarding
        }
    }
}

