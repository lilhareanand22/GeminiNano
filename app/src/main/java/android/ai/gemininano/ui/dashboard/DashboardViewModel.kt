package android.ai.gemininano.ui.dashboard

import android.ai.gemininano.util.DemoConfig
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class DashboardViewModel : ViewModel() {

    private val capabilitiesList = listOf(
        ApiCapability(
            type = ApiType.PROMPT,
            title = "Prompt",
            description = "Generate content from custom text/multimodal inputs",
            iconSymbol = "✧",
            category = "Text Generation"
        ),
        ApiCapability(
            type = ApiType.SUMMARIZATION,
            title = "Summarization",
            description = "Condense articles and chats into bulleted lists",
            iconSymbol = "📝",
            category = "Text Utilities"
        ),
        ApiCapability(
            type = ApiType.PROOFREADING,
            title = "Proofreading",
            description = "Analyze and fix short chat messages instantly",
            iconSymbol = "✍️",
            category = "Refinement"
        ),
        ApiCapability(
            type = ApiType.REWRITING,
            title = "Rewriting",
            description = "Modify tone and style of text dynamically",
            iconSymbol = "🔄",
            category = "Refinement"
        ),
        ApiCapability(
            type = ApiType.IMAGE_DESCRIPTION,
            title = "Image Description",
            description = "Generate highly descriptive text for local images",
            iconSymbol = "🖼️",
            category = "Vision"
        ),
        ApiCapability(
            type = ApiType.SPEECH_RECOGNITION,
            title = "Speech Recognition",
            description = "Accurately transcribe audio to local text strings",
            iconSymbol = "🎙️",
            category = "Audio"
        )
    )

    private val _state = MutableStateFlow(
        DashboardState(capabilities = capabilitiesList)
    )
    val state: StateFlow<DashboardState> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<DashboardEffect>()
    val effect: SharedFlow<DashboardEffect> = _effect.asSharedFlow()

    init {
        DemoConfig.isDemoMode.onEach { isDemo ->
            _state.value = _state.value.copy(
                isDemoMode = isDemo,
                modelStatus = if (isDemo) "Demo Mode • Simulated Hardware" else "Gemini Nano v1.0 • Ready"
            )
        }.launchIn(viewModelScope)
    }

    fun onIntent(intent: DashboardIntent) {
        when (intent) {
            is DashboardIntent.OnApiCardClicked -> {
                viewModelScope.launch {
                    _effect.emit(DashboardEffect.NavigateToPlayground(intent.apiType))
                }
            }
            DashboardIntent.CheckUpdates -> {
                viewModelScope.launch {
                    _state.value = _state.value.copy(
                        isCheckingUpdates = true,
                        modelStatus = "Checking for local updates..."
                    )
                    delay(1500)
                    _state.value = _state.value.copy(
                        isCheckingUpdates = false,
                        modelStatus = "Gemini Nano v1.0 • Up to Date"
                    )
                }
            }
        }
    }
}
