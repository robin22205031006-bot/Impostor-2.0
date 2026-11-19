package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.data.AvatarCharacter
import com.example.data.AvatarGraphicMotif
import com.example.data.AvatarPose
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

private val HexCrestShape = GenericShape { size, _ ->
    val cx = size.width / 2f
    val cy = size.height / 2f
    val r = minOf(size.width, size.height) / 2f
    for (i in 0 until 6) {
        val angle = (i * 60.0 - 90.0) * (PI / 180.0)
        val x = cx + r * cos(angle).toFloat()
        val y = cy + r * sin(angle).toFloat()
        if (i == 0) moveTo(x, y) else lineTo(x, y)
    }
    close()
}

@Composable
fun CharacterAvatarThumbnail(
    character: AvatarCharacter,
    pose: AvatarPose = AvatarPose.ORIGINAL,
    size: Dp = 46.dp,
    borderColor: Color = character.glowColor,
    isHighlighted: Boolean = false,
    modifier: Modifier = Modifier
) {
    val shape = if (pose == AvatarPose.CREST_AURA) HexCrestShape else CircleShape
    val zoomScale = character.bustZoom
    val biasX = character.bustBiasX.coerceIn(-1f, 1f)
    val biasY = character.bustBiasY.coerceIn(-1f, 1f)

    Box(
        modifier = modifier
            .size(size)
            .drawBehind {
                if (isHighlighted) {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(borderColor.copy(alpha = 0.45f), Color.Transparent),
                            radius = this.size.minDimension * 0.76f
                        ),
                        radius = this.size.minDimension * 0.76f
                    )
                }
            }
            .clip(shape)
            .background(
                Brush.radialGradient(
                    colors = listOf(character.accentColor.copy(alpha = 0.30f), character.surfaceTint)
                )
            )
            .border(
                width = if (isHighlighted) 2.dp else 1.5.dp,
                brush = Brush.linearGradient(listOf(borderColor, character.accentColor)),
                shape = shape
            ),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = character.drawableFor(pose)),
            contentDescription = "${character.name} (${pose.label})",
            contentScale = ContentScale.Crop,
            alignment = BiasAlignment(biasX, biasY),
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = zoomScale
                    scaleY = zoomScale
                }
        )

        if (pose == AvatarPose.TACTICAL_BUST) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val stroke = 1.2.dp.toPx()
                val len = this.size.minDimension * 0.22f
                drawLine(borderColor.copy(alpha = 0.72f), Offset(2f, 2f), Offset(len, 2f), stroke)
                drawLine(borderColor.copy(alpha = 0.72f), Offset(2f, 2f), Offset(2f, len), stroke)
                drawLine(
                    borderColor.copy(alpha = 0.72f),
                    Offset(this.size.width - len, this.size.height - 2f),
                    Offset(this.size.width - 2f, this.size.height - 2f),
                    stroke
                )
            }
        }
    }
}

