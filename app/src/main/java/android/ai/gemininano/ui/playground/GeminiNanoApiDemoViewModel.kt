package android.ai.gemininano.ui.playground

import android.ai.gemininano.data.repository.GeminiNanoRepository
import android.ai.gemininano.ui.dashboard.ApiType
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.mlkit.genai.speechrecognition.SpeechRecognizerResponse
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class GeminiNanoApiDemoViewModel(
    application: Application,
    private val repository: GeminiNanoRepository,
    private val initialApiType: ApiType
) : AndroidViewModel(application) {

    private val _state = MutableStateFlow(GeminiNanoApiDemoState(apiType = initialApiType))
    val state: StateFlow<GeminiNanoApiDemoState> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<GeminiNanoApiDemoEffect>()
    val effect: SharedFlow<GeminiNanoApiDemoEffect> = _effect.asSharedFlow()

    private var recordingJob: Job? = null

    fun onIntent(intent: GeminiNanoApiDemoIntent) {
        when (intent) {
            is GeminiNanoApiDemoIntent.OnInputTextChanged -> {
                _state.value = _state.value.copy(inputText = intent.text)
            }
            is GeminiNanoApiDemoIntent.OnSecondInputTextChanged -> {
                _state.value = _state.value.copy(secondInputText = intent.text)
            }
            is GeminiNanoApiDemoIntent.OnImageSelected -> {
                _state.value = _state.value.copy(selectedImageResId = intent.imageResId)
            }
            is GeminiNanoApiDemoIntent.OnToneSelected -> {
                _state.value = _state.value.copy(toneOption = intent.tone)
            }
            is GeminiNanoApiDemoIntent.OnLengthSelected -> {
                _state.value = _state.value.copy(lengthOption = intent.length)
            }
            GeminiNanoApiDemoIntent.ToggleRecording -> {
                toggleSpeechRecording()
            }
            GeminiNanoApiDemoIntent.RunInference -> {
                triggerModelInference()
            }
            GeminiNanoApiDemoIntent.ClearOutput -> {
                _state.value = _state.value.copy(
                    outputText = "",
                    diffResult = emptyList(),
                    executionTimeMs = 0
                )
            }
            GeminiNanoApiDemoIntent.GoBack -> {
                viewModelScope.launch {
                    stopRecordingJob()
                    _effect.emit(GeminiNanoApiDemoEffect.NavigateBack)
                }
            }
        }
    }

    private fun toggleSpeechRecording() {
        val currentState = _state.value
        val nextRecordingState = !currentState.isRecording

        if (nextRecordingState) {
            // Start recording
            _state.value = currentState.copy(
                isRecording = true,
                recordingDurationSec = 0,
                outputText = "",
                executionTimeMs = 0
            )
            recordingJob = viewModelScope.launch {
                while (true) {
                    delay(1000)
                    _state.value = _state.value.copy(
                        recordingDurationSec = _state.value.recordingDurationSec + 1
                    )
                }
            }
        } else {
            // Stop recording
            stopRecordingJob()
            _state.value = _state.value.copy(isRecording = false)
        }
    }

    private fun stopRecordingJob() {
        recordingJob?.cancel()
        recordingJob = null
    }

    private fun triggerModelInference() {
        val currentState = _state.value
        if (currentState.isLoading) return

        viewModelScope.launch {
            _state.value = currentState.copy(isLoading = true)
            val startTime = System.currentTimeMillis()

            try {
                val result: Pair<String, List<DiffSegment>> = when (currentState.apiType) {
                    ApiType.PROMPT -> {
                        repository.generatePrompt(currentState.inputText) to emptyList()
                    }
                    ApiType.SUMMARIZATION -> {
                        repository.summarizeText(currentState.inputText) to emptyList()
                    }
                    ApiType.PROOFREADING -> {
                        repository.proofreadText(currentState.inputText)
                    }
                    ApiType.REWRITING -> {
                        repository.rewriteText(currentState.inputText, currentState.toneOption, currentState.lengthOption) to emptyList()
                    }
                    ApiType.IMAGE_DESCRIPTION -> {
                        repository.describeImage(currentState.selectedImageResId) to emptyList()
                    }
                    ApiType.SPEECH_RECOGNITION -> {
                        var transcribedText = ""
                        repository.transcribeSpeech().collect { response ->
                            when (response) {
                                is SpeechRecognizerResponse.PartialTextResponse -> {
                                    transcribedText = response.text
                                }
                                is SpeechRecognizerResponse.FinalTextResponse -> {
                                    transcribedText = response.text
                                }
                                is SpeechRecognizerResponse.ErrorResponse -> {
                                    throw IllegalStateException("Speech recognition error: ${response.e}")
                                }
                                is SpeechRecognizerResponse.CompletedResponse -> {
                                    // Complete
                                }
                            }
                        }
                        transcribedText to emptyList()
                    }
                }

                val duration = System.currentTimeMillis() - startTime
                _state.value = _state.value.copy(
                    isLoading = false,
                    outputText = result.first,
                    diffResult = result.second,
                    executionTimeMs = duration
                )
            } catch (e: Exception) {
                val duration = System.currentTimeMillis() - startTime
                _state.value = _state.value.copy(
                    isLoading = false,
                    outputText = "Error running inference: ${e.message}",
                    executionTimeMs = duration
                )
            }
        }
    }
}
