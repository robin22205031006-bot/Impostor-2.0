package com.example.ui.components

import android.content.Context
import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LocalImpostorPalette
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import java.util.Locale
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

data class TurnExpiredBroadcast(
    val playerName: String,
    val turnIndex: Int,
    val totalTurns: Int,
    val timestampMs: Long
)

object TurnTimerEventBus {
    const val ACTION_TURN_TIMER_EXPIRED = "com.example.ACTION_TURN_TIMER_EXPIRED"
    const val EXTRA_PLAYER_NAME = "extra_player_name"
    const val EXTRA_TURN_INDEX = "extra_turn_index"
    const val EXTRA_TOTAL_TURNS = "extra_total_turns"

    private val _events = MutableSharedFlow<TurnExpiredBroadcast>(extraBufferCapacity = 8)
    val events: SharedFlow<TurnExpiredBroadcast> = _events.asSharedFlow()

    @Volatile
    private var lastSignature: String = ""

    @Volatile
    private var lastEmitMs: Long = 0L

    fun emitExpired(
        context: Context,
        playerName: String,
        turnIndex: Int,
        totalTurns: Int
    ) {
        val now = System.currentTimeMillis()
        val signature = "$turnIndex|$totalTurns|$playerName"
        if (signature == lastSignature && (now - lastEmitMs) < 1500L) {
            return
        }
        lastSignature = signature
        lastEmitMs = now

        _events.tryEmit(
            TurnExpiredBroadcast(
                playerName = playerName,
                turnIndex = turnIndex,
                totalTurns = totalTurns,
                timestampMs = now
            )
        )
        try {
            val intent = Intent(ACTION_TURN_TIMER_EXPIRED).apply {
                setPackage(context.packageName)
                putExtra(EXTRA_PLAYER_NAME, playerName)
                putExtra(EXTRA_TURN_INDEX, turnIndex)
                putExtra(EXTRA_TOTAL_TURNS, totalTurns)
            }
            context.sendBroadcast(intent)
        } catch (_: Throwable) {
            // Safe no-op in isolated unit-test environments
        }
    }
}