@Composable
fun CharacterAvatarCard(
    character: AvatarCharacter,
    pose: AvatarPose,
    isSelected: Boolean = false,
    compact: Boolean = false,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "avatar_card_fx_${character.id}")
    val pulse by transition.animateFloat(
        initialValue = 0.58f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "card_pulse"
    )
    val orbitDeg by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 10000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "card_orbit"
    )
    val idlePhase by transition.animateFloat(
        initialValue = 0f,
        targetValue = (2.0 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "card_idle_phase"
    )

    val idleNorm = (sin(idlePhase) + 1f) * 0.5f // 0..1 smooth breath wave
    val selectScale by animateFloatAsState(
        targetValue = if (isSelected) 1.02f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "card_select_scale"
    )

    val cardShape = RoundedCornerShape(if (compact) 18.dp else 24.dp)
    val cardContent: @Composable () -> Unit = {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(if (compact) 0.72f else 0.76f)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            character.surfaceTint,
                            MaterialTheme.colorScheme.surfaceContainerLow,
                            character.surfaceTint.copy(alpha = 0.96f)
                        )
                    )
                )
        ) {
            // Unified Character Motif & Atmospheric Stage Behind Sprite
            AvatarMotifBackdrop(
                character = character,
                pose = pose,
                pulse = pulse,
                orbitDeg = orbitDeg,
                modifier = Modifier.fillMaxSize()
            )

            // Unified Anime Full-Body Cutout Sprite + Synchronized Pose VFX & Idle Animation
            AnimatedContent(
                targetState = pose,
                transitionSpec = {
                    (fadeIn(tween(220)) + scaleIn(
                        initialScale = 0.88f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMediumLow
                        )
                    )).togetherWith(
                        fadeOut(tween(140)) + scaleOut(targetScale = 1.04f, animationSpec = tween(140))
                    )
                },
                label = "pose_swap_${character.id}",
                modifier = Modifier.fillMaxSize()
            ) { currentPose ->
                AvatarPoseArtwork(
                    character = character,
                    pose = currentPose,
                    pulse = pulse,
                    orbitDeg = orbitDeg,
                    idleWave = sin(idlePhase),
                    idleNorm = idleNorm,
                    compact = compact
                )
            }

            // Bottom gradient scrim for legible character nameplate
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(if (compact) 64.dp else 84.dp)
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color(0xCC090B11),
                                Color(0xF5090B11)
                            )
                        )
                    )
            )

            // Top Corner Badges: Code & Active Pose Tag
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(if (compact) 8.dp else 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = Color(0xCC0B0D14),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, character.glowColor.copy(alpha = 0.55f))
                ) {
                    Text(
                        text = character.code,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = character.glowColor,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                    )
                }

                val isCustomActionBadge = pose != AvatarPose.ORIGINAL
                Surface(
                    color = if (isCustomActionBadge) {
                        character.accentColor.copy(alpha = 0.90f)
                    } else {
                        Color(0xCC0B0D14)
                    },
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, character.glowColor.copy(alpha = 0.72f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AutoAwesome,
                            contentDescription = "Custom anime pose",
                            tint = if (isCustomActionBadge) Color.White else character.glowColor,
                            modifier = Modifier.size(11.dp)
                        )
                        Text(
                            text = pose.badgeCode,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isCustomActionBadge) Color.White else character.glowColor
                        )
                    }
                }
            }

            // Bottom Character Nameplate & Graphic Motif Tag
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .padding(horizontal = if (compact) 10.dp else 14.dp, vertical = if (compact) 8.dp else 12.dp)
            ) {
                Text(
                    text = character.name,
                    style = if (compact) MaterialTheme.typography.titleMedium else MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = if (compact) character.title else "${character.title} • ${character.motif.displayName}",
                    style = MaterialTheme.typography.labelSmall,
                    color = character.glowColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (!compact) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${character.archetype} • ${pose.label}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFB6C2DC),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }

    val containerModifier = modifier
        .graphicsLayer {
            scaleX = selectScale
            scaleY = selectScale
        }
        .drawBehind {
            if (isSelected) {
                val spread = 10.dp.toPx()
                drawRoundRect(
                    brush = Brush.radialGradient(
                        colors = listOf(character.glowColor.copy(alpha = 0.42f * pulse), Color.Transparent),
                        radius = maxOf(size.width, size.height) * 0.72f
                    ),
                    cornerRadius = CornerRadius(26.dp.toPx(), 26.dp.toPx()),
                    size = size.copy(width = size.width + spread, height = size.height + spread),
                    topLeft = Offset(-spread / 2f, -spread / 2f)
                )
            }
        }

    if (onClick != null) {
        Surface(
            onClick = onClick,
            shape = cardShape,
            border = BorderStroke(
                width = if (isSelected) 2.2.dp else 1.2.dp,
                brush = Brush.linearGradient(
                    colors = if (isSelected) {
                        listOf(character.glowColor, character.accentColor, character.glowColor)
                    } else {
                        listOf(character.glowColor.copy(alpha = 0.45f), character.accentColor.copy(alpha = 0.25f))
                    }
                )
            ),
            color = character.surfaceTint,
            modifier = containerModifier
        ) {
            cardContent()
        }
    } else {
        Surface(
            shape = cardShape,
            border = BorderStroke(
                width = 2.dp,
                brush = Brush.linearGradient(
                    colors = listOf(character.glowColor, character.accentColor, character.glowColor)
                )
            ),
            color = character.surfaceTint,
            modifier = containerModifier
        ) {
            cardContent()
        }
    }
}

