package android.ai.gemininano.ui.playground

import android.ai.gemininano.ui.dashboard.ApiType

enum class DiffType {
    UNCHANGED,
    ADDED,
    DELETED
}

data class DiffSegment(
    val text: String,
    val type: DiffType
)

data class GeminiNanoApiDemoState(
    val apiType: ApiType,
    val inputText: String = "",
    val secondInputText: String = "",
    val selectedImageResId: Int? = null,
    val isRecording: Boolean = false,
    val recordingDurationSec: Int = 0,
    val outputText: String = "",
    val diffResult: List<DiffSegment> = emptyList(),
    val isLoading: Boolean = false,
    val executionTimeMs: Long = 0,
    val toneOption: String = "Professional",
    val lengthOption: String = "Short"
)

sealed interface GeminiNanoApiDemoIntent {
    data class OnInputTextChanged(val text: String) : GeminiNanoApiDemoIntent
    data class OnSecondInputTextChanged(val text: String) : GeminiNanoApiDemoIntent
    data class OnImageSelected(val imageResId: Int?) : GeminiNanoApiDemoIntent
    data class OnToneSelected(val tone: String) : GeminiNanoApiDemoIntent
    data class OnLengthSelected(val length: String) : GeminiNanoApiDemoIntent
    object ToggleRecording : GeminiNanoApiDemoIntent
    object RunInference : GeminiNanoApiDemoIntent
    object ClearOutput : GeminiNanoApiDemoIntent
    object GoBack : GeminiNanoApiDemoIntent
}

sealed interface GeminiNanoApiDemoEffect {
    object NavigateBack : GeminiNanoApiDemoEffect
}
