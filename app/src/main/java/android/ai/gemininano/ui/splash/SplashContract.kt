package android.ai.gemininano.ui.splash

sealed interface SplashState {
    object Loading : SplashState
    object DeviceSupported : SplashState
    data class DeviceUnsupported(val reason: String) : SplashState
}

sealed interface SplashIntent {
    object CheckCapability : SplashIntent
    object RetryCheck : SplashIntent
    object ProceedDemo : SplashIntent
}

sealed interface SplashEffect {
    object NavigateToDashboard : SplashEffect
}