@Composable
private fun AvatarPoseArtwork(
    character: AvatarCharacter,
    pose: AvatarPose,
    pulse: Float,
    orbitDeg: Float,
    idleWave: Float,
    idleNorm: Float,
    compact: Boolean
) {
    val topStagePad = if (compact) 26.dp else 32.dp
    val bottomStagePad = if (compact) 38.dp else 48.dp
    val bobAmplitudeDp = if (compact) 2.0f else 3.2f
    val breathScale = 1.0f + idleNorm * 0.016f + if (pose == AvatarPose.ACTION_STANCE) 0.025f else 0f
    val shadowScale = 1.0f - idleNorm * 0.14f

    Box(modifier = Modifier.fillMaxSize()) {
        // 1. Unified Anime VFX & Pedestal Shadow Layer Behind Character Cutout
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val heroCenter = Offset(w * 0.5f, h * 0.44f)
            val pedestalCenter = Offset(w * 0.5f, h - bottomStagePad.toPx() + 2.dp.toPx())
            val pedestalW = w * 0.54f * shadowScale
            val pedestalH = (if (compact) 11.dp else 15.dp).toPx() * shadowScale

            // Ground aura & cast shadow beneath feet
            drawOval(
                brush = Brush.radialGradient(
                    colors = listOf(
                        character.glowColor.copy(alpha = 0.34f * pulse),
                        character.accentColor.copy(alpha = 0.14f),
                        Color.Transparent
                    ),
                    center = pedestalCenter,
                    radius = pedestalW * 0.68f
                ),
                topLeft = Offset(pedestalCenter.x - pedestalW * 0.62f, pedestalCenter.y - pedestalH * 0.72f),
                size = Size(pedestalW * 1.24f, pedestalH * 1.44f)
            )
            drawOval(
                color = Color(0xAA04060A),
                topLeft = Offset(pedestalCenter.x - pedestalW * 0.42f, pedestalCenter.y - pedestalH * 0.40f),
                size = Size(pedestalW * 0.84f, pedestalH * 0.80f)
            )
            drawOval(
                color = character.glowColor.copy(alpha = 0.42f * pulse),
                topLeft = Offset(pedestalCenter.x - pedestalW * 0.52f, pedestalCenter.y - pedestalH * 0.50f),
                size = Size(pedestalW * 1.04f, pedestalH),
                style = Stroke(width = 1.2.dp.toPx())
            )

            // Backlit anime rim halo behind hero silhouette
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        character.glowColor.copy(alpha = 0.24f * pulse),
                        character.accentColor.copy(alpha = 0.08f),
                        Color.Transparent
                    ),
                    center = heroCenter,
                    radius = minOf(w, h) * 0.46f
                ),
                radius = minOf(w, h) * 0.46f,
                center = heroCenter
            )

            // Pose-specific unified anime graphics behind the sprite
            when (pose) {
                AvatarPose.ORIGINAL -> {
                    drawOriginalHeroGlints(w, h, heroCenter, character, pulse, orbitDeg)
                }
                AvatarPose.ACTION_STANCE -> {
                    drawCombatSlashFx(w, h, character, pulse)
                }
                AvatarPose.TACTICAL_BUST -> {
                    drawSkillArcaneRingFx(w, h, heroCenter, character, pulse, orbitDeg)
                }
                AvatarPose.CREST_AURA -> {
                    drawStanceHexCrestFx(heroCenter, minOf(w, h) * 0.36f, character, pulse, orbitDeg)
                }
            }
        }

        // 2. Background-Free Transparent Full-Body Character Sprite
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = topStagePad, bottom = bottomStagePad, start = 6.dp, end = 6.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Image(
                painter = painterResource(id = character.drawableFor(pose)),
                contentDescription = "${character.name} (${pose.label})",
                contentScale = ContentScale.Fit,
                alignment = Alignment.BottomCenter,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        translationY = idleWave * bobAmplitudeDp.dp.toPx()
                        scaleX = breathScale
                        scaleY = breathScale
                    }
            )
        }

        // 3. Subtle Foreground Anime Spark / Slash Highlight Layer
        if (pose == AvatarPose.ACTION_STANCE || pose == AvatarPose.TACTICAL_BUST) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                if (pose == AvatarPose.ACTION_STANCE) {
                    val slashHighlight = Path().apply {
                        moveTo(w * 0.04f, h * 0.66f)
                        quadraticTo(w * 0.52f, h * 0.40f, w * 0.96f, h * 0.22f)
                    }
                    drawPath(
                        path = slashHighlight,
                        brush = Brush.linearGradient(
                            listOf(
                                Color.Transparent,
                                character.glowColor.copy(alpha = 0.42f * pulse),
                                Color.White.copy(alpha = 0.28f * pulse),
                                Color.Transparent
                            )
                        ),
                        style = Stroke(width = 2.2.dp.toPx(), cap = StrokeCap.Round)
                    )
                } else {
                    // Ascending skill motes
                    val phase = orbitDeg / 360f
                    listOf(0.18f to 0.72f, 0.82f to 0.62f, 0.28f to 0.36f, 0.76f to 0.30f).forEachIndexed { idx, (px, py) ->
                        val curY = ((py - phase * 0.45f + 1f) % 0.72f + 0.12f) * h
                        drawCircle(
                            color = character.glowColor.copy(alpha = (0.55f - idx * 0.08f) * pulse),
                            radius = (2.2f + (idx % 2)).dp.toPx(),
                            center = Offset(px * w, curY)
                        )
                    }
                }
            }
        }
    }
}

