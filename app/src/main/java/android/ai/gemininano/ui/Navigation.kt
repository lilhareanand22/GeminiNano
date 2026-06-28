package android.ai.gemininano.ui

import android.ai.gemininano.ui.dashboard.ApiType
import android.ai.gemininano.ui.dashboard.DashboardScreen
import android.ai.gemininano.ui.dashboard.DashboardViewModel
import android.ai.gemininano.ui.playground.GeminiNanoApiDemoScreen
import android.ai.gemininano.ui.playground.GeminiNanoApiDemoViewModel
import android.ai.gemininano.ui.splash.SplashScreen
import android.ai.gemininano.ui.splash.SplashViewModel
import androidx.compose.animation.Crossfade
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel

sealed interface Screen {
    object Splash : Screen
    object Dashboard : Screen
    data class GeminiNanoApiDemo(val apiType: ApiType) : Screen
}

@Composable
fun AppNavigation() {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Splash) }

    Crossfade(targetState = currentScreen, label = "screenTransition") { screen ->
        when (screen) {
            Screen.Splash -> {
                val splashViewModel: SplashViewModel = viewModel()
                SplashScreen(
                    viewModel = splashViewModel,
                    onNavigateToDashboard = { currentScreen = Screen.Dashboard }
                )
            }
            Screen.Dashboard -> {
                val dashboardViewModel: DashboardViewModel = viewModel()
                DashboardScreen(
                    viewModel = dashboardViewModel,
                    onNavigateToPlayground = { apiType ->
                        currentScreen = Screen.GeminiNanoApiDemo(apiType)
                    }
                )
            }
            is Screen.GeminiNanoApiDemo -> {
                val context = androidx.compose.ui.platform.LocalContext.current
                val application = context.applicationContext as android.app.Application
                val repository = android.ai.gemininano.data.repository.GeminiNanoRepositoryImpl(application)
                val demoViewModel: GeminiNanoApiDemoViewModel = viewModel(
                    key = screen.apiType.name,
                    factory = object : ViewModelProvider.Factory {
                        @Suppress("UNCHECKED_CAST")
                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                            return GeminiNanoApiDemoViewModel(application, repository, screen.apiType) as T
                        }
                    }
                )
                GeminiNanoApiDemoScreen(
                    viewModel = demoViewModel,
                    onNavigateBack = { currentScreen = Screen.Dashboard }
                )
            }
        }
    }
}
