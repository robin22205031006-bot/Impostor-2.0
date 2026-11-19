package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.AvatarCatalog
import com.example.data.AvatarPose
import com.example.data.PlayerRole
import com.example.ui.theme.LocalImpostorPalette

@Composable
fun RoleWordCard(
    role: PlayerRole,
    category: String,
    genre: String,
    secretWord: String,
    hint: String,
    showCategoryAndGenre: Boolean = true,
    showHint: Boolean = true,
    roleIdentity: GameRoleIdentity = if (role == PlayerRole.IMPOSTOR) {
        GameRoleIdentity.IMPOSTOR
    } else {
        GameRoleIdentity.AGENT
    },
    avatarId: String? = null,
    avatarPose: AvatarPose = AvatarPose.ORIGINAL,
    modifier: Modifier = Modifier
) {
    val palette = LocalImpostorPalette.current
    val isImpostor = role == PlayerRole.IMPOSTOR
    val hasCategory = showCategoryAndGenre && (category.isNotBlank() || genre.isNotBlank())
    val visibleSecretWord = if (isImpostor) "" else secretWord
    val visibleAgentHint = if (isImpostor) "" else hint
    val hasAgentHint = !isImpostor && showHint && visibleAgentHint.isNotBlank()

    val targetStyle = roleIdentity.style(palette)
    val accent by animateColorAsState(
        targetValue = targetStyle.accent,
        animationSpec = tween(durationMillis = 420),
        label = "role_card_accent"
    )
    val glow by animateColorAsState(
        targetValue = targetStyle.glow,
        animationSpec = tween(durationMillis = 420),
        label = "role_card_glow"
    )
    val surfaceBg by animateColorAsState(
        targetValue = targetStyle.deepSurface,
        animationSpec = tween(durationMillis = 420),
        label = "role_card_surface"
    )
    val borderColor by animateColorAsState(
        targetValue = targetStyle.border,
        animationSpec = tween(durationMillis = 420),
        label = "role_card_border"
    )

    val pulseTransition = rememberInfiniteTransition(label = "role_glow")
    val glowAlpha by pulseTransition.animateFloat(
        initialValue = 0.28f,
        targetValue = 0.72f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )
    val glowSpreadDp by pulseTransition.animateFloat(
        initialValue = 12f,
        targetValue = 28f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_spread"
    )
    val borderPulseAlpha by pulseTransition.animateFloat(
        initialValue = 0.62f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "border_pulse_alpha"
    )
    val cardScale by pulseTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.014f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "card_scale"
    )
    val sheenOffset by pulseTransition.animateFloat(
        initialValue = -0.35f,
        targetValue = 1.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sheen_offset"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag(if (isImpostor) "role_card_impostor" else "role_card_normal"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Main Role-Themed Glowing Word Box with unique Emblem per Role
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer {
                    scaleX = cardScale
                    scaleY = cardScale
                }
                .drawBehind {
                    val spread = glowSpreadDp.dp.toPx()
                    drawRoundRect(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                glow.copy(alpha = glowAlpha),
                                accent.copy(alpha = glowAlpha * 0.48f),
                                Color.Transparent
                            ),
                            radius = maxOf(size.width, size.height) * 0.74f
                        ),
                        cornerRadius = CornerRadius(28.dp.toPx(), 28.dp.toPx()),
                        size = size.copy(
                            width = size.width + spread,
                            height = size.height + spread
                        ),
                        topLeft = Offset(-spread / 2f, -spread / 2f)
                    )
                }
                .clip(RoundedCornerShape(24.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            surfaceBg,
                            MaterialTheme.colorScheme.surface
                        )
                    )
                )
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            glow.copy(alpha = glowAlpha * 0.24f),
                            Color.Transparent
                        )
                    )
                )
                .drawBehind {
                    val sweepX = size.width * sheenOffset
                    drawRect(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color.Transparent,
                                glow.copy(alpha = 0.09f),
                                Color.Transparent
                            ),
                            start = Offset(sweepX - size.width * 0.3f, 0f),
                            end = Offset(sweepX + size.width * 0.3f, size.height)
                        )
                    )
                }
                .border(
                    width = 2.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            glow.copy(alpha = borderPulseAlpha),
                            accent.copy(alpha = borderPulseAlpha),
                            borderColor.copy(alpha = borderPulseAlpha)
                        )
                    ),
                    shape = RoundedCornerShape(24.dp)
                )
                .padding(20.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (hasCategory) Arrangement.SpaceBetween else Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (hasCategory) {
                        Column(modifier = Modifier.weight(1f, fill = false)) {
                            Text(
                                text = stringResource(R.string.category_label),
                                style = MaterialTheme.typography.labelSmall,
                                color = glow
                            )
                            Text(
                                text = category.ifEmpty { "General" },
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    RoleEmblemChip(
                        identity = roleIdentity,
                        badgeSize = 24.dp,
                        labelOverride = roleIdentity.displayTitle
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))
                HorizontalDivider(color = accent.copy(alpha = 0.28f))
                Spacer(modifier = Modifier.height(18.dp))

                // Prominent Animated Role Emblem Medallion + Player Character Avatar
                if (avatarId != null) {
                    val avatarChar = AvatarCatalog.findById(avatarId)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        CharacterAvatarThumbnail(
                            character = avatarChar,
                            pose = avatarPose,
                            size = 62.dp,
                            borderColor = glow,
                            isHighlighted = true
                        )
                        RoleEmblemBadge(
                            identity = roleIdentity,
                            size = 62.dp,
                            animated = true
                        )
                    }
                } else {
                    RoleEmblemBadge(
                        identity = roleIdentity,
                        size = 64.dp,
                        animated = true
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (isImpostor) {
                    Text(
                        text = if (roleIdentity == GameRoleIdentity.AGENT_KILLER) {
                            "YOU ARE THE AGENT KILLER"
                        } else {
                            "YOU ARE THE IMPOSTOR"
                        },
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = glow,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        color = targetStyle.accent.copy(alpha = 0.16f),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, targetStyle.glow.copy(alpha = 0.55f)),
                        modifier = Modifier.testTag("impostor_hidden_word_badge")
                    ) {
                        Text(
                            text = "SECRET WORD: HIDDEN (???)",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = targetStyle.glow,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (roleIdentity == GameRoleIdentity.AGENT_KILLER) {
                            "Secret word & Agent hints are hidden. Listen closely to blend in — and void one player's vote below!"
                        } else {
                            "Secret word & Agent hints are hidden. Listen to the Agents' clues to blend in or guess the word!"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.testTag("impostor_no_hint_note")
                    )
                } else {
                    Text(
                        text = when (roleIdentity) {
                            GameRoleIdentity.SAVIOR_AGENT -> "SAVIOR AGENT • SECRET WORD"
                            GameRoleIdentity.AGENT_DIRECTOR -> "AGENT DIRECTOR • SECRET WORD"
                            else -> "AGENT • SECRET WORD"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = glow.copy(alpha = 0.9f),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = visibleSecretWord,
                        style = MaterialTheme.typography.displayLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.testTag("secret_word_text")
                    )
                }

                if (hasCategory) {
                    Spacer(modifier = Modifier.height(22.dp))
                    HorizontalDivider(color = accent.copy(alpha = 0.25f))
                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "${stringResource(R.string.genre_label).uppercase()}: ",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = genre.ifEmpty { category },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = glow,
                            modifier = Modifier.testTag("word_genre_text")
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
        }

        // Glowing Hint Box shown ONLY for Agents
        if (hasAgentHint) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .drawBehind {
                        val spread = (glowSpreadDp * 0.7f).dp.toPx()
                        drawRoundRect(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    glow.copy(alpha = glowAlpha * 0.9f),
                                    accent.copy(alpha = glowAlpha * 0.35f),
                                    Color.Transparent
                                ),
                                radius = maxOf(size.width, size.height) * 0.72f
                            ),
                            cornerRadius = CornerRadius(22.dp.toPx(), 22.dp.toPx()),
                            size = size.copy(
                                width = size.width + spread,
                                height = size.height + spread
                            ),
                            topLeft = Offset(-spread / 2f, -spread / 2f)
                        )
                    }
                    .clip(RoundedCornerShape(18.dp))
                    .background(surfaceBg.copy(alpha = 0.92f))
                    .border(
                        width = 1.5.dp,
                        color = glow.copy(alpha = borderPulseAlpha * 0.9f),
                        shape = RoundedCornerShape(18.dp)
                    )
                    .padding(horizontal = 18.dp, vertical = 16.dp)
                    .testTag("hint_box_green")
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = accent.copy(alpha = 0.16f + (glowAlpha * 0.12f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Lightbulb,
                            contentDescription = "Word hint",
                            tint = glow,
                            modifier = Modifier
                                .padding(10.dp)
                                .size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = when (roleIdentity) {
                                GameRoleIdentity.SAVIOR_AGENT -> "SAVIOR HINT"
                                GameRoleIdentity.AGENT_DIRECTOR -> "DIRECTOR HINT"
                                else -> "AGENT HINT"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = glow
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = visibleAgentHint,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}
