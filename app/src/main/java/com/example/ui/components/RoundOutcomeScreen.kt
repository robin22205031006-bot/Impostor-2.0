package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.HowToVote
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.data.AvatarCatalog
import com.example.data.GameState
import com.example.data.PlayerRole
import com.example.data.RoundOutcome
import com.example.ui.theme.LocalImpostorPalette

@Composable
fun RoundOutcomeScreen(
    state: GameState,
    onPlayAgain: () -> Unit,
    onReturnToLobby: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalImpostorPalette.current
    val isVictory = state.roundOutcome == RoundOutcome.VICTORY
    val heroIdentity = if (isVictory) GameRoleIdentity.AGENT else GameRoleIdentity.IMPOSTOR
    val heroStyle = heroIdentity.style(palette)
    val impostorPlayers = remember(state.players, state.impostorIds) {
        state.players.filter { it.id in state.impostorIds }
    }
    val maxVotes = remember(state.totalVotesPerPlayer) {
        (state.totalVotesPerPlayer.values.maxOrNull() ?: 0).coerceAtLeast(1)
    }
    val sortedPlayers = remember(state.players, state.totalVotesPerPlayer) {
        state.players.sortedByDescending { state.totalVotesPerPlayer[it.id] ?: 0 }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("round_outcome_screen")
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Victory / Defeat Hero Card
            Surface(
                color = heroStyle.deepSurface,
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(2.dp, heroStyle.glow.copy(alpha = 0.85f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .drawBehind {
                        val spread = 14.dp.toPx()
                        drawRoundRect(
                            brush = Brush.radialGradient(
                                colors = listOf(heroStyle.glow.copy(alpha = 0.28f), Color.Transparent),
                                radius = maxOf(size.width, size.height) * 0.72f
                            ),
                            cornerRadius = CornerRadius(28.dp.toPx(), 28.dp.toPx()),
                            size = size.copy(width = size.width + spread, height = size.height + spread),
                            topLeft = Offset(-spread / 2f, -spread / 2f)
                        )
                    }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    RoleEmblemBadge(
                        identity = heroIdentity,
                        size = 56.dp,
                        animated = true
                    )

                    Surface(
                        color = heroStyle.accent.copy(alpha = 0.20f),
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(1.dp, heroStyle.glow.copy(alpha = 0.7f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = if (isVictory) Icons.Filled.EmojiEvents else Icons.Filled.Warning,
                                contentDescription = null,
                                tint = heroStyle.glow,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = if (isVictory) "IMPOSTORS VOTED OUT" else "IMPOSTORS ACHIEVED THEIR GOAL",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = heroStyle.glow
                            )
                        }
                    }

                    Text(
                        text = if (isVictory) "VICTORY" else "DEFEAT",
                        style = MaterialTheme.typography.displayLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = heroStyle.glow,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.testTag("outcome_headline_text")
                    )

                    Text(
                        text = state.outcomeReason.ifBlank {
                            if (isVictory) {
                                "The Agents identified and voted out the Impostor!"
                            } else {
                                "The Impostors avoided elimination and achieved their goal!"
                            }
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Impostors & Secret Word Reveal Card
            GlassSectionCard(
                title = "CLASSIFIED DEBRIEF",
                icon = Icons.Filled.LocationOn,
                accentColor = palette.cyanAccent
            ) {
                Text(
                    text = "Impostor${if (impostorPlayers.size > 1) "s" else ""}: ${impostorPlayers.joinToString(", ") { it.name }.ifBlank { "None" }}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = palette.impostorGlow
                )
                state.locationWord?.let { loc ->
                    Text(
                        text = "Secret Word: ${loc.word} (${loc.category} • ${loc.genre})",
                        style = MaterialTheme.typography.bodyMedium,
                        color = palette.normalGlow
                    )
                }
            }

            // Total Votes Per Player Tally
            GlassSectionCard(
                title = "TOTAL VOTES PER PLAYER",
                icon = Icons.Filled.HowToVote,
                accentColor = palette.impostorAccent,
                modifier = Modifier.testTag("outcome_votes_breakdown_card")
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    sortedPlayers.forEach { player ->
                        val votes = state.totalVotesPerPlayer[player.id] ?: 0
                        val isImpostor = (state.playerRoles[player.id] ?: PlayerRole.NORMAL) == PlayerRole.IMPOSTOR
                        val wasVotedOut = player.id in state.votedOutPlayerIds
                        val roleIdentity = if (isImpostor) GameRoleIdentity.IMPOSTOR else GameRoleIdentity.AGENT
                        val roleStyle = roleIdentity.style(palette)
                        val progress by animateFloatAsState(
                            targetValue = (votes.toFloat() / maxVotes.toFloat()).coerceIn(0f, 1f),
                            animationSpec = tween(durationMillis = 360),
                            label = "vote_bar_${player.id}"
                        )

                        Surface(
                            color = if (wasVotedOut) roleStyle.deepSurface else MaterialTheme.colorScheme.surfaceContainer,
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(
                                width = if (wasVotedOut) 1.5.dp else 1.dp,
                                color = if (wasVotedOut) roleStyle.glow else MaterialTheme.colorScheme.outlineVariant
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        val avatarChar = AvatarCatalog.findById(player.avatarId)
                                        CharacterAvatarThumbnail(
                                            character = avatarChar,
                                            pose = player.avatarPose,
                                            size = 36.dp,
                                            borderColor = if (wasVotedOut) roleStyle.glow else avatarChar.glowColor,
                                            isHighlighted = wasVotedOut
                                        )
                                        RoleEmblemBadge(
                                            identity = roleIdentity,
                                            size = 24.dp,
                                            animated = false
                                        )
                                        Column {
                                            Text(
                                                text = player.name,
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = buildString {
                                                    append(if (isImpostor) "Impostor" else "Agent")
                                                    if (wasVotedOut) append(" • Voted Out")
                                                },
                                                style = MaterialTheme.typography.labelSmall,
                                                color = roleStyle.glow
                                            )
                                        }
                                    }
                                    Surface(
                                        color = if (votes > 0) palette.timerSurface else MaterialTheme.colorScheme.surfaceContainerHigh,
                                        shape = RoundedCornerShape(50),
                                        border = BorderStroke(
                                            1.dp,
                                            if (votes > 0) palette.timerAccent.copy(alpha = 0.7f) else MaterialTheme.colorScheme.outlineVariant
                                        )
                                    ) {
                                        Text(
                                            text = "$votes vote${if (votes == 1) "" else "s"}",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = if (votes > 0) palette.timerAccent else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier
                                                .padding(horizontal = 10.dp, vertical = 4.dp)
                                                .testTag("outcome_player_votes_${player.id}")
                                        )
                                    }
                                }

                                LinearProgressIndicator(
                                    progress = { progress },
                                    color = if (wasVotedOut) roleStyle.glow else palette.cyanAccent,
                                    trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(50))
                                )
                            }
                        }
                    }
                }
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
                OutlinedButton(
                    onClick = onReturnToLobby,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .testTag("outcome_return_lobby_button")
                ) {
                    Text("Lobby")
                }
                Button(
                    onClick = onPlayAgain,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isVictory) palette.normalAccent else palette.impostorAccent,
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .testTag("outcome_play_again_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Refresh,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 6.dp)
                    )
                    Text("Play Again", style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}
