package com.iti.careerpilot

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.iti.careerpilot.core.designsystem.CareerPilotTheme
import com.iti.careerpilot.rootnavigation.RootNavDisplay
import com.iti.careerpilot.rootnavigation.Route
import com.iti.careerpilot.optimization.toPendingCvOptimization
import com.iti.common.network.NetworkMonitor
import com.iti.careerpilot.share.toPendingSharedText
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private val viewModel: MainViewModel by viewModels()
    @Inject
    lateinit var networkMonitor: NetworkMonitor

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        viewModel.acceptSharedText(intent.toPendingSharedText())
        viewModel.acceptCvOptimization(intent.toPendingCvOptimization())
        enableEdgeToEdge()
        setContent {
            val isLoggedIn by viewModel.isLoggedIn.collectAsStateWithLifecycle()
            val isOnline by networkMonitor.isOnline.collectAsStateWithLifecycle()
            val pendingSharedText by viewModel.pendingSharedText.collectAsStateWithLifecycle()
            val pendingCvOptimization by viewModel.pendingCvOptimization.collectAsStateWithLifecycle()

            CareerPilotTheme {
                RootNavDisplay(
                    startRoute = Route.Splash,
                    isOnline = isOnline,
                    isLoggedIn = isLoggedIn,
                    pendingSharedText = pendingSharedText,
                    onSharedTextConsumed = viewModel::consumeSharedText,
                    pendingCvOptimization = pendingCvOptimization,
                    onCvOptimizationConsumed = viewModel::consumeCvOptimization,
                )
            }
        }
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        viewModel.acceptSharedText(intent.toPendingSharedText())
        viewModel.acceptCvOptimization(intent.toPendingCvOptimization())
    }
}
