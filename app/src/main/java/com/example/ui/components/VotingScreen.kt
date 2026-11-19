package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.HowToVote
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.AvatarCatalog
import com.example.data.GameState
import com.example.data.LocalPlayer
import com.example.ui.theme.LocalImpostorPalette

@Composable
fun VotingScreen(
    state: GameState,
    onCastVote: (voterId: String, suspectId: String) -> Unit,
    onFinalizeVoting: () -> Unit = {},
    onReturnToLobby: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalImpostorPalette.current
    var activeVoterId by remember(state.players) {
        mutableStateOf(state.players.firstOrNull()?.id.orEmpty())
    }

    val activeVoterName = state.players.firstOrNull { it.id == activeVoterId }?.name ?: "Player"
    val selectedSuspectId = state.votesByPlayerId[activeVoterId]
    val voteCounts = state.totalVotesPerPlayer

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("voting_screen")
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            GlassSectionCard(
                title = "VOTING • SELECT ACTIVE VOTER",
                icon = Icons.Filled.HowToVote,
                accentColor = palette.impostorAccent
            ) {
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    state.players.forEach { voter ->
                        val hasVoted = voter.id in state.votesByPlayerId
                        FilterChip(
                            selected = voter.id == activeVoterId,
                            onClick = { activeVoterId = voter.id },
                            label = {
                                Text(if (hasVoted) "${voter.name} ✓" else voter.name)
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = palette.impostorSurface,
                                selectedLabelColor = palette.impostorGlow
                            ),
                            modifier = Modifier.testTag("voter_chip_${voter.id}")
                        )
                    }
                }
                Text(
                    text = "$activeVoterName: Tap an active player card below to cast or change your vote (${state.votesByPlayerId.size}/${state.players.size} voted).",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            state.players.filter { it.id != activeVoterId }.forEach { candidate ->
                val isSelected = selectedSuspectId == candidate.id
                val count = voteCounts[candidate.id] ?: 0
                VotingPlayerCard(
                    player = candidate,
                    isSelected = isSelected,
                    voteCount = count,
                    onClick = { onCastVote(activeVoterId, candidate.id) }
                )
            }
        }

        Surface(
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
            tonalElevation = 4.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (state.votesByPlayerId.isNotEmpty()) {
                    Button(
                        onClick = onFinalizeVoting,
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = palette.impostorAccent,
                            contentColor = Color.White
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(54.dp)
                            .testTag("reveal_voting_verdict_button")
                    ) {
                        Text("Reveal Verdict")
                    }
                }
                Button(
                    onClick = onReturnToLobby,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = palette.normalAccent,
                        contentColor = MaterialTheme.colorScheme.onSecondary
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(54.dp)
                        .testTag("return_to_lobby_button")
                ) {
                    Text("Return to Lobby", style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}

@Composable
fun VotingPlayerCard(
    player: LocalPlayer,
    isSelected: Boolean,
    voteCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalImpostorPalette.current
    val cardBg by animateColorAsState(
        targetValue = if (isSelected) palette.impostorSurface else MaterialTheme.colorScheme.surfaceContainer,
        animationSpec = tween(220),
        label = "voting_card_bg"
    )
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) palette.impostorGlow else MaterialTheme.colorScheme.outlineVariant,
        animationSpec = tween(220),
        label = "voting_card_border"
    )
    val cardScale by animateFloatAsState(
        targetValue = if (isSelected) 1.015f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "voting_card_scale"
    )

    Surface(
        onClick = onClick,
        color = cardBg,
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = borderColor
        ),
        tonalElevation = if (isSelected) 6.dp else 1.dp,
        modifier = modifier
            .fillMaxWidth()
            .minimumInteractiveComponentSize()
            .graphicsLayer {
                scaleX = cardScale
                scaleY = cardScale
            }
            .drawBehind {
                if (isSelected) {
                    val spread = 10.dp.toPx()
                    drawRoundRect(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                palette.impostorGlow.copy(alpha = 0.28f),
                                Color.Transparent
                            ),
                            radius = maxOf(size.width, size.height) * 0.72f
                        ),
                        cornerRadius = CornerRadius(22.dp.toPx(), 22.dp.toPx()),
                        size = size.copy(width = size.width + spread, height = size.height + spread),
                        topLeft = Offset(-spread / 2f, -spread / 2f)
                    )
                }
            }
            .testTag("voting_player_card_${player.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val avatarChar = AvatarCatalog.findById(player.avatarId)
            Box(contentAlignment = Alignment.Center) {
                CharacterAvatarThumbnail(
                    character = avatarChar,
                    pose = player.avatarPose,
                    size = 46.dp,
                    borderColor = if (isSelected) palette.impostorGlow else avatarChar.glowColor,
                    isHighlighted = isSelected
                )
                if (isSelected) {
                    Surface(
                        color = palette.impostorAccent,
                        shape = CircleShape,
                        border = BorderStroke(1.dp, Color.White),
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(20.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Filled.GpsFixed,
                                contentDescription = "Selected suspect ${player.name}",
                                tint = Color.White,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = player.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                    color = if (isSelected) palette.impostorGlow else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = if (isSelected) "SELECTED FOR ELIMINATION" else "Tap card to vote against ${player.name}",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isSelected) palette.impostorGlow.copy(alpha = 0.9f) else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (voteCount > 0) {
                    Surface(
                        color = palette.timerSurface,
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(1.dp, palette.timerAccent.copy(alpha = 0.7f))
                    ) {
                        Text(
                            text = "$voteCount vote${if (voteCount == 1) "" else "s"}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = palette.timerAccent,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
                AnimatedVisibility(
                    visible = isSelected,
                    enter = fadeIn(tween(180)),
                    exit = fadeOut(tween(150))
                ) {
                    Surface(
                        color = palette.impostorAccent,
                        shape = CircleShape,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = "Vote cast",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