@Composable
fun CircularCountdownTimer(
    remainingSeconds: Int,
    totalSeconds: Int,
    isPaused: Boolean,
    modifier: Modifier = Modifier,
    size: Dp = 104.dp,
    strokeWidth: Dp = 8.dp,
    onTimeExpired: () -> Unit = {}
) {
    val palette = LocalImpostorPalette.current
    val safeTotal = totalSeconds.coerceAtLeast(1)
    val clampedRemaining = remainingSeconds.coerceIn(0, safeTotal)
    val isExpired = !isPaused && clampedRemaining == 0
    val isUrgent = !isPaused && clampedRemaining in 1..5

    var previousRemaining by remember { mutableIntStateOf(clampedRemaining) }
    LaunchedEffect(clampedRemaining, isPaused) {
        if (!isPaused && previousRemaining > 0 && clampedRemaining == 0) {
            onTimeExpired()
        }
        previousRemaining = clampedRemaining
    }

    val targetProgress = clampedRemaining.toFloat() / safeTotal.toFloat()
    val animatedProgress by animateFloatAsState(
        targetValue = targetProgress,
        animationSpec = tween(durationMillis = 880, easing = LinearEasing),
        label = "circular_timer_progress"
    )

    val timerColor by animateColorAsState(
        targetValue = when {
            isExpired -> palette.impostorAccent
            clampedRemaining <= 5 -> palette.impostorAccent
            clampedRemaining <= 12 -> palette.timerAccent
            else -> palette.cyanAccent
        },
        animationSpec = tween(durationMillis = 360),
        label = "circular_timer_color"
    )
    val timerGlow by animateColorAsState(
        targetValue = when {
            isExpired -> palette.impostorGlow
            clampedRemaining <= 5 -> palette.impostorGlow
            clampedRemaining <= 12 -> palette.directorGlow
            else -> palette.saviorGlow
        },
        animationSpec = tween(durationMillis = 360),
        label = "circular_timer_glow"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "circular_timer_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = when {
            isExpired -> 1.06f
            isUrgent -> 1.05f
            else -> 1.0f
        },
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 420, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "timer_pulse_scale"
    )
    val shockwaveFraction by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "expired_shockwave"
    )

    val mins = clampedRemaining / 60
    val secs = clampedRemaining % 60
    val formattedTime = String.format(Locale.US, "%02d:%02d", mins, secs)
    val trackColor = MaterialTheme.colorScheme.outlineVariant

    Box(
        modifier = modifier
            .size(size)
            .graphicsLayer {
                scaleX = pulseScale
                scaleY = pulseScale
            }
            .testTag("circular_countdown_timer"),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasCenter = Offset(this.size.width / 2f, this.size.height / 2f)
            val strokePx = strokeWidth.toPx()
            val arcRadius = (this.size.minDimension / 2f) - strokePx * 1.35f
            val arcTopLeft = Offset(canvasCenter.x - arcRadius, canvasCenter.y - arcRadius)
            val arcSize = Size(arcRadius * 2f, arcRadius * 2f)

            // Ambient radial glow behind the dial
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        timerGlow.copy(alpha = if (isExpired || isUrgent) 0.30f else 0.16f),
                        timerColor.copy(alpha = 0.06f),
                        Color.Transparent
                    ),
                    center = canvasCenter,
                    radius = arcRadius * 1.35f
                ),
                radius = arcRadius * 1.35f,
                center = canvasCenter
            )

            // Expanding broadcast shockwave rings when expired
            if (isExpired) {
                val ringRadius = arcRadius * (0.95f + shockwaveFraction * 0.48f)
                val ringAlpha = (1f - shockwaveFraction) * 0.65f
                drawCircle(
                    color = timerGlow.copy(alpha = ringAlpha),
                    radius = ringRadius,
                    center = canvasCenter,
                    style = Stroke(width = 2.5.dp.toPx())
                )
                val secondPhase = (shockwaveFraction + 0.5f) % 1f
                drawCircle(
                    color = timerColor.copy(alpha = (1f - secondPhase) * 0.42f),
                    radius = arcRadius * (0.95f + secondPhase * 0.48f),
                    center = canvasCenter,
                    style = Stroke(width = 1.5.dp.toPx())
                )
            }

            // 60 precision radial ticks around the outer perimeter
            val tickCount = 60
            val litTicks = (animatedProgress * tickCount).toInt()
            val tickOuter = arcRadius + strokePx * 0.92f
            val tickInner = arcRadius + strokePx * 0.55f
            for (i in 0 until tickCount) {
                val angleRad = (-90.0 + i * (360.0 / tickCount)) * (PI / 180.0)
                val cosA = cos(angleRad).toFloat()
                val sinA = sin(angleRad).toFloat()
                val isLit = i < litTicks
                drawLine(
                    color = if (isLit) timerColor.copy(alpha = 0.72f) else trackColor.copy(alpha = 0.45f),
                    start = Offset(canvasCenter.x + cosA * tickInner, canvasCenter.y + sinA * tickInner),
                    end = Offset(canvasCenter.x + cosA * tickOuter, canvasCenter.y + sinA * tickOuter),
                    strokeWidth = if (i % 5 == 0) 1.8.dp.toPx() else 1.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }

            // Base circular track
            drawArc(
                color = trackColor.copy(alpha = 0.5f),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = arcTopLeft,
                size = arcSize,
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )

            // Soft glowing outer aura stroke along the active progress arc
            val sweepAngle = (360f * animatedProgress).coerceIn(0f, 360f)
            if (sweepAngle > 0.5f) {
                drawArc(
                    color = timerGlow.copy(alpha = 0.28f),
                    startAngle = -90f,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    topLeft = arcTopLeft,
                    size = arcSize,
                    style = Stroke(width = strokePx * 2.0f, cap = StrokeCap.Round)
                )
                drawArc(
                    brush = Brush.sweepGradient(
                        colors = listOf(timerColor, timerGlow, timerColor),
                        center = canvasCenter
                    ),
                    startAngle = -90f,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    topLeft = arcTopLeft,
                    size = arcSize,
                    style = Stroke(width = strokePx, cap = StrokeCap.Round)
                )

                // Luminous leading tip orb
                val tipRad = (-90f + sweepAngle) * (PI / 180.0)
                val tipOffset = Offset(
                    x = canvasCenter.x + arcRadius * cos(tipRad).toFloat(),
                    y = canvasCenter.y + arcRadius * sin(tipRad).toFloat()
                )
                drawCircle(
                    color = timerGlow.copy(alpha = 0.55f),
                    radius = strokePx * 0.95f,
                    center = tipOffset
                )
                drawCircle(
                    color = Color.White,
                    radius = strokePx * 0.44f,
                    center = tipOffset
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = formattedTime,
                fontFamily = JetBrainsMonoFontFamily,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = timerGlow,
                modifier = Modifier.testTag("turn_timer_clock")
            )
            Text(
                text = when {
                    isExpired -> "EXPIRED"
                    isPaused -> "PAUSED"
                    isUrgent -> "HURRY"
                    else -> "LIVE"
                },
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = timerColor.copy(alpha = 0.92f)
            )
        }
    }
}

