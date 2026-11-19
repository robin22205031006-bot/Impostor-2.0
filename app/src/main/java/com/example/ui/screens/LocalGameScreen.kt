package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.audio.SoundCuePlayer
import com.example.data.GamePhase
import com.example.data.PlayerRole
import com.example.ui.components.EliminatedPlayerReveal
import com.example.ui.components.EliminationRevealOverlay
import com.example.ui.components.EmergencyMeetingOverlay
import com.example.ui.components.GameRoleIdentity
import com.example.ui.components.ImpostorAnimatedBackdrop
import com.example.ui.components.PulsingStatusDot
import com.example.ui.components.SettingsMenuSheet
import com.example.ui.components.style
import com.example.ui.theme.LocalImpostorPalette
import com.example.viewmodel.GameViewModel
import com.example.viewmodel.LocalGameUiState
import com.example.viewmodel.LocalGameViewModel
import com.example.viewmodel.LocalPhase
import com.example.viewmodel.SetupWizardStep

@Composable
fun LocalGameScreen(
    viewModel: LocalGameViewModel,
    gameViewModel: GameViewModel = androidx.lifecycle.viewmodel.compose.viewModel(key = "tactical_game_lobby")
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val lobbyState by gameViewModel.gameState.collectAsStateWithLifecycle()
    val soundEnabled by SoundCuePlayer.soundEnabled.collectAsStateWithLifecycle()
    val emergencySoundEnabled by SoundCuePlayer.emergencyAlertSoundEnabled.collectAsStateWithLifecycle()
    val palette = LocalImpostorPalette.current
    var dismissedEliminationRound by rememberSaveable { mutableIntStateOf(0) }
    var showConfirmExitDialog by remember { mutableStateOf(false) }
    var showSettingsSheet by remember { mutableStateOf(false) }
    var showRoleLobby by rememberSaveable { mutableStateOf(false) }

    val isRoundInProgress = state.phase == LocalPhase.REVEAL ||
        state.phase == LocalPhase.PLAYING ||
        state.phase == LocalPhase.VOTING

    BackHandler(enabled = state.phase != LocalPhase.SETUP || (showRoleLobby && lobbyState.phase == GamePhase.LOBBY)) {
        if (state.phase == LocalPhase.SETUP && showRoleLobby) {
            showRoleLobby = false
        } else if (isRoundInProgress) {
            showConfirmExitDialog = true
        } else {
            viewModel.returnToSetup(SetupWizardStep.PACKS)
        }
    }

    val revealPlayer = state.players.getOrNull(state.revealPlayerIndex)
    val revealedIdentity = if (state.phase == LocalPhase.REVEAL && state.isRoleCardVisible && revealPlayer != null) {
        GameRoleIdentity.resolve(
            role = viewModel.roleFor(revealPlayer.id),
            isUltimateKiller = state.impostorAbilityEnabled && state.ultimateKillerId == revealPlayer.id,
            isSavior = state.saviorAgentEnabled && state.saviorAgentId == revealPlayer.id,
            isDirector = state.agentDirectorEnabled && state.agentDirectorId == revealPlayer.id
        )
    } else null

    val isTimerUrgentOrExpired = state.phase == LocalPhase.PLAYING &&
        state.timerEnabled &&
        !state.isTimerPaused &&
        state.remainingTurnSeconds <= 5

    val (orbPrimary, orbSecondary, orbTertiary) = when {
        revealedIdentity != null -> {
            val s = revealedIdentity.style(palette)
            Triple(s.glow, s.accent, s.deepSurface)
        }
        isTimerUrgentOrExpired -> Triple(palette.impostorGlow, palette.impostorAccent, palette.goldAccent)
        state.phase == LocalPhase.VOTING -> Triple(palette.impostorAccent, palette.goldAccent, palette.directorAccent)
        state.phase == LocalPhase.RESULTS -> {
            val winAccent = if (state.crewWon) palette.normalGlow else palette.impostorGlow
            Triple(winAccent, palette.goldAccent, if (state.crewWon) palette.normalAccent else palette.impostorAccent)
        }
        else -> Triple(palette.goldAccent, palette.normalAccent, palette.impostorAccent)
    }

    ImpostorAnimatedBackdrop(
        primaryOrbColor = orbPrimary,
        secondaryOrbColor = orbSecondary,
        tertiaryOrbColor = orbTertiary
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .widthIn(max = 640.dp)
                .align(Alignment.TopCenter)
        ) {
            // Show shared top bar ONLY in Role Lobby or active game phases; Setup wizard renders its own clean video-matched chrome
            if (state.phase != LocalPhase.SETUP || showRoleLobby) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        IconButton(
                            onClick = {
                                if (state.phase == LocalPhase.SETUP && showRoleLobby) {
                                    showRoleLobby = false
                                } else if (isRoundInProgress) {
                                    showConfirmExitDialog = true
                                } else {
                                    viewModel.returnToSetup(SetupWizardStep.PACKS)
                                }
                            },
                            modifier = Modifier
                                .size(48.dp)
                                .testTag(if (showRoleLobby && state.phase == LocalPhase.SETUP) "tab_pass_and_play" else "local_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        }
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                PulsingStatusDot(color = palette.goldAccent)
                                Text(
                                    text = if (showRoleLobby && state.phase == LocalPhase.SETUP) "ROLE LOBBY" else "ROUND ${state.roundNumber}",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                            }
                            Text(
                                text = if (showRoleLobby && state.phase == LocalPhase.SETUP) {
                                    "${lobbyState.phase.title} • ${lobbyState.players.size} Players"
                                } else {
                                    "${state.gameMode.title} • ${state.players.size} Players"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = palette.goldAccent
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        IconButton(
                            onClick = { showSettingsSheet = true },
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("settings_menu_button")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Settings,
                                contentDescription = "Open settings menu",
                                tint = palette.goldAccent
                            )
                        }
                        IconButton(
                            onClick = SoundCuePlayer::toggleSound,
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("local_sound_toggle_button")
                        ) {
                            Icon(
                                imageVector = if (soundEnabled) Icons.AutoMirrored.Filled.VolumeUp else Icons.AutoMirrored.Filled.VolumeOff,
                                contentDescription = if (soundEnabled) "Mute sound cues" else "Enable sound cues",
                                tint = if (soundEnabled) palette.goldAccent else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (state.phase != LocalPhase.SETUP) {
                            TextButton(
                                onClick = {
                                    if (isRoundInProgress) {
                                        showConfirmExitDialog = true
                                    } else {
                                        viewModel.returnToSetup(SetupWizardStep.PACKS)
                                    }
                                },
                                modifier = Modifier.testTag("local_end_round_button")
                            ) {
                                Text("Quit", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }

            if (state.errorBanner != null) {
                Surface(
                    color = palette.impostorSurface,
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, palette.impostorAccent),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = state.errorBanner ?: "",
                            style = MaterialTheme.typography.bodyMedium,
                            color = palette.impostorGlow,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = viewModel::clearError, modifier = Modifier.size(36.dp)) {
                            Icon(Icons.Filled.Close, contentDescription = "Dismiss", tint = palette.impostorGlow)
                        }
                    }
                }
            }

            AnimatedContent(
                targetState = state.phase,
                transitionSpec = {
                    (fadeIn(animationSpec = tween(260)) +
                        slideInVertically(animationSpec = tween(260)) { it / 16 } +
                        scaleIn(initialScale = 0.98f, animationSpec = tween(260)))
                        .togetherWith(fadeOut(animationSpec = tween(180)))
                        .using(SizeTransform(clip = false))
                },
                label = "local_phase_transition",
                modifier = Modifier.weight(1f)
            ) { phase ->
                when (phase) {
                    LocalPhase.SETUP -> {
                        if (showRoleLobby) {
                            GameLobbyScreen(viewModel = gameViewModel)
                        } else {
                            GameSetupScreen(
                                state = state,
                                onStepChange = viewModel::setSetupStep,
                                onOpenRoleLobby = { showRoleLobby = true },
                                onToggleFavoritePack = viewModel::toggleFavoritePack,
                                onOpenSettings = { showSettingsSheet = true },
                                soundEnabled = soundEnabled,
                                onToggleSound = SoundCuePlayer::toggleSound,
                                onAddPlayer = viewModel::addPlayer,
                                onRenamePlayer = viewModel::renamePlayer,
                                onRemovePlayer = viewModel::removePlayer,
                                onSetPlayerCount = viewModel::setPlayerCount,
                                onSetPlayerAvatar = viewModel::setPlayerAvatar,
                                onCyclePlayerPose = viewModel::cyclePlayerPose,
                                onSelectMode = viewModel::setGameMode,
                                onSetImpostors = viewModel::setImpostorCount,
                                onSetDuration = viewModel::setTurnDuration,
                                onSelectCategory = viewModel::setCategory,
                                onToggleHints = viewModel::setHintsEnabled,
                                onToggleCategoryClues = viewModel::setCategoryCluesEnabled,
                                onToggleTimer = viewModel::setTimerEnabled,
                                onToggleImpostorAbility = viewModel::setImpostorAbilityEnabled,
                                onToggleSaviorAgent = viewModel::setSaviorAgentEnabled,
                                onToggleAgentDirector = viewModel::setAgentDirectorEnabled,
                                onToggleAgentPrediction = viewModel::setAgentPredictionEnabled,
                                onStartRound = viewModel::startRound
                            )
                        }
                    }

                    LocalPhase.REVEAL -> LocalRevealContent(
                        state = state,
                        roleForPlayer = viewModel::roleFor,
                        onSelectKillTarget = viewModel::setNullifiedVoter,
                        onSelectProtectedTarget = viewModel::setProtectedPlayer,
                        onSelectPrediction = viewModel::setAgentPrediction,
                        onReshuffleRoles = viewModel::reshuffleSessionRoles,
                        onReveal = viewModel::showCurrentPlayerCard,
                        onHideAndNext = viewModel::hideAndAdvanceReveal
                    )

                    LocalPhase.PLAYING -> LocalPlayingContent(
                        state = state,
                        roleForPlayer = viewModel::roleFor,
                        onTogglePause = viewModel::toggleTimerPause,
                        onAddSeconds = { viewModel.addExtraSeconds(10) },
                        onNextTurn = viewModel::nextTurn,
                        onUpdateGuess = viewModel::updateImpostorWordGuess,
                        onSelectKillTarget = viewModel::setNullifiedVoter,
                        onSelectProtectedTarget = viewModel::setProtectedPlayer,
                        onSelectPrediction = viewModel::setAgentPrediction,
                        onTriggerEmergencyMeeting = {
                            viewModel.triggerEmergencyMeeting(
                                reason = "Suspicious clue reported during discussion!"
                            )
                        },
                        onGoToVoting = viewModel::goToVoting
                    )

                    LocalPhase.VOTING -> LocalVotingContent(
                        state = state,
                        onRevealBallot = viewModel::revealVoterBallot,
                        onCastVote = viewModel::castLocalVote,
                        onConfirmVoteAndNext = viewModel::confirmVoteAndAdvance,
                        onUpdateGuess = viewModel::updateImpostorWordGuess,
                        onSubmitResults = viewModel::submitResults
                    )

                    LocalPhase.RESULTS -> LocalResultsContent(
                        state = state,
                        onNextRound = viewModel::startRound,
                        onReturnSetup = { viewModel.returnToSetup(SetupWizardStep.PACKS) }
                    )
                }
            }
        }

        val showEliminationOverlay =
            state.phase == LocalPhase.RESULTS && dismissedEliminationRound != state.roundNumber
        AnimatedVisibility(
            visible = showEliminationOverlay,
            enter = fadeIn(animationSpec = tween(220)),
            exit = fadeOut(animationSpec = tween(220))
        ) {
            val topVotes = state.roundVoteCounts.values.maxOrNull() ?: 0
            val eliminatedPlayers = remember(state.roundNumber, state.roundVoteCounts, state.players, state.impostorIds, state.ultimateKillerId, state.saviorAgentId, state.agentDirectorId) {
                if (topVotes > 0) {
                    state.roundVoteCounts.filterValues { it == topVotes }.keys.mapNotNull { pid ->
                        state.players.firstOrNull { it.id == pid }?.let { player ->
                            val isImp = player.id in state.impostorIds
                            val identity = GameRoleIdentity.resolve(
                                role = if (isImp) PlayerRole.IMPOSTOR else PlayerRole.NORMAL,
                                isUltimateKiller = state.impostorAbilityEnabled && player.id == state.ultimateKillerId,
                                isSavior = state.saviorAgentEnabled && player.id == state.saviorAgentId,
                                isDirector = state.agentDirectorEnabled && player.id == state.agentDirectorId
                            )
                            EliminatedPlayerReveal(
                                id = player.id,
                                name = player.name,
                                isImpostor = isImp,
                                voteCount = topVotes,
                                roleIdentity = identity,
                                avatarId = player.avatarId,
                                avatarPose = player.avatarPose
                            )
                        }
                    }
                } else {
                    emptyList()
                }
            }
            androidx.compose.runtime.key(state.roundNumber) {
                EliminationRevealOverlay(
                    eliminatedPlayers = eliminatedPlayers,
                    crewWon = state.crewWon,
                    impostorGuessCorrect = state.impostorGuessCorrect,
                    onRevealVerdict = { SoundCuePlayer.playVotingResultsCue(state.crewWon) },
                    onDismiss = { dismissedEliminationRound = state.roundNumber }
                )
            }
        }

        AnimatedVisibility(
            visible = state.phase == LocalPhase.PLAYING && state.emergencyMeeting.isActive,
            enter = fadeIn(animationSpec = tween(180)),
            exit = fadeOut(animationSpec = tween(180))
        ) {
            EmergencyMeetingOverlay(
                alert = state.emergencyMeeting,
                onProceedNow = viewModel::dismissEmergencyMeetingAndVote
            )
        }

        if (showSettingsSheet) {
            val activeTimerDuration = if (state.phase == LocalPhase.SETUP && showRoleLobby) {
                lobbyState.turnDurationSeconds
            } else {
                state.turnDurationSeconds
            }
            SettingsMenuSheet(
                emergencyAlertSoundEnabled = emergencySoundEnabled,
                soundEffectsEnabled = soundEnabled,
                discussionDurationSeconds = activeTimerDuration,
                onToggleEmergencyAlertSound = { enabled ->
                    gameViewModel.setEmergencyAlertSoundEnabled(enabled)
                },
                onToggleSoundEffects = { enabled ->
                    gameViewModel.setSoundEffectsEnabled(enabled)
                },
                onChangeDiscussionDuration = { seconds ->
                    gameViewModel.setTurnDuration(seconds)
                    viewModel.setTurnDuration(seconds)
                },
                onDismiss = { showSettingsSheet = false }
            )
        }

        if (showConfirmExitDialog) {
            AlertDialog(
                onDismissRequest = { showConfirmExitDialog = false },
                title = { Text("Quit Current Round?") },
                text = { Text("Returning to setup will end the active round in progress.") },
                confirmButton = {
                    Button(
                        onClick = {
                            showConfirmExitDialog = false
                            viewModel.returnToSetup(SetupWizardStep.PACKS)
                        }
                    ) {
                        Text("Quit Round")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showConfirmExitDialog = false }) {
                        Text("Keep Playing")
                    }
                }
            )
        }
    }
}

@Composable
private fun LocalRevealContent(
    state: LocalGameUiState,
    roleForPlayer: (String) -> PlayerRole,
    onSelectKillTarget: (String?) -> Unit,
    onSelectProtectedTarget: (String?) -> Unit,
    onSelectPrediction: (String, String?) -> Unit,
    onReshuffleRoles: () -> Unit,
    onReveal: () -> Unit,
    onHideAndNext: () -> Unit
) {
    val player = state.players.getOrNull(state.revealPlayerIndex) ?: return
    val wordEntry = state.activeWord ?: return
    val role = roleForPlayer(player.id)
    val briefing = state.wordBriefingFor(player.id)
    val otherPlayers = remember(state.players, player.id) {
        state.players.filter { it.id != player.id }.map { it.id to it.name }
    }
    val impCount = state.impostorIds.size.coerceAtLeast(1)
    val agentCount = (state.players.size - impCount).coerceAtLeast(1)

    androidx.compose.runtime.key(player.id, state.roundNumber) {
        RoleRevealScreen(
            playerName = player.name,
            playerIndex = state.revealPlayerIndex,
            totalPlayers = state.players.size,
            turnDurationSeconds = state.turnDurationSeconds,
            role = role,
            category = wordEntry.category,
            genre = wordEntry.genre,
            secretWord = briefing?.secretWord.orEmpty(),
            hint = briefing?.agentHint.orEmpty(),
            isRevealed = state.isRoleCardVisible,
            showCategoryAndGenre = state.categoryCluesEnabled,
            showHint = state.hintsEnabled,
            timerEnabled = state.timerEnabled,
            impostorAbilityEnabled = state.impostorAbilityEnabled,
            isUltimateKiller = state.ultimateKillerId == player.id,
            saviorAgentEnabled = state.saviorAgentEnabled,
            isSavior = state.saviorAgentId == player.id,
            agentDirectorEnabled = state.agentDirectorEnabled,
            isDirector = state.agentDirectorId == player.id,
            agentPredictionEnabled = state.agentPredictionEnabled,
            otherPlayers = otherPlayers,
            selectedKillTargetId = state.nullifiedVoterId,
            selectedProtectedId = state.protectedPlayerId,
            selectedPredictionId = state.agentPredictions[player.id],
            onSelectKillTarget = onSelectKillTarget,
            onSelectProtectedTarget = onSelectProtectedTarget,
            onSelectPrediction = { suspectId -> onSelectPrediction(player.id, suspectId) },
            onReveal = onReveal,
            onHideAndNext = onHideAndNext,
            impostorCount = impCount,
            agentCount = agentCount,
            onReshuffleRoles = onReshuffleRoles,
            avatarId = player.avatarId,
            avatarPose = player.avatarPose
        )
    }
}