private fun DrawScope.drawOriginalHeroGlints(
    w: Float,
    h: Float,
    center: Offset,
    ch: AvatarCharacter,
    pulse: Float,
    orbitDeg: Float
) {
    val glintPts = listOf(0.16f to 0.24f, 0.84f to 0.30f, 0.20f to 0.62f, 0.82f to 0.66f)
    glintPts.forEachIndexed { idx, (px, py) ->
        val r = (2.2f + (idx % 2) * 1.2f).dp.toPx() * (0.7f + 0.3f * pulse)
        val cx = px * w
        val cy = py * h + sin(((orbitDeg + idx * 90f) * PI / 180.0)).toFloat() * 3.dp.toPx()
        drawCircle(ch.glowColor.copy(alpha = 0.36f * pulse), radius = r, center = Offset(cx, cy))
    }
    drawCircle(
        color = ch.glowColor.copy(alpha = 0.16f * pulse),
        radius = minOf(w, h) * 0.34f,
        center = center,
        style = Stroke(width = 1.2.dp.toPx())
    )
}

private fun DrawScope.drawCombatSlashFx(w: Float, h: Float, ch: AvatarCharacter, pulse: Float) {
    val slashPath = Path().apply {
        moveTo(-w * 0.08f, h * 0.72f)
        quadraticTo(w * 0.48f, h * 0.38f, w * 1.08f, h * 0.12f)
    }
    drawPath(
        path = slashPath,
        brush = Brush.linearGradient(
            listOf(
                Color.Transparent,
                ch.accentColor.copy(alpha = 0.45f * pulse),
                ch.glowColor.copy(alpha = 0.72f * pulse),
                Color.Transparent
            )
        ),
        style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round)
    )
    for (i in 0..4) {
        val y = h * (0.18f + i * 0.13f)
        drawLine(
            color = ch.glowColor.copy(alpha = 0.24f * pulse),
            start = Offset(w * 0.05f, y),
            end = Offset(w * (0.22f + (i % 2) * 0.09f), y - h * 0.05f),
            strokeWidth = 1.6.dp.toPx(),
            cap = StrokeCap.Round
        )
    }
}

