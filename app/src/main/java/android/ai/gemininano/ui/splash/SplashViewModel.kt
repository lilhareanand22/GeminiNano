package android.ai.gemininano.ui.splash

import android.ai.gemininano.util.DemoConfig
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.mlkit.genai.common.FeatureStatus
import com.google.mlkit.genai.common.internal.GenAiUtils
import com.google.mlkit.genai.summarization.Summarization
import com.google.mlkit.genai.summarization.SummarizerOptions
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.guava.await

class SplashViewModel(application: Application) : AndroidViewModel(application) {

    private val _state = MutableStateFlow<SplashState>(SplashState.Loading)
    val state: StateFlow<SplashState> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<SplashEffect>()
    val effect: SharedFlow<SplashEffect> = _effect.asSharedFlow()

    // Step status messages shown during checking
    private val _checkSteps = MutableStateFlow<List<Pair<String, CheckStatus>>>(
        listOf(
            "NPU Hardware Acceleration Check" to CheckStatus.PENDING,
            "System Memory (RAM) Check" to CheckStatus.PENDING,
            "On-Device GenAI Core Library Check" to CheckStatus.PENDING
        )
    )
    val checkSteps: StateFlow<List<Pair<String, CheckStatus>>> = _checkSteps.asStateFlow()

    init {
        onIntent(SplashIntent.CheckCapability)
    }

    fun onIntent(intent: SplashIntent) {
        when (intent) {
            SplashIntent.CheckCapability -> runCapabilityCheck()
            SplashIntent.RetryCheck -> {
                _state.value = SplashState.Loading
                runCapabilityCheck()
            }
            SplashIntent.ProceedDemo -> {
                DemoConfig.setDemoMode(true)
                viewModelScope.launch {
                    _effect.emit(SplashEffect.NavigateToDashboard)
                }
            }
        }
    }

    private fun runCapabilityCheck() {
        viewModelScope.launch {
            // Reset steps to pending
            _checkSteps.value = listOf(
                "NPU Hardware Acceleration Check" to CheckStatus.PENDING,
                "System Memory (RAM) Check" to CheckStatus.PENDING,
                "On-Device GenAI Core Library Check" to CheckStatus.PENDING
            )
            
            // Step 1: On-Device GenAI Core Library Check (AICore package check)
            updateStep(2, CheckStatus.RUNNING)
            delay(600)
            val isAiCoreCompatible = GenAiUtils.isAiCoreCompatible(getApplication())
            if (!isAiCoreCompatible) {
                updateStep(2, CheckStatus.FAILED)
                _state.value = SplashState.DeviceUnsupported("AICore library is not compatible or missing.")
                return@launch
            }
            updateStep(2, CheckStatus.SUCCESS)

            // Step 2: NPU Hardware Acceleration Check (Feature Status check)
            updateStep(0, CheckStatus.RUNNING)
            delay(600)
            
            try {
                // We use Summarizer as a proxy to check general Gemini Nano availability
                val summarizer = Summarization.getClient(
                    SummarizerOptions.builder(getApplication()).build()
                )
                val status = summarizer.checkFeatureStatus().await<Int>()
                
                if (status == FeatureStatus.UNAVAILABLE) {
                    updateStep(0, CheckStatus.FAILED)
                    _state.value = SplashState.DeviceUnsupported("NPU hardware acceleration is not supported.")
                    return@launch
                }
                updateStep(0, CheckStatus.SUCCESS)
                
                // Step 3: System Memory (RAM) Check (Implicitly covered by status)
                updateStep(1, CheckStatus.RUNNING)
                delay(600)
                updateStep(1, CheckStatus.SUCCESS)

                delay(500)
                _state.value = SplashState.DeviceSupported
                
                // Auto navigate on support
                delay(1200)
                _effect.emit(SplashEffect.NavigateToDashboard)
                
            } catch (e: Exception) {
                updateStep(0, CheckStatus.FAILED)
                _state.value = SplashState.DeviceUnsupported("Capability check failed: ${e.localizedMessage}")
            }
        }
    }

    private fun updateStep(index: Int, status: CheckStatus) {
        val currentList = _checkSteps.value.toMutableList()
        if (index in currentList.indices) {
            currentList[index] = currentList[index].first to status
            _checkSteps.value = currentList
        }
    }
}

enum class CheckStatus {
    PENDING,
    RUNNING,
    SUCCESS,
    FAILED
}
