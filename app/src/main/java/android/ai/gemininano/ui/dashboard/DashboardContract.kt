package android.ai.gemininano.ui.dashboard

enum class ApiType(val id: String) {
    PROMPT("prompt"),
    SUMMARIZATION("summarization"),
    PROOFREADING("proofreading"),
    REWRITING("rewriting"),
    IMAGE_DESCRIPTION("image_description"),
    SPEECH_RECOGNITION("speech_recognition")
}

data class ApiCapability(
    val type: ApiType,
    val title: String,
    val description: String,
    val iconSymbol: String,
    val category: String
)

data class DashboardState(
    val capabilities: List<ApiCapability>,
    val modelStatus: String = "Gemini Nano v1.0 • Ready",
    val isCheckingUpdates: Boolean = false,
    val isDemoMode: Boolean = false
)

sealed interface DashboardIntent {
    data class OnApiCardClicked(val apiType: ApiType) : DashboardIntent
    object CheckUpdates : DashboardIntent
}

sealed interface DashboardEffect {
    data class NavigateToPlayground(val apiType: ApiType) : DashboardEffect
}