private fun DrawScope.drawSkillArcaneRingFx(
    w: Float,
    h: Float,
    center: Offset,
    ch: AvatarCharacter,
    pulse: Float,
    orbitDeg: Float
) {
    val outerR = minOf(w, h) * 0.36f
    val innerR = minOf(w, h) * 0.27f
    rotate(degrees = orbitDeg * 0.65f, pivot = center) {
        drawCircle(
            brush = Brush.sweepGradient(
                listOf(ch.glowColor.copy(alpha = 0.55f * pulse), Color.Transparent, ch.accentColor.copy(alpha = 0.45f), Color.Transparent),
                center = center
            ),
            radius = outerR,
            center = center,
            style = Stroke(width = 2.2.dp.toPx(), cap = StrokeCap.Round)
        )
        for (i in 0 until 6) {
            val a = i * 60.0 * (PI / 180.0)
            drawCircle(
                color = ch.glowColor.copy(alpha = 0.72f * pulse),
                radius = 2.6.dp.toPx(),
                center = Offset(center.x + outerR * cos(a).toFloat(), center.y + outerR * sin(a).toFloat())
            )
        }
    }
    rotate(degrees = -orbitDeg * 0.9f, pivot = center) {
        drawCircle(
            color = ch.accentColor.copy(alpha = 0.32f * pulse),
            radius = innerR,
            center = center,
            style = Stroke(width = 1.4.dp.toPx())
        )
    }
}

private fun DrawScope.drawStanceHexCrestFx(
    center: Offset,
    radius: Float,
    ch: AvatarCharacter,
    pulse: Float,
    orbitDeg: Float
) {
    val hexPath = Path().apply {
        for (i in 0 until 6) {
            val a = (i * 60.0 - 90.0) * (PI / 180.0)
            val x = center.x + radius * cos(a).toFloat()
            val y = center.y + radius * sin(a).toFloat()
            if (i == 0) moveTo(x, y) else lineTo(x, y)
        }
        close()
    }
    rotate(degrees = orbitDeg * 0.35f, pivot = center) {
        drawPath(
            path = hexPath,
            color = ch.glowColor.copy(alpha = 0.45f * pulse),
            style = Stroke(width = 2.dp.toPx())
        )
    }
    rotate(degrees = -orbitDeg * 0.7f, pivot = center) {
        drawArc(
            brush = Brush.sweepGradient(
                colors = listOf(ch.glowColor, Color.Transparent, ch.accentColor, Color.Transparent),
                center = center
            ),
            startAngle = 0f,
            sweepAngle = 360f,
            useCenter = false,
            topLeft = Offset(center.x - radius * 1.15f, center.y - radius * 1.15f),
            size = Size(radius * 2.30f, radius * 2.30f),
            style = Stroke(width = 2.2.dp.toPx(), cap = StrokeCap.Round)
        )
    }
}

