# Skill: Implement_MLKit_GenAI_Gemini_Nano

## Context
This skill outlines how to interface with the local ML Kit GenAI client SDKs powered by Gemini Nano through the Android `AICore` service.

## 1. Feature Availability Wrapper
Before executing inference on any ML Kit GenAI API, check model availability using `FeatureStatus` flags to handle asynchronous LoRA adapter downloads smoothly.

```kotlin
import com.google.mlkit.nl.genai.common.FeatureStatus
import kotlinx.coroutines.tasks.await

suspend fun <T> runWithModelCheck(
    checkStatus: suspend () -> FeatureStatus,
    downloadModel: suspend () -> Unit,
    onReady: suspend () -> T
): Result<T> = runCatching {
    when (val status = checkStatus()) {
        FeatureStatus.AVAILABLE -> onReady()
        FeatureStatus.DOWNLOADABLE -> {
            downloadModel()
            throw IllegalStateException("Model download initiated. Please try again when ready.")
        }
        FeatureStatus.DOWNLOADING -> {
            throw IllegalStateException("Model is currently downloading via AICore.")
        }
        FeatureStatus.UNAVAILABLE -> {
            throw UnsupportedOperationException("This on-device GenAI feature is not supported on this hardware configuration.")
        }
        else -> throw IllegalStateException("Unknown AICore device status state: $status")
    }
}