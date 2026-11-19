package com.example.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
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
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HowToVote
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.data.AvatarCatalog
import com.example.data.AvatarPose
import com.example.ui.theme.LocalImpostorPalette
import kotlinx.coroutines.delay

data class EliminatedPlayerReveal(
    val id: String,
    val name: String,
    val isImpostor: Boolean,
    val voteCount: Int,
    val roleIdentity: GameRoleIdentity = if (isImpostor) GameRoleIdentity.IMPOSTOR else GameRoleIdentity.AGENT,
    val avatarId: String = AvatarCatalog.defaultIdForSeed(id),
    val avatarPose: AvatarPose = AvatarPose.ORIGINAL
)

@Composable
fun EliminationRevealOverlay(
    eliminatedPlayers: List<EliminatedPlayerReveal>,
    crewWon: Boolean,
    impostorGuessCorrect: Boolean,
    onRevealVerdict: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalImpostorPalette.current
    var roleRevealed by remember { mutableStateOf(false) }
    var verdictNotified by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(950L)
        roleRevealed = true
        if (!verdictNotified) {
            verdictNotified = true
            onRevealVerdict()
        }
    }

    BackHandler {
        if (!verdictNotified) {
            verdictNotified = true
            onRevealVerdict()
        }
        onDismiss()
    }

    val singleTarget = eliminatedPlayers.singleOrNull()
    val hasEliminatedImpostor = eliminatedPlayers.any { it.isImpostor }
    val primaryIdentity = when {
        singleTarget != null -> singleTarget.roleIdentity
        hasEliminatedImpostor -> eliminatedPlayers.first { it.isImpostor }.roleIdentity
        else -> GameRoleIdentity.AGENT
    }
    val primaryStyle = primaryIdentity.style(palette)

    val verdictAccent = when {
        !roleRevealed -> palette.cyanAccent
        eliminatedPlayers.isEmpty() -> palette.timerAccent
        else -> primaryStyle.accent
    }
    val verdictGlow = when {
        !roleRevealed -> palette.cyanAccent
        eliminatedPlayers.isEmpty() -> palette.timerAccent
        else -> primaryStyle.glow
    }
    val verdictSurface = when {
        !roleRevealed -> palette.cyanSurface
        eliminatedPlayers.isEmpty() -> palette.timerSurface
        else -> primaryStyle.deepSurface
    }

    val animatedAccent by animateColorAsState(
        targetValue = verdictAccent,
        animationSpec = tween(durationMillis = 420),
        label = "elimination_accent"
    )
    val animatedGlow by animateColorAsState(
        targetValue = verdictGlow,
        animationSpec = tween(durationMillis = 420),
        label = "elimination_glow"
    )
    val animatedSurface by animateColorAsState(
        targetValue = verdictSurface,
        animationSpec = tween(durationMillis = 420),
        label = "elimination_surface"
    )

    val pulseTransition = rememberInfiniteTransition(label = "elimination_pulse")
    val ringScale by pulseTransition.animateFloat(
        initialValue = 0.65f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "elimination_ring_scale"
    )
    val glowAlpha by pulseTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "elimination_glow_alpha"
    )
    val flipScale by animateFloatAsState(
        targetValue = if (roleRevealed) 1f else 0.94f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "elimination_card_scale"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background.copy(alpha = 0.94f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {}
            )
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(20.dp)
            .testTag("elimination_reveal_overlay"),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val maxR = size.minDimension * 0.44f
            drawCircle(
                color = animatedGlow.copy(alpha = 0.14f * glowAlpha),
                radius = maxR * 0.85f
            )
            drawCircle(
                color = animatedGlow.copy(alpha = ((1.15f - ringScale).coerceIn(0f, 1f)) * 0.35f),
                radius = maxR * ringScale,
                style = Stroke(width = 2.5.dp.toPx())
            )
        }

        Column(
            modifier = Modifier
                .widthIn(max = 520.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Surface(
                color = animatedSurface.copy(alpha = 0.94f),
                shape = RoundedCornerShape(28.dp),
                border = BorderStroke(2.dp, animatedAccent.copy(alpha = glowAlpha)),
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer {
                        scaleX = flipScale
                        scaleY = flipScale
                    }
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    animatedAccent.copy(alpha = 0.16f),
                                    Color.Transparent,
                                    animatedSurface.copy(alpha = 0.65f)
                                )
                            )
                        )
                        .padding(24.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        AnimatedContent(
                            targetState = roleRevealed && eliminatedPlayers.isNotEmpty(),
                            transitionSpec = {
                                (fadeIn(tween(260)) + scaleIn(initialScale = 0.78f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)))
                                    .togetherWith(fadeOut(tween(140)))
                            },
                            label = "elimination_emblem_swap"
                        ) { showEmblem ->
                            if (showEmblem) {
                                if (singleTarget != null) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        CharacterAvatarThumbnail(
                                            character = AvatarCatalog.findById(singleTarget.avatarId),
                                            pose = singleTarget.avatarPose,
                                            size = 72.dp,
                                            borderColor = animatedGlow,
                                            isHighlighted = true
                                        )
                                        RoleEmblemBadge(
                                            identity = primaryIdentity,
                                            size = 72.dp,
                                            animated = true
                                        )
                                    }
                                } else {
                                    RoleEmblemBadge(
                                        identity = primaryIdentity,
                                        size = 76.dp,
                                        animated = true
                                    )
                                }
                            } else if (singleTarget != null) {
                                Box(contentAlignment = Alignment.Center) {
                                    CharacterAvatarThumbnail(
                                        character = AvatarCatalog.findById(singleTarget.avatarId),
                                        pose = singleTarget.avatarPose,
                                        size = 74.dp,
                                        borderColor = animatedAccent,
                                        isHighlighted = true
                                    )
                                    Surface(
                                        color = animatedSurface,
                                        shape = CircleShape,
                                        border = BorderStroke(1.5.dp, animatedAccent),
                                        modifier = Modifier.align(Alignment.BottomEnd)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.HowToVote,
                                            contentDescription = "Elimination verdict icon",
                                            tint = animatedGlow,
                                            modifier = Modifier
                                                .padding(5.dp)
                                                .size(16.dp)
                                        )
                                    }
                                }
                            } else {
                                Surface(
                                    color = animatedAccent.copy(alpha = 0.16f),
                                    shape = CircleShape,
                                    border = BorderStroke(1.5.dp, animatedAccent.copy(alpha = 0.8f))
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.HowToVote,
                                        contentDescription = "Elimination verdict icon",
                                        tint = animatedGlow,
                                        modifier = Modifier
                                            .padding(16.dp)
                                            .size(38.dp)
                                    )
                                }
                            }
                        }

                        Text(
                            text = when {
                                eliminatedPlayers.isEmpty() -> "NO VOTES COUNTED"
                                eliminatedPlayers.size > 1 -> "TIE VOTE ELIMINATION"
                                else -> "PLAYER ELIMINATED"
                            },
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = animatedGlow,
                            textAlign = TextAlign.Center
                        )

                        if (eliminatedPlayers.isEmpty()) {
                            Text(
                                text = "No Player Eliminated",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center
                            )
                        } else if (singleTarget != null) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = singleTarget.name,
                                    style = MaterialTheme.typography.displayMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.testTag("eliminated_player_name")
                                )
                                Surface(
                                    color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.8f),
                                    shape = RoundedCornerShape(50)
                                ) {
                                    Text(
                                        text = "${singleTarget.voteCount} vote${if (singleTarget.voteCount == 1) "" else "s"} against",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        } else {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                eliminatedPlayers.forEach { target ->
                                    val targetStyle = target.roleIdentity.style(palette)
                                    Surface(
                                        color = if (roleRevealed) targetStyle.deepSurface else MaterialTheme.colorScheme.surfaceContainerHigh,
                                        shape = RoundedCornerShape(14.dp),
                                        border = BorderStroke(
                                            1.dp,
                                            if (!roleRevealed) palette.cyanAccent.copy(alpha = 0.6f) else targetStyle.accent
                                        ),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                val targetChar = AvatarCatalog.findById(target.avatarId)
                                                CharacterAvatarThumbnail(
                                                    character = targetChar,
                                                    pose = target.avatarPose,
                                                    size = 34.dp,
                                                    borderColor = if (roleRevealed) targetStyle.glow else targetChar.glowColor,
                                                    isHighlighted = roleRevealed
                                                )
                                                if (roleRevealed) {
                                                    RoleEmblemBadge(
                                                        identity = target.roleIdentity,
                                                        size = 24.dp,
                                                        animated = true
                                                    )
                                                }
                                                Text(
                                                    text = "${target.name} (${target.voteCount} votes)",
                                                    style = MaterialTheme.typography.titleMedium,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                            }
                                            Text(
                                                text = if (!roleRevealed) "CHECKING..." else target.roleIdentity.displayTitle,
                                                style = MaterialTheme.typography.labelLarge,
                                                fontWeight = FontWeight.Bold,
                                                color = if (!roleRevealed) palette.cyanAccent else targetStyle.glow
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        AnimatedContent(
                            targetState = roleRevealed,
                            transitionSpec = {
                                (fadeIn(tween(300)) + scaleIn(initialScale = 0.88f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)))
                                    .togetherWith(fadeOut(tween(150)))
                            },
                            label = "role_verdict_reveal"
                        ) { revealed ->
                            if (!revealed) {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Text(
                                        text = "REVEALING TRUE IDENTITY...",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = palette.cyanAccent
                                    )
                                    LinearProgressIndicator(
                                        color = palette.cyanAccent,
                                        trackColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                                        modifier = Modifier
                                            .fillMaxWidth(0.75f)
                                            .height(6.dp)
                                            .clip(CircleShape)
                                    )
                                }
                            } else {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Surface(
                                        color = animatedAccent.copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(16.dp),
                                        border = BorderStroke(1.5.dp, animatedAccent),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(vertical = 14.dp, horizontal = 16.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Text(
                                                text = "TRUE ROLE REVEALED",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = animatedGlow
                                            )
                                            Text(
                                                text = when {
                                                    eliminatedPlayers.isEmpty() -> "NO IMPOSTOR CAUGHT"
                                                    singleTarget != null -> when (singleTarget.roleIdentity) {
                                                        GameRoleIdentity.AGENT_KILLER -> "AGENT KILLER!"
                                                        GameRoleIdentity.IMPOSTOR -> "IMPOSTOR!"
                                                        GameRoleIdentity.SAVIOR_AGENT -> "SAVIOR AGENT"
                                                        GameRoleIdentity.AGENT_DIRECTOR -> "AGENT DIRECTOR"
                                                        GameRoleIdentity.AGENT -> "REGULAR AGENT"
                                                    }
                                                    hasEliminatedImpostor -> "IMPOSTOR EXPOSED!"
                                                    else -> "ALL ELIMINATED WERE AGENTS"
                                                },
                                                style = MaterialTheme.typography.headlineMedium,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = animatedGlow,
                                                textAlign = TextAlign.Center,
                                                modifier = Modifier.testTag("eliminated_true_role_text")
                                            )
                                        }
                                    }

                                    Text(
                                        text = when {
                                            impostorGuessCorrect -> "Wait — the Impostor guessed the secret word and stole the round!"
                                            crewWon -> "Agents successfully identified the threat!"
                                            singleTarget?.isImpostor == false -> "An innocent Agent was voted out — Impostors prevail!"
                                            else -> "Impostors survived the vote undetected!"
                                        },
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }
            }

            AnimatedVisibility(
                visible = roleRevealed,
                enter = fadeIn(tween(260)) + slideInVertically(tween(260)) { it / 3 }
            ) {
                Button(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = animatedAccent,
                        contentColor = if (singleTarget?.isImpostor == true || hasEliminatedImpostor) {
                            MaterialTheme.colorScheme.onError
                        } else {
                            MaterialTheme.colorScheme.onSecondary
                        }
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("elimination_continue_button")
                ) {
                    Text(
                        text = "View Full Breakdown & Scores",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }

            if (!roleRevealed) {
                TextButton(
                    onClick = {
                        if (!verdictNotified) {
                            verdictNotified = true
                            onRevealVerdict()
                        }
                        onDismiss()
                    },
                    modifier = Modifier.testTag("elimination_skip_button")
                ) {
                    Text("Skip Reveal", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}