@Composable
fun AvatarMotifBackdrop(
    character: AvatarCharacter,
    pose: AvatarPose,
    pulse: Float,
    orbitDeg: Float,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val center = Offset(w * 0.5f, h * 0.45f)

        // Core character aura gradient
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    character.glowColor.copy(alpha = 0.24f * pulse),
                    character.accentColor.copy(alpha = 0.10f),
                    Color.Transparent
                ),
                center = center,
                radius = maxOf(w, h) * 0.58f
            ),
            radius = maxOf(w, h) * 0.58f,
            center = center
        )

        when (character.motif) {
            AvatarGraphicMotif.INK_WASH -> drawInkWashGraphics(w, h, character, pulse)
            AvatarGraphicMotif.EMERALD_RUNES -> drawSigilRuneGraphics(center, minOf(w, h) * 0.38f, character, orbitDeg, pulse)
            AvatarGraphicMotif.CRIMSON_EMBERS -> drawEmberGraphics(w, h, character, pulse, orbitDeg)
            AvatarGraphicMotif.CYBER_HUD -> drawCyberGridGraphics(w, h, character, pulse)
            AvatarGraphicMotif.LUNAR_RIPPLES, AvatarGraphicMotif.ARCANE_TIDE -> drawRippleTideGraphics(center, w, h, character, pulse)
            AvatarGraphicMotif.SOLAR_RAYS -> drawSolarRayGraphics(center, minOf(w, h) * 0.55f, character, orbitDeg, pulse)
            AvatarGraphicMotif.BLADE_SLASH -> drawBladeArcGraphics(w, h, character, pulse)
            AvatarGraphicMotif.FEATHER_PLUME, AvatarGraphicMotif.FOREST_WIND -> drawWindPlumeGraphics(w, h, character, pulse, orbitDeg)
        }

        if (pose != AvatarPose.ORIGINAL) {
            val tick = 10.dp.toPx()
            val inset = 8.dp.toPx()
            drawLine(
                color = character.glowColor.copy(alpha = 0.38f),
                start = Offset(inset, h - inset - tick),
                end = Offset(inset + tick, h - inset),
                strokeWidth = 1.5.dp.toPx()
            )
            drawLine(
                color = character.glowColor.copy(alpha = 0.38f),
                start = Offset(w - inset - tick, h - inset),
                end = Offset(w - inset, h - inset - tick),
                strokeWidth = 1.5.dp.toPx()
            )
        }
    }
}

private fun DrawScope.drawInkWashGraphics(w: Float, h: Float, ch: AvatarCharacter, pulse: Float) {
    val arc = Path().apply {
        moveTo(w * 0.08f, h * 0.30f)
        cubicTo(w * 0.30f, h * 0.12f, w * 0.72f, h * 0.24f, w * 0.92f, h * 0.56f)
    }
    drawPath(
        path = arc,
        color = ch.glowColor.copy(alpha = 0.22f * pulse),
        style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
    )
    drawCircle(ch.glowColor.copy(alpha = 0.28f), radius = 4.dp.toPx(), center = Offset(w * 0.16f, h * 0.24f))
    drawCircle(ch.accentColor.copy(alpha = 0.32f), radius = 3.dp.toPx(), center = Offset(w * 0.84f, h * 0.62f))
}

private fun DrawScope.drawSigilRuneGraphics(
    center: Offset,
    radius: Float,
    ch: AvatarCharacter,
    orbitDeg: Float,
    pulse: Float
) {
    rotate(degrees = orbitDeg * 0.4f, pivot = center) {
        drawCircle(
            color = ch.glowColor.copy(alpha = 0.22f * pulse),
            radius = radius,
            center = center,
            style = Stroke(width = 1.5.dp.toPx())
        )
        for (i in 0 until 4) {
            val angle = i * 90.0 * (PI / 180.0)
            val px = center.x + radius * cos(angle).toFloat()
            val py = center.y + radius * sin(angle).toFloat()
            drawCircle(color = ch.glowColor.copy(alpha = 0.50f), radius = 2.8.dp.toPx(), center = Offset(px, py))
        }
    }
}

private fun DrawScope.drawEmberGraphics(w: Float, h: Float, ch: AvatarCharacter, pulse: Float, orbitDeg: Float) {
    val shift = (orbitDeg / 360f)
    val points = listOf(
        0.15f to 0.70f, 0.28f to 0.35f, 0.75f to 0.65f, 0.86f to 0.28f, 0.42f to 0.18f, 0.62f to 0.78f
    )
    points.forEachIndexed { idx, (px, py) ->
        val y = ((py - shift * 0.25f + 1f) % 1f) * h
        drawCircle(
            color = if (idx % 2 == 0) ch.glowColor.copy(alpha = 0.55f * pulse) else ch.accentColor.copy(alpha = 0.45f),
            radius = (2.2f + (idx % 3)).dp.toPx(),
            center = Offset(px * w, y)
        )
    }
}

