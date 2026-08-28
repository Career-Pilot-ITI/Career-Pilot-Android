package com.iti.careerpilot

import android.graphics.Color
import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.iti.careerpilot.core.designsystem.CareerPilotTheme
import com.iti.careerpilot.optimization.toPendingCvOptimization
import com.iti.careerpilot.rootnavigation.RootNavDisplay
import com.iti.careerpilot.rootnavigation.Route
import com.iti.careerpilot.share.toPendingChallengeDeepLink
import com.iti.careerpilot.share.toPendingCompanyInterviewDeepLink
import com.iti.careerpilot.share.toPendingSharedText
import com.iti.careerpilot.settings.presentation.viewmodel.LocalSettingsUser
import com.iti.common.network.NetworkMonitor
import com.iti.core.datastore.settings.domain.models.LanguageSetting
import com.iti.core.datastore.settings.domain.models.ThemeSetting
import com.iti.core.datastore.settings.domain.models.UserSettings
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private val viewModel: MainViewModel by viewModels()
    @Inject
    lateinit var networkMonitor: NetworkMonitor

    private var mainUiState: MainUiState by mutableStateOf(MainUiState.Loading)

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        viewModel.acceptSharedText(intent.toPendingSharedText())
        viewModel.acceptCvOptimization(intent.toPendingCvOptimization())
        viewModel.acceptChallengeDeepLink(intent.toPendingChallengeDeepLink())
        viewModel.acceptCompanyInterviewDeepLink(intent.toPendingCompanyInterviewDeepLink())
        enableEdgeToEdge()
        splashScreen.setKeepOnScreenCondition {
            mainUiState == MainUiState.Loading
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.mainUiState.collect {
                    mainUiState = it
                }
            }
        }

        lifecycleScope.launch {
            val locales = AppCompatDelegate.getApplicationLocales()
            val langTag = locales.toLanguageTags()
            val languageSetting = when (langTag) {
                "ar" -> LanguageSetting.ARABIC
                else -> LanguageSetting.ENGLISH
            }
            viewModel.saveLanguageSettings(languageSetting)
        }

        setContent {
            val isLoggedIn by viewModel.isLoggedIn.collectAsStateWithLifecycle()
            val isOnline by networkMonitor.isOnline.collectAsStateWithLifecycle()
            val pendingSharedText by viewModel.pendingSharedText.collectAsStateWithLifecycle()
            val pendingCvOptimization by viewModel.pendingCvOptimization.collectAsStateWithLifecycle()
            val pendingChallengeDeepLink by viewModel.pendingChallengeDeepLink.collectAsStateWithLifecycle()
            val pendingCompanyInterviewDeepLink by viewModel.pendingCompanyInterviewDeepLink.collectAsStateWithLifecycle()
            val isDarkTheme = shouldShowDarkTheme(mainUiState)
            LaunchedEffect(isDarkTheme) {
                enableEdgeToEdge(
                    statusBarStyle = if (isDarkTheme) SystemBarStyle.dark(Color.TRANSPARENT)
                    else SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
                )
            }

            CareerPilotTheme(
                darkTheme = isDarkTheme
            ) {
                CompositionLocalProvider(
                    LocalSettingsUser provides ((mainUiState as? MainUiState.Ready)?.userSettings
                        ?: UserSettings())
                ) {
                    RootNavDisplay(
                        startRoute = Route.Splash,
                        isOnline = isOnline,
                        isLoggedIn = isLoggedIn,
                        pendingSharedText = pendingSharedText,
                        onSharedTextConsumed = viewModel::consumeSharedText,
                        pendingCvOptimization = pendingCvOptimization,
                        onCvOptimizationConsumed = viewModel::consumeCvOptimization,
                        pendingChallengeDeepLink = pendingChallengeDeepLink,
                        onChallengeDeepLinkConsumed = viewModel::consumeChallengeDeepLink,
                        pendingCompanyInterviewDeepLink = pendingCompanyInterviewDeepLink,
                        onCompanyInterviewDeepLinkConsumed = viewModel::consumeCompanyInterviewDeepLink,
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        viewModel.acceptSharedText(intent.toPendingSharedText())
        viewModel.acceptCvOptimization(intent.toPendingCvOptimization())
        viewModel.acceptChallengeDeepLink(intent.toPendingChallengeDeepLink())
        viewModel.acceptCompanyInterviewDeepLink(intent.toPendingCompanyInterviewDeepLink())
    }
}


@Composable
fun shouldShowDarkTheme(
    uiState: MainUiState
): Boolean = when (uiState) {
    MainUiState.Loading -> isSystemInDarkTheme()
    is MainUiState.Ready ->
        when (uiState.userSettings.theme) {
            ThemeSetting.LIGHT -> false
            ThemeSetting.DARK -> true
            ThemeSetting.FOLLOW_SYSTEM -> isSystemInDarkTheme()
        }
}
