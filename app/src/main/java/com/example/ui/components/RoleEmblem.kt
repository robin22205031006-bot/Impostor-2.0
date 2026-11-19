package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.data.PlayerRole
import com.example.ui.theme.ImpostorRolePalette
import com.example.ui.theme.LocalImpostorPalette
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

enum class GameRoleIdentity(
    val displayTitle: String,
    val shortLabel: String,
    val emblemDescription: String
) {
    AGENT(
        displayTitle = "AGENT",
        shortLabel = "AGENT",
        emblemDescription = "Agent tactical shield crest emblem"
    ),
    IMPOSTOR(
        displayTitle = "IMPOSTOR",
        shortLabel = "IMPOSTOR",
        emblemDescription = "Impostor phantom visor emblem"
    ),
    AGENT_KILLER(
        displayTitle = "AGENT KILLER",
        shortLabel = "AGENT KILLER",
        emblemDescription = "Agent Killer assassin crosshair emblem"
    ),
    SAVIOR_AGENT(
        displayTitle = "SAVIOR AGENT",
        shortLabel = "SAVIOR",
        emblemDescription = "Savior Agent aegis halo shield emblem"
    ),
    AGENT_DIRECTOR(
        displayTitle = "AGENT DIRECTOR",
        shortLabel = "DIRECTOR",
        emblemDescription = "Agent Director sovereign command emblem"
    );

    companion object {
        fun resolve(
            role: PlayerRole,
            isUltimateKiller: Boolean = false,
            isSavior: Boolean = false,
            isDirector: Boolean = false
        ): GameRoleIdentity = when {
            role == PlayerRole.IMPOSTOR && isUltimateKiller -> AGENT_KILLER
            role == PlayerRole.IMPOSTOR -> IMPOSTOR
            isSavior -> SAVIOR_AGENT
            isDirector -> AGENT_DIRECTOR
            else -> AGENT
        }
    }
}

@Immutable
data class RoleVisualStyle(
    val accent: Color,
    val glow: Color,
    val deepSurface: Color,
    val border: Color
)

fun GameRoleIdentity.style(palette: ImpostorRolePalette): RoleVisualStyle = when (this) {
    GameRoleIdentity.AGENT -> RoleVisualStyle(
        accent = palette.normalAccent,
        glow = palette.normalGlow,
        deepSurface = palette.normalSurface,
        border = palette.normalBorder
    )
    GameRoleIdentity.IMPOSTOR -> RoleVisualStyle(
        accent = palette.impostorAccent,
        glow = palette.impostorGlow,
        deepSurface = palette.impostorSurface,
        border = palette.impostorBorder
    )
    GameRoleIdentity.AGENT_KILLER -> RoleVisualStyle(
        accent = palette.killerAccent,
        glow = palette.killerGlow,
        deepSurface = palette.killerSurface,
        border = palette.killerBorder
    )
    GameRoleIdentity.SAVIOR_AGENT -> RoleVisualStyle(
        accent = palette.saviorAccent,
        glow = palette.saviorGlow,
        deepSurface = palette.saviorSurface,
        border = palette.saviorBorder
    )
    GameRoleIdentity.AGENT_DIRECTOR -> RoleVisualStyle(
        accent = palette.directorAccent,
        glow = palette.directorGlow,
        deepSurface = palette.directorSurface,
        border = palette.directorBorder
    )
}

