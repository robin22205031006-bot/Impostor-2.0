package com.example.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FlipCameraAndroid
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.audio.SoundCuePlayer
import com.example.data.AvatarCatalog
import com.example.data.AvatarPose
import com.example.data.PlayerRole
import com.example.ui.components.AgentDirectorBadge
import com.example.ui.components.AgentPredictionCard
import com.example.ui.components.FloatingHeroSprite
import com.example.ui.components.GameRoleIdentity
import com.example.ui.components.GoldCtaButton
import com.example.ui.components.OtherImpostorAllyNote
import com.example.ui.components.RoleEmblemChip
import com.example.ui.components.SaviorAbilityCard
import com.example.ui.components.UltimateKillerAbilityCard
import com.example.ui.components.style
import com.example.ui.theme.LocalImpostorPalette

@Composable
fun RoleRevealScreen(
    playerName: String,
    playerIndex: Int,
    totalPlayers: Int,
    turnDurationSeconds: Int,
    role: PlayerRole,
    category: String,
    genre: String,
    secretWord: String,
    hint: String,
    isRevealed: Boolean,
    showCategoryAndGenre: Boolean = true,
    showHint: Boolean = true,
    timerEnabled: Boolean = true,
    impostorAbilityEnabled: Boolean = false,
    isUltimateKiller: Boolean = false,
    saviorAgentEnabled: Boolean = false,
    isSavior: Boolean = false,
    agentDirectorEnabled: Boolean = false,
    isDirector: Boolean = false,
    agentPredictionEnabled: Boolean = false,
    otherPlayers: List<Pair<String, String>> = emptyList(),
    selectedKillTargetId: String? = null,
    selectedProtectedId: String? = null,
    selectedPredictionId: String? = null,
    onSelectKillTarget: (String?) -> Unit = {},
    onSelectProtectedTarget: (String?) -> Unit = {},
    onSelectPrediction: (String?) -> Unit = {},
    onReveal: () -> Unit,
    onHideAndNext: () -> Unit,
    impostorCount: Int = 1,
    agentCount: Int = (totalPlayers - 1).coerceAtLeast(1),
    onReshuffleRoles: (() -> Unit)? = null,
    avatarId: String? = null,
    avatarPose: AvatarPose = AvatarPose.ORIGINAL,
    modifier: Modifier = Modifier
) {
    val palette = LocalImpostorPalette.current
    val density = LocalDensity.current.density
    val isLastPlayer = playerIndex >= totalPlayers - 1
    val isImpostor = role == PlayerRole.IMPOSTOR
    val roleIdentity = GameRoleIdentity.resolve(
        role = role,
        isUltimateKiller = impostorAbilityEnabled && isUltimateKiller,
        isSavior = saviorAgentEnabled && isSavior,
        isDirector = agentDirectorEnabled && isDirector
    )
    val roleStyle = roleIdentity.style(palette)

    val flipRotation by animateFloatAsState(
        targetValue = if (isRevealed) 180f else 0f,
        animationSpec = spring(
            dampingRatio = 0.78f,
            stiffness = Spring.StiffnessLow
        ),
        label = "card_flip_rotation"
    )

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed && !isRevealed) 0.965f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "card_press_scale"
    )

    LaunchedEffect(isRevealed, role) {
        if (isRevealed) {
            SoundCuePlayer.playRoleRevealCue(isImpostor = isImpostor)
        }
    }

    val revealScrollState = rememberScrollState()
    LaunchedEffect(playerIndex) {
        revealScrollState.scrollTo(0)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 10.dp)
            .testTag("role_reveal_screen"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(revealScrollState),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Top Pass Progress Header + Randomized Session Role Split
            Surface(
                color = MaterialTheme.colorScheme.surfaceContainer,
                shape = RoundedCornerShape(50),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Text(
                    text = "${playerIndex + 1} / $totalPlayers PLAYERS • PASS THE PHONE",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = palette.goldAccent,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 7.dp)
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceContainerLowest,
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier.testTag("reveal_session_roles_summary")
                ) {
                    Text(
                        text = "$impostorCount IMPOSTOR${if (impostorCount > 1) "S" else ""} • $agentCount AGENT${if (agentCount != 1) "S" else ""}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                    )
                }
                if (onReshuffleRoles != null && playerIndex == 0 && !isRevealed) {
                    Surface(
                        onClick = onReshuffleRoles,
                        color = palette.goldSurface,
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(1.dp, palette.goldAccent.copy(alpha = 0.6f)),
                        modifier = Modifier.testTag("reveal_reshuffle_roles_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Refresh,
                                contentDescription = "Re-assign roles",
                                tint = palette.goldAccent,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "Re-deal",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = palette.goldAccent
                            )
                        }
                    }
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "For your eyes only",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = playerName,
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = palette.goldAccent,
                    textAlign = TextAlign.Center
                )
            }

            // Progress dots
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(totalPlayers.coerceAtMost(12)) { idx ->
                    val isCurrent = idx == playerIndex
                    val isDone = idx < playerIndex
                    val dotWidth by animateDpAsState(
                        targetValue = if (isCurrent) 24.dp else 8.dp,
                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                        label = "dot_width"
                    )
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 3.dp)
                            .size(width = dotWidth, height = 8.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    isCurrent -> if (isRevealed) roleStyle.glow else palette.goldAccent
                                    isDone -> palette.goldAccent.copy(alpha = 0.45f)
                                    else -> MaterialTheme.colorScheme.outline
                                }
                            )
                    )
                }
            }

            // True 3D Flippable Card Container
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer {
                        rotationY = flipRotation
                        cameraDistance = 14f * density
                        scaleX = pressScale
                        scaleY = pressScale
                    }
            ) {
                if (flipRotation <= 90f) {
                    CardFrontFace(
                        playerName = playerName,
                        playerIndex = playerIndex,
                        avatarId = avatarId,
                        avatarPose = avatarPose,
                        interactionSource = interactionSource,
                        onReveal = onReveal
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .graphicsLayer { rotationY = 180f }
                    ) {
                        CardBackFace(
                            role = role,
                            roleIdentity = roleIdentity,
                            category = category,
                            genre = genre,
                            secretWord = secretWord,
                            hint = hint,
                            showCategoryAndGenre = showCategoryAndGenre,
                            showHint = showHint
                        )
                    }
                }
            }

            // Post-flip Special Role Ability Controls
            if (isRevealed) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (impostorAbilityEnabled && isImpostor) {
                        if (isUltimateKiller) {
                            UltimateKillerAbilityCard(
                                candidates = otherPlayers,
                                selectedTargetId = selectedKillTargetId,
                                onSelectTarget = onSelectKillTarget
                            )
                        } else {
                            OtherImpostorAllyNote()
                        }
                    }
                    if (agentDirectorEnabled && !isImpostor && isDirector) {
                        AgentDirectorBadge()
                    }
                    if (saviorAgentEnabled && !isImpostor && isSavior) {
                        SaviorAbilityCard(
                            candidates = otherPlayers,
                            selectedProtectedId = selectedProtectedId,
                            onSelectProtected = onSelectProtectedTarget
                        )
                    }
                    if (agentPredictionEnabled && !isImpostor) {
                        AgentPredictionCard(
                            candidates = otherPlayers,
                            selectedSuspectId = selectedPredictionId,
                            onSelectSuspect = onSelectPrediction
                        )
                    }
                }
            }
        }

        // Bottom Fixed Action Button matching the video's "Read your card first" -> active Gold CTA
        val ctaBg by animateColorAsState(
            targetValue = when {
                !isRevealed -> MaterialTheme.colorScheme.surfaceContainerHigh
                isImpostor -> palette.impostorAccent
                else -> palette.goldAccent
            },
            animationSpec = tween(280),
            label = "reveal_cta_bg"
        )
        val ctaInk = when {
            !isRevealed -> MaterialTheme.colorScheme.onSurfaceVariant
            isImpostor -> Color.White
            else -> palette.goldDarkInk
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (isRevealed) {
            GoldCtaButton(
                text = when {
                    !isLastPlayer -> "Pass the phone"
                    timerEnabled -> "Got it • start (${turnDurationSeconds}s)"
                    else -> "Got it • start discussion"
                },
                onClick = onHideAndNext,
                containerColor = ctaBg,
                contentColor = ctaInk,
                modifier = Modifier.testTag("local_hide_and_pass_button")
            )
        } else {
            Surface(
                color = MaterialTheme.colorScheme.surfaceContainer,
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "Read your card first — Tap card to flip",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun CardFrontFace(
    playerName: String,
    playerIndex: Int,
    avatarId: String?,
    avatarPose: AvatarPose,
    interactionSource: MutableInteractionSource,
    onReveal: () -> Unit
) {
    val palette = LocalImpostorPalette.current
    val pulseTransition = rememberInfiniteTransition(label = "front_card_pulse")
    val borderAlpha by pulseTransition.animateFloat(
        initialValue = 0.55f,
        targetValue = 0.96f,
        animationSpec = infiniteRepeatable(
            animation = tween(1300, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "front_border_alpha"
    )

    val heroChar = remember(avatarId) {
        AvatarCatalog.findById(avatarId ?: AvatarCatalog.all[playerIndex % AvatarCatalog.all.size].id)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 360.dp)
            .drawBehind {
                val spread = 12.dp.toPx()
                drawRoundRect(
                    brush = Brush.radialGradient(
                        colors = listOf(palette.goldAccent.copy(alpha = 0.16f * borderAlpha), Color.Transparent),
                        radius = maxOf(size.width, size.height) * 0.72f
                    ),
                    cornerRadius = CornerRadius(30.dp.toPx(), 30.dp.toPx()),
                    size = size.copy(width = size.width + spread, height = size.height + spread),
                    topLeft = Offset(-spread / 2f, -spread / 2f)
                )
            }
            .clip(RoundedCornerShape(28.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF2B213A),
                        Color(0xFF1F172B)
                    )
                )
            )
            .border(
                width = 2.dp,
                color = palette.goldAccent.copy(alpha = borderAlpha),
                shape = RoundedCornerShape(28.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = androidx.compose.material3.ripple(),
                onClick = onReveal
            )
            .padding(22.dp)
            .testTag("local_tap_to_reveal_card")
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "PLAYER ${playerIndex + 1}",
                    style = MaterialTheme.typography.labelMedium,
                    color = palette.goldAccent,
                    fontWeight = FontWeight.Bold
                )
                Surface(
                    color = palette.goldSurface,
                    shape = CircleShape,
                    border = BorderStroke(1.dp, palette.goldAccent.copy(alpha = 0.5f)),
                    modifier = Modifier.size(34.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Filled.FlipCameraAndroid,
                            contentDescription = "Flip role card",
                            tint = palette.goldAccent,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            FloatingHeroSprite(
                character = heroChar,
                pose = avatarPose,
                size = 200.dp,
                bobOffsetDp = 4f,
                isFocal = true
            )

            Text(
                text = playerName,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            Surface(
                color = palette.goldSurface,
                shape = RoundedCornerShape(50),
                border = BorderStroke(1.dp, palette.goldAccent.copy(alpha = borderAlpha))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.TouchApp,
                        contentDescription = null,
                        tint = palette.goldAccent,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Tap to flip",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = palette.goldAccent
                    )
                }
            }
        }
    }
}

@Composable
private fun CardBackFace(
    role: PlayerRole,
    roleIdentity: GameRoleIdentity,
    category: String,
    genre: String,
    secretWord: String,
    hint: String,
    showCategoryAndGenre: Boolean,
    showHint: Boolean
) {
    val palette = LocalImpostorPalette.current
    val isImpostor = role == PlayerRole.IMPOSTOR
    val targetStyle = roleIdentity.style(palette)
    val hasCategory = showCategoryAndGenre && (category.isNotBlank() || genre.isNotBlank())
    val visibleSecretWord = if (isImpostor) "" else secretWord
    val visibleAgentHint = if (isImpostor) "" else hint
    val hasAgentHint = !isImpostor && showHint && visibleAgentHint.isNotBlank()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(if (isImpostor) "role_card_impostor" else "role_card_normal"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 250.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            targetStyle.deepSurface,
                            MaterialTheme.colorScheme.surface
                        )
                    )
                )
                .border(
                    width = 2.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(targetStyle.glow, targetStyle.accent, targetStyle.border)
                    ),
                    shape = RoundedCornerShape(28.dp)
                )
                .padding(22.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                RoleEmblemChip(
                    identity = roleIdentity,
                    badgeSize = 24.dp,
                    labelOverride = roleIdentity.displayTitle
                )

                HorizontalDivider(color = targetStyle.accent.copy(alpha = 0.28f))

                if (isImpostor) {
                    Text(
                        text = if (roleIdentity == GameRoleIdentity.AGENT_KILLER) {
                            "YOU ARE THE AGENT KILLER"
                        } else {
                            "YOU ARE THE IMPOSTOR"
                        },
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = targetStyle.glow,
                        textAlign = TextAlign.Center
                    )
                    Surface(
                        color = targetStyle.accent.copy(alpha = 0.16f),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, targetStyle.glow.copy(alpha = 0.6f)),
                        modifier = Modifier.testTag("impostor_hidden_word_badge")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Lock,
                                contentDescription = "Secret word hidden",
                                tint = targetStyle.glow,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "SECRET WORD: HIDDEN",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = targetStyle.glow
                            )
                        }
                    }
                    Text(
                        text = if (roleIdentity == GameRoleIdentity.AGENT_KILLER) {
                            "Secret word & Agent hints stay hidden. Listen to blend in — and void 1 vote below."
                        } else {
                            "Secret word & Agent hints stay hidden. Listen to the Agents' clues — or deduce the word."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.testTag("impostor_no_hint_note")
                    )
                } else {
                    Text(
                        text = "YOUR SECRET WORD",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = palette.goldAccent
                    )
                    Text(
                        text = visibleSecretWord,
                        style = MaterialTheme.typography.displayLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.testTag("secret_word_text")
                    )
                    Text(
                        text = "Give a clue using your Agent hint. Keep the word secret.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }

                if (hasCategory) {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceContainerLowest,
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = category.ifEmpty { "General" },
                                style = MaterialTheme.typography.labelLarge,
                                color = palette.goldAccent
                            )
                            Text(
                                text = genre.ifEmpty { category },
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.testTag("word_genre_text")
                            )
                        }
                    }
                }
            }
        }

        if (hasAgentHint) {
            Surface(
                color = targetStyle.deepSurface,
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(1.2.dp, targetStyle.glow.copy(alpha = 0.7f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("hint_box_green")
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Lightbulb,
                        contentDescription = "Word hint",
                        tint = targetStyle.glow,
                        modifier = Modifier.size(22.dp)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "AGENT HINT",
                            style = MaterialTheme.typography.labelSmall,
                            color = targetStyle.glow
                        )
                        Text(
                            text = visibleAgentHint,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}
