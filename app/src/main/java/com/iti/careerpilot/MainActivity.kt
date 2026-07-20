package com.iti.careerpilot

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.iti.careerpilot.core.designsystem.CareerPilotTheme
import com.iti.careerpilot.core.designsystem.components.CareerPilotSplash
import com.iti.careerpilot.rootnavigation.RootNavDisplay
import com.iti.careerpilot.rootnavigation.Route
import com.iti.common.network.NetworkMonitor
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()
    @Inject
    lateinit var networkMonitor: NetworkMonitor

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val hasCompletedOnboarding by viewModel.hasCompletedOnboarding.collectAsStateWithLifecycle()
            val isLoggedIn by viewModel.isLoggedIn.collectAsStateWithLifecycle()

            val isOnline by networkMonitor.isOnline.collectAsStateWithLifecycle()

            var isTimeOut by rememberSaveable { mutableStateOf(false) }
            LaunchedEffect(Unit) {
                delay(2000L.milliseconds)
                isTimeOut = true
            }

            CareerPilotTheme {
                if (hasCompletedOnboarding != null && isLoggedIn != null && isTimeOut) {
                    val startRoute = when {
                        isLoggedIn == false -> Route.Login
                        hasCompletedOnboarding == false -> Route.Onboarding
                        else -> Route.NestedNav
                    }
                    RootNavDisplay(
                        startRoute = startRoute,
                        isOnline = isOnline,
                        isLoggedIn = isLoggedIn == true,
                        hasCompletedOnboarding = hasCompletedOnboarding == true
                    )
                } else {
                    CareerPilotSplash()
                }
            }
        }
    }
}