@Composable
fun TurnTimerBar(
    activePlayerName: String,
    turnIndex: Int,
    totalTurns: Int,
    remainingSeconds: Int,
    totalSeconds: Int,
    isPaused: Boolean,
    showControls: Boolean,
    onTogglePause: () -> Unit = {},
    onNextTurn: () -> Unit = {},
    onTimeExpired: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val palette = LocalImpostorPalette.current
    val context = LocalContext.current
    val haptics = LocalHapticFeedback.current
    val safeTotal = totalSeconds.coerceAtLeast(1)
    val clampedRemaining = remainingSeconds.coerceIn(0, safeTotal)
    val isExpired = !isPaused && clampedRemaining == 0
    val isUrgent = !isPaused && clampedRemaining in 1..5

    val timerColor by animateColorAsState(
        targetValue = when {
            isExpired -> palette.impostorAccent
            clampedRemaining <= 5 -> palette.impostorAccent
            clampedRemaining <= 12 -> palette.timerAccent
            else -> palette.cyanAccent
        },
        animationSpec = tween(durationMillis = 360),
        label = "timer_bar_color"
    )

    val expiredGlowTransition = rememberInfiniteTransition(label = "expired_banner_pulse")
    val expiredAlpha by expiredGlowTransition.animateFloat(
        initialValue = 0.55f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 550, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "expired_alpha"
    )

    val shape = RoundedCornerShape(22.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .drawBehind {
                val spread = 12.dp.toPx()
                drawRoundRect(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            timerColor.copy(alpha = if (isExpired || isUrgent) 0.28f else 0.14f),
                            Color.Transparent
                        ),
                        radius = maxOf(size.width, size.height) * 0.72f
                    ),
                    cornerRadius = CornerRadius(26.dp.toPx(), 26.dp.toPx()),
                    size = size.copy(width = size.width + spread, height = size.height + spread),
                    topLeft = Offset(-spread / 2f, -spread / 2f)
                )
            }
            .clip(shape)
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.94f),
                        MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.96f)
                    )
                )
            )
            .border(
                width = if (isExpired || isUrgent) 1.6.dp else 1.dp,
                color = timerColor.copy(alpha = if (isExpired) expiredAlpha else if (isUrgent) 0.85f else 0.48f),
                shape = shape
            )
            .testTag("turn_timer_bar")
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                CircularCountdownTimer(
                    remainingSeconds = clampedRemaining,
                    totalSeconds = safeTotal,
                    isPaused = isPaused,
                    size = 98.dp,
                    strokeWidth = 8.dp,
                    onTimeExpired = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        TurnTimerEventBus.emitExpired(
                            context = context,
                            playerName = activePlayerName,
                            turnIndex = turnIndex,
                            totalTurns = totalTurns
                        )
                        onTimeExpired()
                    }
                )

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        PulsingStatusDot(color = timerColor)
                        Text(
                            text = "TURN ${turnIndex + 1} OF ${totalTurns.coerceAtLeast(1)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = timerColor
                        )
                    }

                    Text(
                        text = activePlayerName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    // Turn indicator pills
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        repeat(totalTurns.coerceIn(1, 10)) { idx ->
                            val isCurrent = idx == turnIndex
                            val isDone = idx < turnIndex
                            val pillWidth by animateDpAsState(
                                targetValue = if (isCurrent) 18.dp else 6.dp,
                                animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                                label = "turn_dot_w"
                            )
                            Box(
                                modifier = Modifier
                                    .size(width = pillWidth, height = 5.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when {
                                            isCurrent -> timerColor
                                            isDone -> palette.normalAccent.copy(alpha = 0.72f)
                                            else -> MaterialTheme.colorScheme.outlineVariant
                                        }
                                    )
                            )
                        }
                    }
                }

                if (showControls) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceContainerHigh,
                            shape = CircleShape,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            IconButton(
                                onClick = onTogglePause,
                                modifier = Modifier
                                    .size(48.dp)
                                    .testTag("timer_pause_button")
                            ) {
                                Icon(
                                    imageVector = if (isPaused) Icons.Filled.PlayArrow else Icons.Filled.Pause,
                                    contentDescription = if (isPaused) "Resume timer" else "Pause timer",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                        Surface(
                            color = timerColor.copy(alpha = 0.16f),
                            shape = CircleShape,
                            border = BorderStroke(1.dp, timerColor.copy(alpha = 0.75f))
                        ) {
                            IconButton(
                                onClick = onNextTurn,
                                modifier = Modifier
                                    .size(48.dp)
                                    .testTag("timer_next_turn_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.SkipNext,
                                    contentDescription = "Next player's turn",
                                    tint = timerColor
                                )
                            }
                        }
                    }
                }
            }

            AnimatedVisibility(
                visible = isExpired,
                enter = fadeIn(tween(220)) + expandVertically(spring(stiffness = Spring.StiffnessMediumLow)),
                exit = fadeOut(tween(180)) + shrinkVertically(spring(stiffness = Spring.StiffnessMediumLow))
            ) {
                Column {
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        color = palette.impostorSurface.copy(alpha = 0.92f),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, palette.impostorGlow.copy(alpha = expiredAlpha)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("timer_expired_broadcast_banner")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.NotificationsActive,
                                contentDescription = "Time expired broadcast",
                                tint = palette.impostorGlow,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "TIME EXPIRED • BROADCASTING TURN ALERT",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = palette.impostorGlow,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    }
}
