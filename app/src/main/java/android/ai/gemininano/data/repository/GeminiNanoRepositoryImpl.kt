package android.ai.gemininano.data.repository

import android.ai.gemininano.ui.playground.DiffSegment
import android.ai.gemininano.ui.playground.DiffType
import android.ai.gemininano.util.DemoConfig
import android.ai.gemininano.util.runWithModelCheck
import android.app.Application
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import com.google.mlkit.genai.common.FeatureStatus
import com.google.mlkit.genai.common.DownloadCallback
import com.google.mlkit.genai.common.GenAiException
import com.google.mlkit.genai.common.DownloadStatus
import com.google.mlkit.genai.common.audio.AudioSource
import com.google.mlkit.genai.prompt.Generation
import com.google.mlkit.genai.summarization.Summarization
import com.google.mlkit.genai.summarization.SummarizerOptions
import com.google.mlkit.genai.summarization.SummarizationRequest
import com.google.mlkit.genai.proofreading.Proofreading
import com.google.mlkit.genai.proofreading.ProofreaderOptions
import com.google.mlkit.genai.proofreading.ProofreadingRequest
import com.google.mlkit.genai.rewriting.Rewriting
import com.google.mlkit.genai.rewriting.RewriterOptions
import com.google.mlkit.genai.rewriting.RewritingRequest
import com.google.mlkit.genai.imagedescription.ImageDescription
import com.google.mlkit.genai.imagedescription.ImageDescriberOptions
import com.google.mlkit.genai.imagedescription.ImageDescriptionRequest
import com.google.mlkit.genai.speechrecognition.SpeechRecognition
import com.google.mlkit.genai.speechrecognition.SpeechRecognizerOptions
import com.google.mlkit.genai.speechrecognition.speechRecognizerOptions
import com.google.mlkit.genai.speechrecognition.SpeechRecognizerRequest
import com.google.mlkit.genai.speechrecognition.SpeechRecognizerResponse
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.guava.await
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.delay
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class GeminiNanoRepositoryImpl(
    private val application: Application
) : GeminiNanoRepository {

    override suspend fun generatePrompt(inputText: String): String {
        return try {
            val client = Generation.getClient()
            val result = runWithModelCheck<String>(
                checkStatus = { client.checkStatus() },
                downloadModel = {
                    client.download().collect { downloadStatus ->
                        if (downloadStatus is DownloadStatus.DownloadCompleted) {
                            return@collect
                        } else if (downloadStatus is DownloadStatus.DownloadFailed) {
                            throw IllegalStateException("Model download failed.")
                        }
                    }
                },
                onReady = {
                    val response = client.generateContent(inputText)
                    response.candidates.firstOrNull()?.text ?: ""
                }
            )
            result.getOrThrow()
        } catch (e: Exception) {
            "Device is not supported\n\n${e.message}"
        }
    }

    override suspend fun summarizeText(inputText: String): String {
        return try {
            val options = SummarizerOptions.builder(application)
                .setInputType(SummarizerOptions.InputType.ARTICLE)
                .setOutputType(SummarizerOptions.OutputType.ONE_BULLET)
                .build()
            val client = Summarization.getClient(options)
            val result = runWithModelCheck<String>(
                checkStatus = { client.checkFeatureStatus().await<Int>() },
                downloadModel = {
                    suspendCancellableCoroutine<Unit> { cont ->
                        client.downloadFeature(object : DownloadCallback {
                            override fun onDownloadStarted(bytesToDownload: Long) {}
                            override fun onDownloadProgress(totalBytesDownloaded: Long) {}
                            override fun onDownloadCompleted() {
                                if (cont.isActive) cont.resume(Unit)
                            }
                            override fun onDownloadFailed(e: GenAiException) {
                                if (cont.isActive) cont.resumeWithException(e)
                            }
                        })
                    }
                },
                onReady = {
                    val request = SummarizationRequest.builder(inputText).build()
                    client.runInference(request).await().summary
                }
            )
            result.getOrThrow()
        } catch (e: Exception) {
            "Device is not supported\n\n${e.message}"
        }
    }

    override suspend fun proofreadText(inputText: String): Pair<String, List<DiffSegment>> {
        return try {
            val options = ProofreaderOptions.builder(application)
                .setInputType(ProofreaderOptions.InputType.KEYBOARD)
                .build()
            val client = Proofreading.getClient(options)
            val result = runWithModelCheck<String>(
                checkStatus = { client.checkFeatureStatus().await<Int>() },
                downloadModel = {
                    suspendCancellableCoroutine<Unit> { cont ->
                        client.downloadFeature(object : DownloadCallback {
                            override fun onDownloadStarted(bytesToDownload: Long) {}
                            override fun onDownloadProgress(totalBytesDownloaded: Long) {}
                            override fun onDownloadCompleted() {
                                if (cont.isActive) cont.resume(Unit)
                            }
                            override fun onDownloadFailed(e: GenAiException) {
                                if (cont.isActive) cont.resumeWithException(e)
                            }
                        })
                    }
                },
                onReady = {
                    val request = ProofreadingRequest.builder(inputText).build()
                    val res = client.runInference(request).await()
                    res.results.firstOrNull()?.text ?: inputText
                }
            )
            val corrected = result.getOrThrow()
            corrected to calculateDiff(inputText, corrected)
        } catch (e: Exception) {
            "Device is not supported\n\n${e.message}" to emptyList()
        }
    }

    override suspend fun rewriteText(inputText: String, tone: String, length: String): String {
        return try {
            val outputType = when {
                length == "Short" -> RewriterOptions.OutputType.SHORTEN
                length == "Long" && tone != "Concise" -> RewriterOptions.OutputType.ELABORATE
                else -> when (tone) {
                    "Professional" -> RewriterOptions.OutputType.PROFESSIONAL
                    "Casual" -> RewriterOptions.OutputType.FRIENDLY
                    "Concise" -> RewriterOptions.OutputType.SHORTEN
                    else -> RewriterOptions.OutputType.REPHRASE
                }
            }
            val options = RewriterOptions.builder(application)
                .setOutputType(outputType)
                .build()
            val client = Rewriting.getClient(options)
            val result = runWithModelCheck<String>(
                checkStatus = { client.checkFeatureStatus().await<Int>() },
                downloadModel = {
                    suspendCancellableCoroutine<Unit> { cont ->
                        client.downloadFeature(object : DownloadCallback {
                            override fun onDownloadStarted(bytesToDownload: Long) {}
                            override fun onDownloadProgress(totalBytesDownloaded: Long) {}
                            override fun onDownloadCompleted() {
                                if (cont.isActive) cont.resume(Unit)
                            }
                            override fun onDownloadFailed(e: GenAiException) {
                                if (cont.isActive) cont.resumeWithException(e)
                            }
                        })
                    }
                },
                onReady = {
                    val request = RewritingRequest.builder(inputText).build()
                    val res = client.runInference(request).await()
                    res.results.firstOrNull()?.text ?: inputText
                }
            )
            result.getOrThrow()
        } catch (e: Exception) {
            "Device is not supported\n\n${e.message}"
        }
    }

    override suspend fun describeImage(selectedImageResId: Int?): String {
        return try {
            val options = ImageDescriberOptions.builder(application).build()
            val client = ImageDescription.getClient(options)
            val result = runWithModelCheck<String>(
                checkStatus = { client.checkFeatureStatus().await<Int>() },
                downloadModel = {
                    suspendCancellableCoroutine<Unit> { cont ->
                        client.downloadFeature(object : DownloadCallback {
                            override fun onDownloadStarted(bytesToDownload: Long) {}
                            override fun onDownloadProgress(totalBytesDownloaded: Long) {}
                            override fun onDownloadCompleted() {
                                if (cont.isActive) cont.resume(Unit)
                            }
                            override fun onDownloadFailed(e: GenAiException) {
                                if (cont.isActive) cont.resumeWithException(e)
                            }
                        })
                    }
                },
                onReady = {
                    // Generate a dummy Bitmap to feed to the model descriptor
                    val bitmap = Bitmap.createBitmap(256, 256, Bitmap.Config.ARGB_8888)
                    val canvas = Canvas(bitmap)
                    val paint = Paint()
                    paint.color = Color.BLUE
                    canvas.drawRect(0f, 0f, 256f, 256f, paint)

                    val request = ImageDescriptionRequest.builder(bitmap).build()
                    client.runInference(request).await().description
                }
            )
            result.getOrThrow()
        } catch (e: Exception) {
            "Device is not supported\n\n${e.message}"
        }
    }

    override suspend fun transcribeSpeech(): Flow<SpeechRecognizerResponse> = flow {
        try {
            val client = SpeechRecognition.getClient(
                speechRecognizerOptions {
                    preferredMode = SpeechRecognizerOptions.Mode.MODE_ADVANCED
                }
            )
            
            val result = runWithModelCheck<Flow<SpeechRecognizerResponse>>(
                checkStatus = { client.checkStatus() },
                downloadModel = {
                    client.download().collect { downloadStatus ->
                        if (downloadStatus is DownloadStatus.DownloadCompleted) {
                            return@collect
                        } else if (downloadStatus is DownloadStatus.DownloadFailed) {
                            throw IllegalStateException("Model download failed.")
                        }
                    }
                },
                onReady = {
                    val request = SpeechRecognizerRequest.Builder().apply {
                        audioSource = AudioSource.fromMic()
                    }.build()
                    client.startRecognition(request)
                }
            )
            
            result.getOrThrow().collect {
                emit(it)
            }
        } catch (e: Exception) {
            emit(SpeechRecognizerResponse.FinalTextResponse(
                "Device is not supported\n\n${e.message}"
            ))
            emit(SpeechRecognizerResponse.CompletedResponse)
        }
    }

    private fun calculateDiff(original: String, corrected: String): List<DiffSegment> {
        val diffs = mutableListOf<DiffSegment>()
        val originalWords = original.split(" ")
        val correctedWords = corrected.split(" ")
        
        var i = 0
        var j = 0
        while (i < originalWords.size && j < correctedWords.size) {
            if (originalWords[i] == correctedWords[j]) {
                diffs.add(DiffSegment(originalWords[i] + " ", DiffType.UNCHANGED))
                i++
                j++
            } else {
                diffs.add(DiffSegment(originalWords[i] + " ", DiffType.DELETED))
                diffs.add(DiffSegment(correctedWords[j] + " ", DiffType.ADDED))
                i++
                j++
            }
        }
        while (i < originalWords.size) {
            diffs.add(DiffSegment(originalWords[i] + " ", DiffType.DELETED))
            i++
        }
        while (j < correctedWords.size) {
            diffs.add(DiffSegment(correctedWords[j] + " ", DiffType.ADDED))
            j++
        }
        return diffs
    }
}