@Composable
fun RoleEmblemBadge(
    identity: GameRoleIdentity,
    modifier: Modifier = Modifier,
    size: Dp = 56.dp,
    animated: Boolean = true
) {
    val palette = LocalImpostorPalette.current
    val style = identity.style(palette)

    val transition = rememberInfiniteTransition(label = "emblem_anim_${identity.name}")
    val pulse by transition.animateFloat(
        initialValue = 0.72f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "emblem_pulse"
    )
    val orbitDeg by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 9000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "emblem_orbit"
    )

    val effectivePulse = if (animated) pulse else 0.9f
    val effectiveOrbit = if (animated) orbitDeg else 0f

    Box(
        modifier = modifier
            .size(size)
            .semantics { contentDescription = identity.emblemDescription },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val r = this.size.minDimension / 2f
            val center = Offset(this.size.width / 2f, this.size.height / 2f)

            // Outer ambient glow aura
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        style.glow.copy(alpha = 0.34f * effectivePulse),
                        style.accent.copy(alpha = 0.14f * effectivePulse),
                        Color.Transparent
                    ),
                    center = center,
                    radius = r * 1.15f
                ),
                radius = r * 1.15f,
                center = center
            )

            // Inner dark medallion backing
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        style.deepSurface,
                        style.deepSurface.copy(alpha = 0.92f)
                    ),
                    center = center,
                    radius = r * 0.86f
                ),
                radius = r * 0.86f,
                center = center
            )

            // Orbiting segmented halo ring
            rotate(degrees = effectiveOrbit, pivot = center) {
                drawArc(
                    brush = Brush.sweepGradient(
                        colors = listOf(
                            style.glow.copy(alpha = 0.95f),
                            style.accent.copy(alpha = 0.25f),
                            style.glow.copy(alpha = 0.95f),
                            style.accent.copy(alpha = 0.25f)
                        ),
                        center = center
                    ),
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = Offset(center.x - r * 0.88f, center.y - r * 0.88f),
                    size = Size(r * 1.76f, r * 1.76f),
                    style = Stroke(width = (r * 0.085f).coerceAtLeast(1.5.dp.toPx()), cap = StrokeCap.Round)
                )
            }

            // Role-unique vector emblem drawn cleanly in Canvas
            when (identity) {
                GameRoleIdentity.AGENT -> drawAgentCrestEmblem(center, r * 0.56f, style)
                GameRoleIdentity.IMPOSTOR -> drawImpostorVisorEmblem(center, r * 0.56f, style)
                GameRoleIdentity.AGENT_KILLER -> drawKillerReticleEmblem(center, r * 0.60f, style, effectiveOrbit)
                GameRoleIdentity.SAVIOR_AGENT -> drawSaviorAegisEmblem(center, r * 0.58f, style, effectivePulse)
                GameRoleIdentity.AGENT_DIRECTOR -> drawDirectorCrownEmblem(center, r * 0.58f, style, effectivePulse)
            }
        }
    }
}

@Composable
fun RoleEmblemChip(
    identity: GameRoleIdentity,
    modifier: Modifier = Modifier,
    badgeSize: Dp = 24.dp,
    labelOverride: String? = null
) {
    val palette = LocalImpostorPalette.current
    val style = identity.style(palette)
    Surface(
        color = style.deepSurface.copy(alpha = 0.92f),
        shape = RoundedCornerShape(50),
        border = BorderStroke(1.dp, style.glow.copy(alpha = 0.72f)),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(start = 6.dp, end = 10.dp, top = 4.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            RoleEmblemBadge(
                identity = identity,
                size = badgeSize,
                animated = true
            )
            Text(
                text = labelOverride ?: identity.shortLabel,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = style.glow
            )
        }
    }
}

private fun DrawScope.drawAgentCrestEmblem(center: Offset, radius: Float, style: RoleVisualStyle) {
    val shield = Path().apply {
        moveTo(center.x, center.y - radius)
        lineTo(center.x + radius * 0.86f, center.y - radius * 0.55f)
        lineTo(center.x + radius * 0.72f, center.y + radius * 0.34f)
        lineTo(center.x, center.y + radius)
        lineTo(center.x - radius * 0.72f, center.y + radius * 0.34f)
        lineTo(center.x - radius * 0.86f, center.y - radius * 0.55f)
        close()
    }
    drawPath(
        path = shield,
        color = style.accent.copy(alpha = 0.24f)
    )
    drawPath(
        path = shield,
        color = style.glow,
        style = Stroke(width = (radius * 0.14f).coerceAtLeast(1.8f), join = StrokeJoin.Round)
    )
    // Inner tactical chevron
    val chevron = Path().apply {
        moveTo(center.x - radius * 0.42f, center.y + radius * 0.08f)
        lineTo(center.x, center.y - radius * 0.36f)
        lineTo(center.x + radius * 0.42f, center.y + radius * 0.08f)
    }
    drawPath(
        path = chevron,
        color = style.glow,
        style = Stroke(width = (radius * 0.14f).coerceAtLeast(1.8f), cap = StrokeCap.Round, join = StrokeJoin.Round)
    )
    drawCircle(
        color = style.glow,
        radius = radius * 0.14f,
        center = Offset(center.x, center.y + radius * 0.32f)
    )
}