private fun DrawScope.drawCyberGridGraphics(w: Float, h: Float, ch: AvatarCharacter, pulse: Float) {
    val step = 28.dp.toPx()
    var x = step
    while (x < w) {
        drawLine(
            color = ch.glowColor.copy(alpha = 0.08f * pulse),
            start = Offset(x, 0f),
            end = Offset(x, h),
            strokeWidth = 1.dp.toPx()
        )
        x += step
    }
    var y = step
    while (y < h) {
        drawLine(
            color = ch.glowColor.copy(alpha = 0.08f * pulse),
            start = Offset(0f, y),
            end = Offset(w, y),
            strokeWidth = 1.dp.toPx()
        )
        y += step
    }
}

private fun DrawScope.drawRippleTideGraphics(center: Offset, w: Float, h: Float, ch: AvatarCharacter, pulse: Float) {
    listOf(0.26f, 0.38f, 0.50f).forEachIndexed { idx, factor ->
        drawCircle(
            color = ch.glowColor.copy(alpha = (0.24f - idx * 0.06f) * pulse),
            radius = minOf(w, h) * factor,
            center = center,
            style = Stroke(width = 1.5.dp.toPx())
        )
    }
}

private fun DrawScope.drawSolarRayGraphics(
    center: Offset,
    radius: Float,
    ch: AvatarCharacter,
    orbitDeg: Float,
    pulse: Float
) {
    rotate(degrees = orbitDeg * 0.25f, pivot = center) {
        for (i in 0 until 12) {
            val angle = i * 30.0 * (PI / 180.0)
            val dx = cos(angle).toFloat()
            val dy = sin(angle).toFloat()
            drawLine(
                color = ch.glowColor.copy(alpha = 0.16f * pulse),
                start = Offset(center.x + dx * radius * 0.22f, center.y + dy * radius * 0.22f),
                end = Offset(center.x + dx * radius, center.y + dy * radius),
                strokeWidth = 2.dp.toPx(),
                cap = StrokeCap.Round
            )
        }
    }
}

private fun DrawScope.drawBladeArcGraphics(w: Float, h: Float, ch: AvatarCharacter, pulse: Float) {
    drawLine(
        brush = Brush.linearGradient(
            listOf(Color.Transparent, ch.glowColor.copy(alpha = 0.28f * pulse), Color.Transparent)
        ),
        start = Offset(w * 0.05f, h * 0.80f),
        end = Offset(w * 0.95f, h * 0.16f),
        strokeWidth = 2.5.dp.toPx(),
        cap = StrokeCap.Round
    )
    drawLine(
        brush = Brush.linearGradient(
            listOf(Color.Transparent, ch.accentColor.copy(alpha = 0.22f * pulse), Color.Transparent)
        ),
        start = Offset(w * 0.12f, h * 0.88f),
        end = Offset(w * 0.96f, h * 0.30f),
        strokeWidth = 1.8.dp.toPx(),
        cap = StrokeCap.Round
    )
}

private fun DrawScope.drawWindPlumeGraphics(w: Float, h: Float, ch: AvatarCharacter, pulse: Float, orbitDeg: Float) {
    val sway = sin((orbitDeg * PI / 180.0)).toFloat() * 12f
    val path = Path().apply {
        moveTo(0f, h * 0.60f)
        quadraticTo(w * 0.45f, h * 0.35f + sway, w, h * 0.28f)
    }
    drawPath(
        path = path,
        color = ch.glowColor.copy(alpha = 0.24f * pulse),
        style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
    )
    drawCircle(ch.glowColor.copy(alpha = 0.45f * pulse), radius = 2.8.dp.toPx(), center = Offset(w * 0.22f, h * 0.45f))
    drawCircle(ch.glowColor.copy(alpha = 0.38f * pulse), radius = 2.2.dp.toPx(), center = Offset(w * 0.78f, h * 0.32f))
}
