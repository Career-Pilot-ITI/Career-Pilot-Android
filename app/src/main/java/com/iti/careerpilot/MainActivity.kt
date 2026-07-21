package com.iti.careerpilot

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.iti.careerpilot.core.designsystem.CareerPilotTheme
import com.iti.careerpilot.rootnavigation.RootNavDisplay
import com.iti.careerpilot.rootnavigation.Route
import com.iti.common.network.NetworkMonitor
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

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
            val isLoggedIn by viewModel.isLoggedIn.collectAsStateWithLifecycle()
            val isOnline by networkMonitor.isOnline.collectAsStateWithLifecycle()

            CareerPilotTheme {
                RootNavDisplay(
                    startRoute = Route.Splash,
                    isOnline = isOnline,
                    isLoggedIn = isLoggedIn,
                )
            }
        }
    }
}
