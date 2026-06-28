package android.ai.gemininano.util

import com.google.mlkit.genai.common.FeatureStatus


/**
 * Feature availability wrapper to handle Gemini Nano model check results and download routines.
 */
suspend fun <T> runWithModelCheck(
    checkStatus: suspend () -> Int,
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
