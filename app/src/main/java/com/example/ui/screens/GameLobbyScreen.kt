package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.foundation.verticalScroll
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.HowToVote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.GamePhase
import com.example.data.GameState
import com.example.data.LocalPlayer
import com.example.data.PlayerRole
import com.example.data.TurnStatus
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.AlertDialog
import androidx.compose.ui.platform.LocalContext
import com.example.data.AvatarCatalog
import com.example.data.AvatarPose
import com.example.data.CategoryWordGenerator
import com.example.data.WordBank
import com.example.ui.components.AvatarPickerSheet
import com.example.ui.components.CharacterAvatarThumbnail
import com.example.ui.components.CircularCountdownTimer
import com.example.ui.components.EmergencyMeetingOverlay
import com.example.ui.components.GameRoleIdentity
import com.example.ui.components.GlassSectionCard
import com.example.ui.components.PlumTogglePill
import com.example.ui.components.PulsingStatusDot
import com.example.ui.components.RoleEmblemBadge
import com.example.ui.components.RoundOutcomeScreen
import com.example.ui.components.TurnTimerEventBus
import com.example.ui.components.VotingScreen
import com.example.ui.components.style
import com.example.ui.theme.LocalImpostorPalette
import com.example.viewmodel.GameViewModel
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Lock

@Composable
fun GameLobbyScreen(
    viewModel: GameViewModel = viewModel(key = "tactical_game_lobby"),
    modifier: Modifier = Modifier
) {
    val state by viewModel.gameState.collectAsStateWithLifecycle()

    BackHandler(enabled = state.phase != GamePhase.LOBBY) {
        viewModel.returnToLobby()
    }

    GameLobbyContent(
        state = state,
        onSetPlayerCount = viewModel::setPlayerCount,
        onSetImpostorCount = viewModel::setImpostorCount,
        onShuffleRoles = viewModel::assignRoles,
        onSelectCategory = viewModel::setCategory,
        onToggleHints = viewModel::setHintsEnabled,
        onShuffleLocation = viewModel::selectNewLocationWord,
        onToggleRoleReveal = viewModel::toggleRoleReveal,
        onRevealAll = viewModel::revealAllRoles,
        onHideAll = viewModel::hideAllRoles,
        onAddPlayer = viewModel::addPlayer,
        onRemovePlayer = viewModel::removePlayer,
        onSetPlayerAvatar = viewModel::setPlayerAvatar,
        onStartDiscussion = viewModel::startDiscussion,
        onToggleTimerPause = viewModel::toggleTimerPause,
        onNextTurn = viewModel::nextTurn,
        onTriggerEmergencyMeeting = viewModel::triggerEmergencyMeeting,
        onDismissEmergencyMeeting = viewModel::dismissEmergencyMeetingAndVote,
        onStartVoting = viewModel::startVoting,
        onCastVote = viewModel::castVote,
        onFinalizeVoting = viewModel::finalizeVoting,
        onPlayAgain = viewModel::playNextRound,
        onReturnToLobby = viewModel::returnToLobby,
        modifier = modifier
    )
}

