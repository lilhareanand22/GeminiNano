package android.ai.gemininano.data.repository

import android.ai.gemininano.ui.playground.DiffSegment
import com.google.mlkit.genai.speechrecognition.SpeechRecognizerResponse
import kotlinx.coroutines.flow.Flow

interface GeminiNanoRepository {
    suspend fun generatePrompt(inputText: String): String
    suspend fun summarizeText(inputText: String): String
    suspend fun proofreadText(inputText: String): Pair<String, List<DiffSegment>>
    suspend fun rewriteText(inputText: String, tone: String, length: String): String
    suspend fun describeImage(selectedImageResId: Int?): String
    suspend fun transcribeSpeech(): Flow<SpeechRecognizerResponse>
}