private fun DrawScope.drawImpostorVisorEmblem(center: Offset, radius: Float, style: RoleVisualStyle) {
    // Hooded angular stealth mask
    val mask = Path().apply {
        moveTo(center.x, center.y - radius)
        lineTo(center.x + radius * 0.92f, center.y - radius * 0.18f)
        lineTo(center.x + radius * 0.52f, center.y + radius * 0.92f)
        lineTo(center.x, center.y + radius * 0.65f)
        lineTo(center.x - radius * 0.52f, center.y + radius * 0.92f)
        lineTo(center.x - radius * 0.92f, center.y - radius * 0.18f)
        close()
    }
    drawPath(path = mask, color = style.accent.copy(alpha = 0.24f))
    drawPath(
        path = mask,
        color = style.glow,
        style = Stroke(width = (radius * 0.13f).coerceAtLeast(1.8f), join = StrokeJoin.Round)
    )
    // Slanted glowing stealth eyes
    val leftEye = Path().apply {
        moveTo(center.x - radius * 0.54f, center.y - radius * 0.18f)
        lineTo(center.x - radius * 0.14f, center.y + radius * 0.04f)
        lineTo(center.x - radius * 0.46f, center.y + radius * 0.14f)
        close()
    }
    val rightEye = Path().apply {
        moveTo(center.x + radius * 0.54f, center.y - radius * 0.18f)
        lineTo(center.x + radius * 0.14f, center.y + radius * 0.04f)
        lineTo(center.x + radius * 0.46f, center.y + radius * 0.14f)
        close()
    }
    drawPath(leftEye, color = style.glow)
    drawPath(rightEye, color = style.glow)
}

private fun DrawScope.drawKillerReticleEmblem(
    center: Offset,
    radius: Float,
    style: RoleVisualStyle,
    orbitDeg: Float
) {
    val strokeW = (radius * 0.12f).coerceAtLeast(1.8f)
    // Outer sniper target ring
    drawCircle(
        color = style.glow,
        radius = radius * 0.82f,
        center = center,
        style = Stroke(width = strokeW)
    )
    // Counter-rotating X blades behind center
    rotate(degrees = -orbitDeg * 0.5f, pivot = center) {
        val diag = radius * 0.68f
        drawLine(
            color = style.accent,
            start = Offset(center.x - diag, center.y - diag),
            end = Offset(center.x + diag, center.y + diag),
            strokeWidth = strokeW * 0.9f,
            cap = StrokeCap.Round
        )
        drawLine(
            color = style.accent,
            start = Offset(center.x + diag, center.y - diag),
            end = Offset(center.x - diag, center.y + diag),
            strokeWidth = strokeW * 0.9f,
            cap = StrokeCap.Round
        )
    }
    // Cardinal crosshair ticks
    val tickOuter = radius * 1.06f
    val tickInner = radius * 0.44f
    listOf(0f, 90f, 180f, 270f).forEach { angleDeg ->
        val rad = angleDeg * (PI / 180.0)
        val dx = cos(rad).toFloat()
        val dy = sin(rad).toFloat()
        drawLine(
            color = style.glow,
            start = Offset(center.x + dx * tickInner, center.y + dy * tickInner),
            end = Offset(center.x + dx * tickOuter, center.y + dy * tickOuter),
            strokeWidth = strokeW,
            cap = StrokeCap.Round
        )
    }
    // Lethal diamond core
    val core = Path().apply {
        moveTo(center.x, center.y - radius * 0.30f)
        lineTo(center.x + radius * 0.30f, center.y)
        lineTo(center.x, center.y + radius * 0.30f)
        lineTo(center.x - radius * 0.30f, center.y)
        close()
    }
    drawPath(core, color = style.glow)
}

