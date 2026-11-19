package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.HowToVote
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.data.EmergencyMeetingAlert
import com.example.ui.theme.LocalImpostorPalette

@Composable
fun EmergencyMeetingOverlay(
    alert: EmergencyMeetingAlert,
    onProceedNow: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalImpostorPalette.current
    val pulseTransition = rememberInfiniteTransition(label = "emergency_meeting_pulse")
    val sirenPulse by pulseTransition.animateFloat(
        initialValue = 0.45f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 480, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "siren_pulse"
    )
    val shockwave by pulseTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "siren_shockwave"
    )
    val cardScale by animateFloatAsState(
        targetValue = if (alert.isActive) 1.0f else 0.9f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "emergency_card_scale"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background.copy(alpha = 0.92f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {}
            )
            .padding(12.dp)
            .testTag("emergency_meeting_overlay"),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val maxR = size.minDimension * 0.45f
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        palette.impostorAccent.copy(alpha = 0.28f * sirenPulse),
                        palette.timerAccent.copy(alpha = 0.08f),
                        Color.Transparent
                    ),
                    radius = maxR * 1.1f
                ),
                radius = maxR * 1.1f
            )
            drawCircle(
                color = palette.impostorGlow.copy(alpha = ((1.25f - shockwave).coerceIn(0f, 1f)) * 0.5f),
                radius = maxR * shockwave,
                style = Stroke(width = 3.dp.toPx())
            )
        }

        Surface(
            color = palette.impostorSurface.copy(alpha = 0.96f),
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(2.dp, palette.impostorGlow.copy(alpha = sirenPulse)),
            modifier = Modifier
                .widthIn(max = 480.dp)
                .fillMaxWidth()
                .graphicsLayer {
                    scaleX = cardScale
                    scaleY = cardScale
                }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                palette.impostorAccent.copy(alpha = 0.22f),
                                Color.Transparent,
                                palette.timerSurface.copy(alpha = 0.25f)
                            )
                        )
                    )
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        color = palette.impostorAccent.copy(alpha = 0.22f),
                        shape = CircleShape,
                        border = BorderStroke(2.dp, palette.impostorGlow.copy(alpha = sirenPulse)),
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Filled.Campaign,
                                contentDescription = "Emergency meeting siren alert",
                                tint = palette.impostorGlow,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    Surface(
                        color = palette.timerSurface,
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(1.dp, palette.timerAccent)
                    ) {
                        Text(
                            text = "REPORT INITIATED",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = palette.timerAccent,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                        )
                    }
                }

                Text(
                    text = "EMERGENCY MEETING",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = palette.impostorGlow,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Called by ${alert.reporterName.ifBlank { "Agent" }} • ${alert.reason}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.testTag("emergency_meeting_reporter")
                )

                Surface(
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        PulsingStatusDot(color = palette.timerAccent)
                        Spacer(modifier = Modifier.size(8.dp))
                        Text(
                            text = "Transitioning to Voting in ${alert.countdownSeconds.coerceAtLeast(1)}s...",
                            style = MaterialTheme.typography.labelLarge,
                            color = palette.timerAccent
                        )
                    }
                }

                Button(
                    onClick = onProceedNow,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = palette.impostorAccent,
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("emergency_proceed_to_vote_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.HowToVote,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text("Proceed to Voting Now", style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}
