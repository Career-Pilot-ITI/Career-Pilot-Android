package com.iti.careerpilot

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.iti.careerpilot.core.designsystem.CareerPilotTheme
import com.iti.careerpilot.rootnavigation.RootNavDisplay
import com.iti.careerpilot.rootnavigation.Route
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val hasCompletedOnboarding by viewModel.hasCompletedOnboarding.collectAsStateWithLifecycle()

            if (hasCompletedOnboarding != null) {
                val startRoute = if (hasCompletedOnboarding == true) {
                    Route.NestedNav
                } else {
                    Route.Onboarding
                }
                CareerPilotTheme {
                    RootNavDisplay(startRoute = startRoute)
                }
            }
        }
    }
}