@Composable
fun GameLobbyContent(
    state: GameState,
    onSetPlayerCount: (Int) -> Unit = {},
    onSetImpostorCount: (Int) -> Unit,
    onShuffleRoles: () -> Unit,
    onSelectCategory: (String) -> Unit = {},
    onToggleHints: (Boolean) -> Unit = {},
    onShuffleLocation: () -> Unit = {},
    onToggleRoleReveal: (String) -> Unit,
    onRevealAll: () -> Unit,
    onHideAll: () -> Unit,
    onAddPlayer: (String) -> Unit,
    onRemovePlayer: (String) -> Unit,
    onSetPlayerAvatar: (String, String, AvatarPose?) -> Unit = { _, _, _ -> },
    onStartDiscussion: () -> Unit,
    onToggleTimerPause: () -> Unit,
    onNextTurn: () -> Unit,
    onTriggerEmergencyMeeting: (String?, String) -> Unit = { _, _ -> },
    onDismissEmergencyMeeting: () -> Unit = {},
    onStartVoting: () -> Unit,
    onCastVote: (String, String) -> Unit,
    onFinalizeVoting: () -> Unit = {},
    onPlayAgain: () -> Unit = {},
    onReturnToLobby: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalImpostorPalette.current

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("game_lobby_screen")
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
        // Phase Stepper Header: Lobby -> Discussion -> Voting -> Results
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            GamePhase.entries.forEach { phase ->
                val isActive = state.phase == phase
                val phaseAccent = when (phase) {
                    GamePhase.LOBBY -> palette.normalAccent
                    GamePhase.DISCUSSION -> palette.cyanAccent
                    GamePhase.VOTING -> palette.impostorAccent
                    GamePhase.RESULTS -> palette.timerAccent
                }
                val bgColor by animateColorAsState(
                    targetValue = if (isActive) phaseAccent.copy(alpha = 0.20f) else MaterialTheme.colorScheme.surfaceContainer,
                    label = "phase_pill_bg"
                )
                val borderColor by animateColorAsState(
                    targetValue = if (isActive) phaseAccent else MaterialTheme.colorScheme.outlineVariant,
                    label = "phase_pill_border"
                )
                Surface(
                    color = bgColor,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(if (isActive) 1.5.dp else 1.dp, borderColor),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("phase_pill_${phase.name.lowercase()}")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        if (isActive) {
                            PulsingStatusDot(color = phaseAccent)
                            Spacer(modifier = Modifier.width(4.dp))
                        }
                        Text(
                            text = phase.title.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                            color = if (isActive) phaseAccent else MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        AnimatedContent(
            targetState = state.phase,
            transitionSpec = {
                (fadeIn(tween(240)) + slideInVertically(tween(240)) { it / 18 }) togetherWith
                    fadeOut(tween(160))
            },
            label = "game_state_phase_transition",
            modifier = Modifier.weight(1f)
        ) { phase ->
            when (phase) {
                GamePhase.LOBBY -> LobbyPhaseView(
                    state = state,
                    onSetPlayerCount = onSetPlayerCount,
                    onSetImpostorCount = onSetImpostorCount,
                    onShuffleRoles = onShuffleRoles,
                    onSelectCategory = onSelectCategory,
                    onToggleHints = onToggleHints,
                    onShuffleLocation = onShuffleLocation,
                    onToggleRoleReveal = onToggleRoleReveal,
                    onRevealAll = onRevealAll,
                    onHideAll = onHideAll,
                    onAddPlayer = onAddPlayer,
                    onRemovePlayer = onRemovePlayer,
                    onSetPlayerAvatar = onSetPlayerAvatar,
                    onStartDiscussion = onStartDiscussion
                )

                GamePhase.DISCUSSION -> DiscussionPhaseView(
                    state = state,
                    onToggleTimerPause = onToggleTimerPause,
                    onNextTurn = onNextTurn,
                    onTriggerEmergencyMeeting = onTriggerEmergencyMeeting,
                    onStartVoting = onStartVoting,
                    onReturnToLobby = onReturnToLobby
                )

                GamePhase.VOTING -> VotingScreen(
                    state = state,
                    onCastVote = onCastVote,
                    onFinalizeVoting = onFinalizeVoting,
                    onReturnToLobby = onReturnToLobby
                )

                GamePhase.RESULTS -> RoundOutcomeScreen(
                    state = state,
                    onPlayAgain = onPlayAgain,
                    onReturnToLobby = onReturnToLobby
                )
            }
        }
        }

        AnimatedVisibility(
            visible = state.emergencyMeeting.isActive,
            enter = fadeIn(tween(180)),
            exit = fadeOut(tween(180))
        ) {
            EmergencyMeetingOverlay(
                alert = state.emergencyMeeting,
                onProceedNow = onDismissEmergencyMeeting
            )
        }
    }
}

@Composable
private fun LobbyPhaseView(
    state: GameState,
    onSetPlayerCount: (Int) -> Unit,
    onSetImpostorCount: (Int) -> Unit,
    onShuffleRoles: () -> Unit,
    onSelectCategory: (String) -> Unit,
    onToggleHints: (Boolean) -> Unit,
    onShuffleLocation: () -> Unit,
    onToggleRoleReveal: (String) -> Unit,
    onRevealAll: () -> Unit,
    onHideAll: () -> Unit,
    onAddPlayer: (String) -> Unit,
    onRemovePlayer: (String) -> Unit,
    onSetPlayerAvatar: (String, String, AvatarPose?) -> Unit = { _, _, _ -> },
    onStartDiscussion: () -> Unit
) {
    val palette = LocalImpostorPalette.current
    val maxImpostors = (state.players.size - 1).coerceAtLeast(1)
    val agentCount = (state.players.size - state.impostorCount).coerceAtLeast(1)
    val allRevealed = state.players.isNotEmpty() && state.revealedRolePlayerIds.size >= state.players.size
    var newPlayerName by remember { mutableStateOf("") }
    var showLocationPreview by remember { mutableStateOf(false) }
    var avatarPickerPlayerId by remember { mutableStateOf<String?>(null) }
    val categories = remember { CategoryWordGenerator.availableCategories() }

    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .testTag("lobby_lazy_column"),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Session Role Assignment Setup Card (Players + Impostors/Agents split + Randomize Action)
            item {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.90f),
                    shape = RoundedCornerShape(18.dp),
                    border = BorderStroke(1.dp, palette.impostorAccent.copy(alpha = 0.45f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("role_assignment_banner")
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "ROLE ASSIGNMENT • DEAL #${state.sessionSeedNumber}",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = palette.goldAccent
                                )
                                Text(
                                    text = "${state.impostorCount} Impostor${if (state.impostorCount > 1) "s" else ""} • $agentCount Agent${if (agentCount != 1) "s" else ""}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.testTag("lobby_role_split_summary")
                                )
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                TextButton(
                                    onClick = onShuffleRoles,
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                                    modifier = Modifier.testTag("lobby_shuffle_roles_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Refresh,
                                        contentDescription = "Re-deal roles",
                                        tint = palette.cyanAccent,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Deal", color = palette.cyanAccent)
                                }
                                TextButton(
                                    onClick = { if (allRevealed) onHideAll() else onRevealAll() },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                                    modifier = Modifier.testTag("lobby_toggle_all_roles_button")
                                ) {
                                    Icon(
                                        imageVector = if (allRevealed) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                        contentDescription = if (allRevealed) "Hide all roles" else "Reveal all roles",
                                        tint = palette.cyanAccent,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (allRevealed) "Hide All" else "Reveal All",
                                        color = palette.cyanAccent
                                    )
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { onSetPlayerCount(state.players.size - 1) },
                                    enabled = state.players.size > 2,
                                    modifier = Modifier
                                        .size(48.dp)
                                        .testTag("lobby_decrease_players_button"),
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Text("-", style = MaterialTheme.typography.titleMedium)
                                }
                                Text(
                                    text = "${state.players.size}P",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = palette.cyanAccent,
                                    modifier = Modifier
                                        .padding(horizontal = 2.dp)
                                        .testTag("lobby_player_count_text")
                                )
                                OutlinedButton(
                                    onClick = { onSetPlayerCount(state.players.size + 1) },
                                    enabled = state.players.size < 16,
                                    modifier = Modifier
                                        .size(48.dp)
                                        .testTag("lobby_increase_players_button"),
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Text("+", style = MaterialTheme.typography.titleMedium)
                                }
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { onSetImpostorCount(state.impostorCount - 1) },
                                    enabled = state.impostorCount > 1,
                                    modifier = Modifier
                                        .size(48.dp)
                                        .testTag("lobby_decrease_impostor_button"),
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Text("-", style = MaterialTheme.typography.titleMedium)
                                }
                                AnimatedContent(
                                    targetState = state.impostorCount,
                                    transitionSpec = {
                                        (slideInVertically { -it / 2 } + fadeIn()) togetherWith
                                            (slideOutVertically { it / 2 } + fadeOut())
                                    },
                                    label = "lobby_impostor_count"
                                ) { count ->
                                    Text(
                                        text = "${count}I",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = palette.impostorGlow,
                                        modifier = Modifier
                                            .padding(horizontal = 2.dp)
                                            .testTag("lobby_impostor_count_text")
                                    )
                                }
                                OutlinedButton(
                                    onClick = { onSetImpostorCount(state.impostorCount + 1) },
                                    enabled = state.impostorCount < maxImpostors,
                                    modifier = Modifier
                                        .size(48.dp)
                                        .testTag("lobby_increase_impostor_button"),
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Text("+", style = MaterialTheme.typography.titleMedium)
                                }
                            }
                        }
                    }
                }
            }

            // Player Cards with Distinct Glowing Compose Modifiers per Assigned Role
            items(state.players, key = { it.id }) { player ->
                val assignedRole = state.playerRoles[player.id] ?: PlayerRole.NORMAL
                val isRevealed = player.id in state.revealedRolePlayerIds
                GlowingLobbyRoleCard(
                    player = player,
                    assignedRole = assignedRole,
                    secretWord = state.referenceWordFor(player.id),
                    agentHint = state.referenceHintFor(player.id),
                    isRevealed = isRevealed,
                    canRemove = state.players.size > 2,
                    onToggleReveal = { onToggleRoleReveal(player.id) },
                    onPickAvatar = { avatarPickerPlayerId = player.id },
                    onRemove = { onRemovePlayer(player.id) }
                )
            }

            // Category-Based Word Generator Card (Agents get Hint; Secret Word hidden from Impostors)
            item {
                GlassSectionCard(
                    title = "CATEGORY WORD GENERATOR",
                    icon = Icons.Filled.Category,
                    accentColor = palette.normalAccent,
                    modifier = Modifier.testTag("category_word_generator_card"),
                    trailing = {
                        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                            TextButton(
                                onClick = { showLocationPreview = !showLocationPreview },
                                modifier = Modifier.testTag("toggle_location_preview_button")
                            ) {
                                Text(
                                    text = if (showLocationPreview) "Hide" else "Peek",
                                    color = palette.normalGlow
                                )
                            }
                            TextButton(
                                onClick = onShuffleLocation,
                                modifier = Modifier.testTag("shuffle_location_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Refresh,
                                    contentDescription = "Generate new word",
                                    tint = palette.normalGlow,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("New", color = palette.normalGlow)
                            }
                        }
                    }
                ) {
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        categories.forEach { category ->
                            val isSelected = state.selectedCategory.equals(category, ignoreCase = true)
                            val label = if (category == WordBank.CATEGORY_ALL) "All Categories" else category
                            FilterChip(
                                selected = isSelected,
                                onClick = { onSelectCategory(category) },
                                label = { Text(label) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = palette.goldAccent,
                                    selectedLabelColor = palette.goldDarkInk
                                ),
                                modifier = Modifier.testTag("lobby_category_chip_${category.lowercase().replace(" ", "_")}")
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Lightbulb,
                                contentDescription = null,
                                tint = palette.goldAccent,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Agent Hints (hidden from Impostor)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        PlumTogglePill(
                            checked = state.hintsEnabled,
                            onCheckedChange = onToggleHints,
                            modifier = Modifier.testTag("lobby_agent_hints_toggle")
                        )
                    }

                    val word = state.locationWord
                    Text(
                        text = if (showLocationPreview && word != null) {
                            buildString {
                                append("${word.word} (${word.category} • ${word.genre})")
                                if (state.hintsEnabled && word.hint.isNotBlank()) {
                                    append(" — Agent Hint: ${word.hint}")
                                }
                            }
                        } else {
                            "Word & Agent Hint ready (${word?.category ?: state.selectedCategory}) — hidden from Impostor"
                        },
                        style = MaterialTheme.typography.titleSmall,
                        color = if (showLocationPreview) palette.normalGlow else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.testTag("lobby_location_word_text")
                    )
                }
            }

            // Add Player Row
            item {
                GlassSectionCard(
                    title = "ADD PLAYER (${state.players.size})",
                    icon = Icons.Filled.Group,
                    accentColor = palette.cyanAccent
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = newPlayerName,
                            onValueChange = { newPlayerName = it.take(24) },
                            label = { Text("Add Player") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        Button(
                            onClick = {
                                onAddPlayer(newPlayerName)
                                newPlayerName = ""
                            },
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.height(54.dp)
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = "Add player")
                        }
                    }
                }
            }
        }

        // Pinned Start Discussion Phase Transition CTA
        Surface(
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
            tonalElevation = 4.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(
                onClick = onStartDiscussion,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = palette.cyanAccent,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 10.dp)
                    .height(54.dp)
                    .testTag("start_discussion_phase_button")
            ) {
                Icon(Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                Text("Start Discussion Phase", style = MaterialTheme.typography.labelLarge)
            }
        }
    }

    if (avatarPickerPlayerId != null) {
        AvatarPickerSheet(
            players = state.players,
            initialPlayerId = avatarPickerPlayerId,
            onSelectAvatarForPlayer = { pid, avatarId, pose ->
                onSetPlayerAvatar(pid, avatarId, pose)
            },
            onDismiss = { avatarPickerPlayerId = null }
        )
    }
}

