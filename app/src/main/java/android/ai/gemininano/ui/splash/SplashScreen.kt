package android.ai.gemininano.ui.splash

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.collectLatest

// Success green color
private val SuccessGreen = Color(0xFF4CAF50)

@Composable
fun SplashScreen(
    viewModel: SplashViewModel,
    onNavigateToDashboard: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val steps by viewModel.checkSteps.collectAsStateWithLifecycle()

    // Observe navigation effects
    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                SplashEffect.NavigateToDashboard -> onNavigateToDashboard()
            }
        }
    }

    SplashScreenContent(
        state = state,
        steps = steps,
        onIntent = viewModel::onIntent
    )
}

@Composable
fun SplashScreenContent(
    state: SplashState,
    steps: List<Pair<String, CheckStatus>>,
    onIntent: (SplashIntent) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            BrandingHeader()

            Spacer(modifier = Modifier.height(48.dp))

            CapabilityCheckList(steps = steps)

            Spacer(modifier = Modifier.height(32.dp))

            StatusSection(state = state, onIntent = onIntent)
        }
    }
}

@Composable
private fun BrandingHeader() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "✧",
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontSize = 44.sp,
                fontWeight = FontWeight.Light
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Gemini Nano",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Text(
            text = "Local Hardware Capability Check",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun CapabilityCheckList(steps: List<Pair<String, CheckStatus>>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        steps.forEach { (title, status) ->
            CheckStepRow(title = title, status = status)
        }
    }
}

@Composable
private fun CheckStepRow(title: String, status: CheckStatus) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        StatusIcon(status = status)

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = when (status) {
                CheckStatus.PENDING -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                CheckStatus.RUNNING -> MaterialTheme.colorScheme.primary
                else -> MaterialTheme.colorScheme.onSurface
            },
            fontWeight = if (status == CheckStatus.RUNNING) FontWeight.SemiBold else FontWeight.Normal,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun StatusIcon(status: CheckStatus) {
    when (status) {
        CheckStatus.PENDING -> {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            )
        }
        CheckStatus.RUNNING -> {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                strokeWidth = 2.5.dp,
                color = MaterialTheme.colorScheme.primary
            )
        }
        CheckStatus.SUCCESS -> {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(SuccessGreen),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "✓",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        CheckStatus.FAILED -> {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.error),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "✕",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun StatusSection(
    state: SplashState,
    onIntent: (SplashIntent) -> Unit
) {
    AnimatedVisibility(
        visible = state is SplashState.DeviceSupported,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Text(
            text = "Gemini Nano is supported! Redirecting to Dashboard...",
            color = SuccessGreen,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center
        )
    }

    AnimatedVisibility(
        visible = state is SplashState.DeviceUnsupported,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            val reason = (state as? SplashState.DeviceUnsupported)?.reason ?: "Hardware requirement check failed."
            Text(
                text = reason,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = { onIntent(SplashIntent.RetryCheck) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("Retry Check")
                }

                Button(
                    onClick = { onIntent(SplashIntent.ProceedDemo) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                ) {
                    Text("Demo Mode")
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SplashScreenContentPreview() {
    MaterialTheme {
        Surface {
            SplashScreenContent(
                state = SplashState.Loading,
                steps = listOf(
                    "NPU Hardware Acceleration Check" to CheckStatus.SUCCESS,
                    "System Memory (RAM) Check" to CheckStatus.RUNNING,
                    "On-Device GenAI Core Library Check" to CheckStatus.PENDING
                ),
                onIntent = {}
            )
        }
    }
}