private fun DrawScope.drawSaviorAegisEmblem(
    center: Offset,
    radius: Float,
    style: RoleVisualStyle,
    pulse: Float
) {
    val strokeW = (radius * 0.12f).coerceAtLeast(1.8f)
    // Hexagonal Aegis barrier
    val hex = Path().apply {
        for (i in 0 until 6) {
            val angle = (i * 60.0 - 90.0) * (PI / 180.0)
            val px = center.x + radius * 0.94f * cos(angle).toFloat()
            val py = center.y + radius * 0.94f * sin(angle).toFloat()
            if (i == 0) moveTo(px, py) else lineTo(px, py)
        }
        close()
    }
    drawPath(hex, color = style.accent.copy(alpha = 0.22f * pulse))
    drawPath(hex, color = style.glow, style = Stroke(width = strokeW, join = StrokeJoin.Round))

    // Glowing Savior Cross + Halo Ring
    drawCircle(
        color = style.glow.copy(alpha = 0.55f),
        radius = radius * 0.54f,
        center = center,
        style = Stroke(width = strokeW * 0.7f)
    )
    val barHalf = radius * 0.42f
    drawLine(
        color = style.glow,
        start = Offset(center.x, center.y - barHalf),
        end = Offset(center.x, center.y + barHalf),
        strokeWidth = strokeW * 1.25f,
        cap = StrokeCap.Round
    )
    drawLine(
        color = style.glow,
        start = Offset(center.x - barHalf, center.y),
        end = Offset(center.x + barHalf, center.y),
        strokeWidth = strokeW * 1.25f,
        cap = StrokeCap.Round
    )
}

private fun DrawScope.drawDirectorCrownEmblem(
    center: Offset,
    radius: Float,
    style: RoleVisualStyle,
    pulse: Float
) {
    val strokeW = (radius * 0.12f).coerceAtLeast(1.8f)
    // Sovereign 3-peak crown crest
    val crown = Path().apply {
        moveTo(center.x - radius * 0.85f, center.y + radius * 0.48f)
        lineTo(center.x - radius * 0.92f, center.y - radius * 0.34f)
        lineTo(center.x - radius * 0.42f, center.y + radius * 0.04f)
        lineTo(center.x, center.y - radius * 0.78f)
        lineTo(center.x + radius * 0.42f, center.y + radius * 0.04f)
        lineTo(center.x + radius * 0.92f, center.y - radius * 0.34f)
        lineTo(center.x + radius * 0.85f, center.y + radius * 0.48f)
        close()
    }
    drawPath(crown, color = style.accent.copy(alpha = 0.26f * pulse))
    drawPath(crown, color = style.glow, style = Stroke(width = strokeW, join = StrokeJoin.Round))

    // Sovereign command bar underneath
    drawLine(
        color = style.glow,
        start = Offset(center.x - radius * 0.68f, center.y + radius * 0.76f),
        end = Offset(center.x + radius * 0.68f, center.y + radius * 0.76f),
        strokeWidth = strokeW,
        cap = StrokeCap.Round
    )
    // Crown peak jewels
    drawCircle(color = style.glow, radius = radius * 0.12f, center = Offset(center.x, center.y - radius * 0.78f))
    drawCircle(color = style.glow, radius = radius * 0.09f, center = Offset(center.x - radius * 0.90f, center.y - radius * 0.34f))
    drawCircle(color = style.glow, radius = radius * 0.09f, center = Offset(center.x + radius * 0.90f, center.y - radius * 0.34f))
}