@Composable
private fun GlowingLobbyRoleCard(
    player: LocalPlayer,
    assignedRole: PlayerRole,
    secretWord: String?,
    agentHint: String?,
    isRevealed: Boolean,
    canRemove: Boolean,
    onToggleReveal: () -> Unit,
    onPickAvatar: () -> Unit = {},
    onRemove: () -> Unit
) {
    val palette = LocalImpostorPalette.current
    val identity = if (assignedRole == PlayerRole.IMPOSTOR) {
        GameRoleIdentity.IMPOSTOR
    } else {
        GameRoleIdentity.AGENT
    }
    val style = identity.style(palette)
    val cardShape = RoundedCornerShape(18.dp)

    val activeAccent by animateColorAsState(
        targetValue = if (isRevealed) style.accent else MaterialTheme.colorScheme.outline,
        animationSpec = tween(320),
        label = "lobby_card_accent"
    )
    val activeGlow by animateColorAsState(
        targetValue = if (isRevealed) style.glow else palette.cyanAccent.copy(alpha = 0.25f),
        animationSpec = tween(320),
        label = "lobby_card_glow"
    )
    val surfaceColor by animateColorAsState(
        targetValue = if (isRevealed) style.deepSurface else MaterialTheme.colorScheme.surfaceContainer,
        animationSpec = tween(320),
        label = "lobby_card_surface"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .distinctRoleGlow(
                isGlowing = isRevealed,
                glowColor = activeGlow,
                accentColor = activeAccent,
                shape = cardShape
            )
            .clip(cardShape)
            .background(surfaceColor)
            .clickable(onClick = onToggleReveal)
            .minimumInteractiveComponentSize()
            .testTag("lobby_role_card_${player.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val avatarChar = AvatarCatalog.findById(player.avatarId)
            Box(
                modifier = Modifier
                    .clickable(onClick = onPickAvatar)
                    .testTag("lobby_avatar_${player.id}")
            ) {
                CharacterAvatarThumbnail(
                    character = avatarChar,
                    pose = player.avatarPose,
                    size = 46.dp,
                    borderColor = if (isRevealed) style.glow else avatarChar.glowColor,
                    isHighlighted = isRevealed
                )
                if (isRevealed) {
                    RoleEmblemBadge(
                        identity = identity,
                        size = 20.dp,
                        animated = true,
                        modifier = Modifier.align(Alignment.BottomEnd)
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = player.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                AnimatedVisibility(visible = isRevealed) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = if (assignedRole == PlayerRole.IMPOSTOR) {
                                "ROLE: IMPOSTOR"
                            } else {
                                "ROLE: AGENT"
                            },
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = style.glow
                        )
                        if (assignedRole == PlayerRole.IMPOSTOR) {
                            Text(
                                text = "Secret Word: HIDDEN (Blend in!)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        } else {
                            Text(
                                text = buildString {
                                    append("Word: ${secretWord ?: "Pending"}")
                                    if (!agentHint.isNullOrBlank()) {
                                        append(" • Hint: $agentHint")
                                    }
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
                if (!isRevealed) {
                    Text(
                        text = "Role hidden — tap to reveal card",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (canRemove) {
                IconButton(
                    onClick = onRemove,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Remove ${player.name}",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * Custom Compose modifier applying a layered shadow, animated radial aura bloom,
 * scale pulse, and gradient border to visually highlight assigned player roles.
 */
fun Modifier.distinctRoleGlow(
    isGlowing: Boolean,
    glowColor: Color,
    accentColor: Color,
    shape: Shape = RoundedCornerShape(18.dp)
): Modifier = composed {
    val transition = rememberInfiniteTransition(label = "distinct_role_glow")
    val pulseAlpha by transition.animateFloat(
        initialValue = 0.34f,
        targetValue = 0.78f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1350, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_pulse_alpha"
    )
    val pulseScale by transition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.015f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1350, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_pulse_scale"
    )
    val shadowElevation by animateDpAsState(
        targetValue = if (isGlowing) 16.dp else 0.dp,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "glow_shadow_elevation"
    )

    this
        .graphicsLayer {
            if (isGlowing) {
                scaleX = pulseScale
                scaleY = pulseScale
            }
        }
        .shadow(
            elevation = shadowElevation,
            shape = shape,
            ambientColor = glowColor,
            spotColor = glowColor
        )
        .drawBehind {
            if (isGlowing) {
                val spread = 14.dp.toPx()
                drawRoundRect(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            glowColor.copy(alpha = pulseAlpha * 0.60f),
                            accentColor.copy(alpha = pulseAlpha * 0.25f),
                            Color.Transparent
                        ),
                        radius = maxOf(size.width, size.height) * 0.74f
                    ),
                    cornerRadius = CornerRadius(22.dp.toPx(), 22.dp.toPx()),
                    size = size.copy(width = size.width + spread, height = size.height + spread),
                    topLeft = Offset(-spread / 2f, -spread / 2f)
                )
            }
        }
        .border(
            width = if (isGlowing) 2.dp else 1.dp,
            brush = Brush.linearGradient(
                colors = if (isGlowing) {
                    listOf(
                        glowColor.copy(alpha = pulseAlpha.coerceAtLeast(0.7f)),
                        accentColor.copy(alpha = 0.9f),
                        glowColor.copy(alpha = pulseAlpha.coerceAtLeast(0.7f))
                    )
                } else {
                    listOf(accentColor.copy(alpha = 0.45f), accentColor.copy(alpha = 0.45f))
                }
            ),
            shape = shape
        )
}

@Composable
private fun DiscussionPhaseView(
    state: GameState,
    onToggleTimerPause: () -> Unit,
    onNextTurn: () -> Unit,
    onTriggerEmergencyMeeting: (String?, String) -> Unit,
    onStartVoting: () -> Unit,
    onReturnToLobby: () -> Unit
) {
    val palette = LocalImpostorPalette.current
    val context = LocalContext.current
    val speakerName = state.activeTurnPlayer?.name ?: "Discussion"
    val isPaused = state.turnStatus == TurnStatus.PAUSED
    var peekPlayer by remember { mutableStateOf<LocalPlayer?>(null) }
    var showReporterDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            GlassSectionCard(
                title = "DISCUSSION • TURN ${state.currentTurnIndex + 1} OF ${state.turnOrderIds.size.coerceAtLeast(1)}",
                icon = Icons.Filled.PlayArrow,
                accentColor = palette.cyanAccent
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    androidx.compose.runtime.key(state.currentTurnIndex) {
                        CircularCountdownTimer(
                            remainingSeconds = state.currentTimerCount,
                            totalSeconds = state.turnDurationSeconds,
                            isPaused = isPaused,
                            size = 156.dp,
                            strokeWidth = 11.dp,
                            onTimeExpired = {
                                TurnTimerEventBus.emitExpired(
                                    context = context,
                                    playerName = speakerName,
                                    turnIndex = state.currentTurnIndex,
                                    totalTurns = state.turnOrderIds.size
                                )
                            }
                        )
                    }

                    Text(
                        text = speakerName,
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Surface(
                        color = when (state.turnStatus) {
                            TurnStatus.EXPIRED -> palette.impostorSurface
                            TurnStatus.PAUSED -> palette.timerSurface
                            else -> palette.cyanSurface
                        },
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = state.turnStatus.label.uppercase(),
                            style = MaterialTheme.typography.labelMedium,
                            color = when (state.turnStatus) {
                                TurnStatus.EXPIRED -> palette.impostorGlow
                                TurnStatus.PAUSED -> palette.timerAccent
                                else -> palette.cyanAccent
                            },
                            modifier = Modifier
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                .testTag("discussion_turn_status_badge")
                        )
                    }

                    AnimatedVisibility(visible = state.turnStatus == TurnStatus.EXPIRED) {
                        Surface(
                            color = palette.impostorSurface.copy(alpha = 0.9f),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, palette.impostorGlow),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.NotificationsActive,
                                    contentDescription = "Time expired alert",
                                    tint = palette.impostorGlow,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "TIME EXPIRED FOR $speakerName — ADVANCING...",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = palette.impostorGlow
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = onToggleTimerPause,
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                        ) {
                            Icon(
                                imageVector = if (isPaused) Icons.Filled.PlayArrow else Icons.Filled.Pause,
                                contentDescription = null,
                                modifier = Modifier.padding(end = 6.dp)
                            )
                            Text(if (isPaused) "Resume" else "Pause")
                        }
                        Button(
                            onClick = onNextTurn,
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("discussion_next_turn_button")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.SkipNext,
                                contentDescription = null,
                                modifier = Modifier.padding(end = 6.dp)
                            )
                            Text("Next Turn")
                        }
                    }
                }
            }

            // Private Word & Agent Hint Reference during Discussion
            GlassSectionCard(
                title = "PRIVATE WORD REFERENCE",
                icon = Icons.Filled.LocationOn,
                accentColor = palette.normalAccent
            ) {
                Text(
                    text = "Agents may tap their name privately to view the secret word & hint (${state.locationWord?.category ?: state.selectedCategory}).",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    state.players.forEach { player ->
                        OutlinedButton(
                            onClick = { peekPlayer = player },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("discussion_peek_${player.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Visibility,
                                contentDescription = "Peek ${player.name}'s location word",
                                tint = palette.normalGlow,
                                modifier = Modifier
                                    .padding(end = 6.dp)
                                    .size(16.dp)
                            )
                            Text(player.name)
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
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = { showReporterDialog = true },
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = palette.timerAccent,
                        contentColor = Color.Black
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("emergency_meeting_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Campaign,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = "Call Emergency Meeting / Report",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onReturnToLobby,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Text("Lobby")
                    }
                    Button(
                        onClick = onStartVoting,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = palette.impostorAccent,
                            contentColor = Color.White
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("go_to_voting_phase_button")
                    ) {
                        Icon(Icons.Filled.HowToVote, contentDescription = null, modifier = Modifier.padding(end = 6.dp))
                        Text("Voting Phase")
                    }
                }
            }
        }
    }

    val target = peekPlayer
    if (target != null) {
        val role = state.playerRoles[target.id] ?: PlayerRole.NORMAL
        val word = state.referenceWordFor(target.id)
        val agentHint = state.referenceHintFor(target.id)
        val identity = if (role == PlayerRole.IMPOSTOR) GameRoleIdentity.IMPOSTOR else GameRoleIdentity.AGENT
        val style = identity.style(palette)
        AlertDialog(
            onDismissRequest = { peekPlayer = null },
            title = { Text("${target.name}'s Reference") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        RoleEmblemBadge(identity = identity, size = 40.dp, animated = true)
                        Text(
                            text = if (role == PlayerRole.IMPOSTOR) "IMPOSTOR" else "AGENT",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = style.glow
                        )
                    }
                    if (word != null) {
                        Text(
                            text = "Secret Word: $word",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = palette.normalGlow
                        )
                        Text(
                            text = buildString {
                                append("Category: ${state.locationWord?.category ?: state.selectedCategory}")
                                append(" • Genre: ${state.locationWord?.genre ?: ""}")
                                if (!agentHint.isNullOrBlank()) {
                                    append("\nAgent Hint: $agentHint")
                                }
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Surface(
                            color = style.accent.copy(alpha = 0.16f),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, style.glow.copy(alpha = 0.6f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Lock,
                                    contentDescription = "Hidden word",
                                    tint = style.glow,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "Secret Word Hidden",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = style.glow
                                )
                            }
                        }
                        Text(
                            text = "You are the Impostor! Listen closely to Agents' clues to deduce the word.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            confirmButton = {
                Button(onClick = { peekPlayer = null }) {
                    Text("Hide")
                }
            }
        )
    }

    if (showReporterDialog) {
        AlertDialog(
            onDismissRequest = { showReporterDialog = false },
            title = { Text("Initiate Emergency Meeting") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Select who is calling the emergency report:",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    state.players.forEach { player ->
                        OutlinedButton(
                            onClick = {
                                showReporterDialog = false
                                onTriggerEmergencyMeeting(player.id, "Suspicious clue reported during discussion!")
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("report_player_${player.id}")
                        ) {
                            Text("Report as ${player.name}")
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showReporterDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
