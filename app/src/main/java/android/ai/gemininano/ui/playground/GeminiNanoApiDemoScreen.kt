package android.ai.gemininano.ui.playground

import android.ai.gemininano.ui.dashboard.ApiType
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun GeminiNanoApiDemoScreen(
    viewModel: GeminiNanoApiDemoViewModel,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val clipboardManager = LocalClipboardManager.current

    LaunchedEffect(key1 = Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                GeminiNanoApiDemoEffect.NavigateBack -> onNavigateBack()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when (state.apiType) {
                            ApiType.PROMPT -> "Prompt Playground"
                            ApiType.SUMMARIZATION -> "Summarization"
                            ApiType.PROOFREADING -> "Proofreading"
                            ApiType.REWRITING -> "Rewriting Suite"
                            ApiType.IMAGE_DESCRIPTION -> "Image Description"
                            ApiType.SPEECH_RECOGNITION -> "Speech Recognition"
                        },
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { viewModel.onIntent(GeminiNanoApiDemoIntent.GoBack) }) {
                        // Standard Unicode arrow for back navigation
                        Text(
                            text = "←",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Model Details Header Card
            ModelDetailsHeaderCard(apiType = state.apiType)

            // Dynamic Input Section Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "INPUT CONTROL",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    when (state.apiType) {
                        ApiType.PROMPT -> {
                            OutlinedTextField(
                                value = state.inputText,
                                onValueChange = { viewModel.onIntent(GeminiNanoApiDemoIntent.OnInputTextChanged(it)) },
                                placeholder = { Text("Enter a prompt (e.g. 'Write a poem about Kotlin' or 'Explain NPU')") },
                                modifier = Modifier.fillMaxWidth(),
                                minLines = 4,
                                maxLines = 8,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                                )
                            )
                        }
                        ApiType.SUMMARIZATION -> {
                            OutlinedTextField(
                                value = state.inputText,
                                onValueChange = { viewModel.onIntent(GeminiNanoApiDemoIntent.OnInputTextChanged(it)) },
                                placeholder = { Text("Paste the text or article you want to summarize here...") },
                                modifier = Modifier.fillMaxWidth(),
                                minLines = 5,
                                maxLines = 10,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                                )
                            )
                        }
                        ApiType.PROOFREADING -> {
                            OutlinedTextField(
                                value = state.inputText,
                                onValueChange = { viewModel.onIntent(GeminiNanoApiDemoIntent.OnInputTextChanged(it)) },
                                placeholder = { Text("Enter a draft sentence containing spelling or grammar errors...") },
                                modifier = Modifier.fillMaxWidth(),
                                minLines = 3,
                                maxLines = 6,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                                )
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Example: He go to school yesterday and see his teacher.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        ApiType.REWRITING -> {
                            OutlinedTextField(
                                value = state.inputText,
                                onValueChange = { viewModel.onIntent(GeminiNanoApiDemoIntent.OnInputTextChanged(it)) },
                                placeholder = { Text("Enter draft text to rewrite...") },
                                modifier = Modifier.fillMaxWidth(),
                                minLines = 3,
                                maxLines = 6,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                                )
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "Tone Settings",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf("Professional", "Casual", "Concise", "Witty").forEach { tone ->
                                    FilterChip(
                                        selected = state.toneOption == tone,
                                        onClick = { viewModel.onIntent(GeminiNanoApiDemoIntent.OnToneSelected(tone)) },
                                        label = { Text(tone) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "Output Length",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf("Short", "Long").forEach { length ->
                                    FilterChip(
                                        selected = state.lengthOption == length,
                                        onClick = { viewModel.onIntent(GeminiNanoApiDemoIntent.OnLengthSelected(length)) },
                                        label = { Text(length) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    )
                                }
                            }
                        }
                        ApiType.IMAGE_DESCRIPTION -> {
                            Text(
                                text = "Select a mock image to process:",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                val mockImages = listOf(
                                    1 to "Processor Chip",
                                    2 to "Smart Home",
                                    3 to "Code Editor"
                                )
                                mockImages.forEach { (id, name) ->
                                    val isSelected = state.selectedImageResId == id
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(80.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(
                                                if (isSelected) MaterialTheme.colorScheme.primaryContainer
                                                else MaterialTheme.colorScheme.surfaceContainerHighest
                                            )
                                            .border(
                                                width = 2.dp,
                                                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                                shape = RoundedCornerShape(8.dp)
                                            )
                                            .clickable {
                                                viewModel.onIntent(
                                                    GeminiNanoApiDemoIntent.OnImageSelected(
                                                        if (isSelected) null else id
                                                    )
                                                )
                                            }
                                            .padding(8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(
                                                text = when (id) {
                                                    1 -> "💾"
                                                    2 -> "🏠"
                                                    else -> "💻"
                                                },
                                                fontSize = 24.sp
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = name,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        ApiType.SPEECH_RECOGNITION -> {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                val buttonColor by animateColorAsState(
                                    targetValue = if (state.isRecording) MaterialTheme.colorScheme.error
                                    else MaterialTheme.colorScheme.primary,
                                    label = "buttonColor"
                                )

                                // Recording Ripple/Pulse Animation
                                val transition = rememberInfiniteTransition(label = "ripple")
                                val scale by transition.animateFloat(
                                    initialValue = 1.0f,
                                    targetValue = 1.25f,
                                    animationSpec = infiniteRepeatable(
                                        animation = tween(1200),
                                        repeatMode = RepeatMode.Restart
                                    ),
                                    label = "scale"
                                )
                                val alpha by transition.animateFloat(
                                    initialValue = 0.5f,
                                    targetValue = 0.0f,
                                    animationSpec = infiniteRepeatable(
                                        animation = tween(1200),
                                        repeatMode = RepeatMode.Restart
                                    ),
                                    label = "alpha"
                                )

                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.size(140.dp)
                                ) {
                                    if (state.isRecording) {
                                        Box(
                                            modifier = Modifier
                                                .size(100.dp)
                                                .alpha(alpha)
                                                .align(Alignment.Center)
                                                .background(buttonColor.copy(alpha = 0.4f), CircleShape)
                                                .size((100.dp * scale))
                                        )
                                    }
                                    Box(
                                        modifier = Modifier
                                            .size(90.dp)
                                            .clip(CircleShape)
                                            .background(buttonColor)
                                            .clickable { viewModel.onIntent(GeminiNanoApiDemoIntent.ToggleRecording) },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = if (state.isRecording) "■" else "🎙️",
                                            color = Color.White,
                                            fontSize = if (state.isRecording) 28.sp else 36.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = if (state.isRecording) "Recording... Listening" else "Tap Mic to Record",
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (state.isRecording) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                                )

                                // Timer display
                                val minutes = state.recordingDurationSec / 60
                                val seconds = state.recordingDurationSec % 60
                                Text(
                                    text = String.format("%02d:%02d", minutes, seconds),
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                // Simple simulated audio wave bars
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .height(30.dp)
                                        .alpha(if (state.isRecording) 1.0f else 0.0f)
                                ) {
                                    val barHeights = listOf(12, 28, 16, 24, 8, 20, 14, 26, 10, 18, 12)
                                    barHeights.forEach { heightVal ->
                                        // Animate heights loosely if recording
                                        val waveTransition = rememberInfiniteTransition(label = "wave")
                                        val waveScale by waveTransition.animateFloat(
                                            initialValue = 0.4f,
                                            targetValue = 1.0f,
                                            animationSpec = infiniteRepeatable(
                                                animation = tween((500..900).random()),
                                                repeatMode = RepeatMode.Reverse
                                            ),
                                            label = "waveScale"
                                        )
                                        val actualHeight = if (state.isRecording) heightVal.dp * waveScale else 4.dp

                                        Box(
                                            modifier = Modifier
                                                .width(3.dp)
                                                .height(actualHeight)
                                                .clip(CircleShape)
                                                .background(MaterialTheme.colorScheme.primary)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Inference Button
                    val isEnabled = when (state.apiType) {
                        ApiType.IMAGE_DESCRIPTION -> state.selectedImageResId != null && !state.isLoading
                        ApiType.SPEECH_RECOGNITION -> state.recordingDurationSec > 0 && !state.isRecording && !state.isLoading
                        else -> state.inputText.isNotEmpty() && !state.isLoading
                    }

                    Button(
                        onClick = { viewModel.onIntent(GeminiNanoApiDemoIntent.RunInference) },
                        enabled = isEnabled,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        if (state.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Processing On-Device...")
                        } else {
                            Text(
                                text = "Run Inference Local (NPU)",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Output Results Container Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "OUTPUT & PERFORMANCE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 0.5.sp
                        )

                        if (state.outputText.isNotEmpty() || state.diffResult.isNotEmpty()) {
                            Text(
                                text = "Clear",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .clickable { viewModel.onIntent(GeminiNanoApiDemoIntent.ClearOutput) }
                                    .padding(4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (state.isLoading) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Executing neural layers locally...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else if (state.outputText.isEmpty() && state.diffResult.isEmpty()) {
                        // Placeholder state
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "graphic_eq", // Placeholder textual symbol
                                    style = MaterialTheme.typography.headlineLarge,
                                    color = MaterialTheme.colorScheme.outlineVariant,
                                    fontSize = 48.sp
                                )
                                Text(
                                    text = "Local results will render here.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                )
                            }
                        }
                    } else {
                        // Execution info header
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Latency: ${state.executionTimeMs} ms",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Hardware: Local NPU (1.0x)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF4CAF50)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Results content
                        if (state.apiType == ApiType.PROOFREADING && state.diffResult.isNotEmpty()) {
                            // Render annotated diff
                            val annotatedDiff = buildAnnotatedStringFromDiff(state.diffResult)
                            Text(
                                text = annotatedDiff,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.fillMaxWidth()
                            )
                        } else {
                            Text(
                                text = state.outputText,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Actions
                        Button(
                            onClick = {
                                clipboardManager.setText(
                                    AnnotatedString(
                                        if (state.apiType == ApiType.PROOFREADING && state.diffResult.isNotEmpty()) {
                                            state.outputText
                                        } else {
                                            state.outputText
                                        }
                                    )
                                )
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = MaterialTheme.colorScheme.primary
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier
                                .align(Alignment.End)
                                .height(32.dp)
                        ) {
                            Text("Copy Output", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ModelDetailsHeaderCard(apiType: ApiType) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val modelTag = when (apiType) {
                    ApiType.SPEECH_RECOGNITION -> "Whisper Tiny v3"
                    ApiType.IMAGE_DESCRIPTION -> "MobileViT v2"
                    else -> "Gemini Nano v1.0"
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = modelTag,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "🔒",
                        fontSize = 12.sp
                    )
                    Text(
                        text = "On-Device Privacy Secured",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            val (title, info) = when (apiType) {
                ApiType.PROMPT -> "Direct Prompting" to "Generates a smart textual continuation based on your custom prompt text and configurations locally."
                ApiType.SUMMARIZATION -> "Text Summarization" to "Uses local keyphrase extraction and abstraction algorithms to summarize long articles in seconds."
                ApiType.PROOFREADING -> "Local Proofreading" to "Performs localized grammar, spelling and punctuation corrections. Displays inline changes instantly."
                ApiType.REWRITING -> "Rewriting & Stylist" to "Configures the style, tone, and length parameters of your draft to customize text delivery."
                ApiType.IMAGE_DESCRIPTION -> "Vision Summarization" to "Runs low-latency neural image captioning to describe scene contexts without cloud processing."
                ApiType.SPEECH_RECOGNITION -> "Universal Speech-to-Text" to "Converts raw audio waves to text strings. Supports multi-language local translation."
            }

            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = info,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// Helper to construct annotated string representation of text diffs
fun buildAnnotatedStringFromDiff(diffSegments: List<DiffSegment>): AnnotatedString {
    return buildAnnotatedString {
        diffSegments.forEach { segment ->
            when (segment.type) {
                DiffType.UNCHANGED -> {
                    append(segment.text)
                }
                DiffType.DELETED -> {
                    withStyle(
                        style = SpanStyle(
                            color = Color(0xFFC62828),
                            textDecoration = TextDecoration.LineThrough,
                            background = Color(0xFFFFEBEE)
                        )
                    ) {
                        append(segment.text)
                    }
                }
                DiffType.ADDED -> {
                    withStyle(
                        style = SpanStyle(
                            color = Color(0xFF2E7D32),
                            fontWeight = FontWeight.Bold,
                            background = Color(0xFFE8F5E9)
                        )
                    ) {
                        append(segment.text)
                    }
                }
            }
        }
    }
}
