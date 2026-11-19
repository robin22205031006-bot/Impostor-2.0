package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.AvatarCatalog
import com.example.data.LocalPlayer
import com.example.data.PlayerRole
import com.example.ui.components.AgentDirectorBadge
import com.example.ui.components.AgentPredictionCard
import com.example.ui.components.CharacterAvatarThumbnail
import com.example.ui.components.CircularCountdownTimer
import com.example.ui.components.FloatingHeroSprite
import com.example.ui.components.GameRoleIdentity
import com.example.ui.components.GoldCtaButton
import com.example.ui.components.HighlightedHeadline
import com.example.ui.components.OtherImpostorAllyNote
import com.example.ui.components.RoleEmblemBadge
import com.example.ui.components.RoleWordCard
import com.example.ui.components.SaviorAbilityCard
import com.example.ui.components.TurnExpiredBroadcast
import com.example.ui.components.TurnTimerEventBus
import com.example.ui.components.UltimateKillerAbilityCard
import com.example.ui.components.style
import com.example.ui.theme.LocalImpostorPalette
import com.example.viewmodel.LocalGameUiState
import kotlinx.coroutines.flow.collectLatest

@Composable
internal fun LocalPlayingContent(
    state: LocalGameUiState,
    roleForPlayer: (String) -> PlayerRole,
    onTogglePause: () -> Unit,
    onAddSeconds: () -> Unit,
    onNextTurn: () -> Unit,
    onUpdateGuess: (String) -> Unit,
    onSelectKillTarget: (String?) -> Unit,
    onSelectProtectedTarget: (String?) -> Unit,
    onSelectPrediction: (String, String?) -> Unit,
    onTriggerEmergencyMeeting: () -> Unit,
    onGoToVoting: () -> Unit
) {
    val palette = LocalImpostorPalette.current
    val context = LocalContext.current
    val activeUid = state.turnOrderIds.getOrNull(state.currentTurnIndex)
    val activePlayer = state.players.firstOrNull { it.id == activeUid } ?: state.players.firstOrNull()
    val nextUid = if (state.turnOrderIds.isNotEmpty()) {
        state.turnOrderIds[(state.currentTurnIndex + 1) % state.turnOrderIds.size]
    } else null
    val nextPlayer = state.players.firstOrNull { it.id == nextUid }
        ?: state.players.lastOrNull { it.id != activePlayer?.id }
        ?: activePlayer

    var peekPlayer by remember { mutableStateOf<LocalPlayer?>(null) }
    var showGuessInput by remember { mutableStateOf(false) }
    var lastBroadcast by remember { mutableStateOf<TurnExpiredBroadcast?>(null) }
    var prevRemaining by remember(state.currentTurnIndex, state.roundNumber) {
        mutableIntStateOf(state.remainingTurnSeconds)
    }

    LaunchedEffect(Unit) {
        TurnTimerEventBus.events.collectLatest { event ->
            lastBroadcast = event
        }
    }

    LaunchedEffect(
        state.remainingTurnSeconds,
        state.isTimerPaused,
        state.timerEnabled,
        state.currentTurnIndex,
        state.roundNumber
    ) {
        if (state.timerEnabled && !state.isTimerPaused && prevRemaining > 0 && state.remainingTurnSeconds == 0) {
            TurnTimerEventBus.emitExpired(
                context = context,
                playerName = activePlayer?.name ?: "Discussion",
                turnIndex = state.currentTurnIndex,
                totalTurns = state.turnOrderIds.size
            )
        }
        prevRemaining = state.remainingTurnSeconds
    }

    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "CLUES FIRST. ACCUSATIONS SECOND.",
                        style = MaterialTheme.typography.labelMedium,
                        color = palette.goldAccent,
                        fontWeight = FontWeight.Bold
                    )
                    HighlightedHeadline(
                        prefix = "Let the ",
                        highlighted = "bluffing",
                        suffix = " begin.",
                        textAlign = TextAlign.Start
                    )
                }
            }

            // Central Countdown Arena Flanked by Opposing Transparent Heroes
            item {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            if (activePlayer != null) {
                                FloatingHeroSprite(
                                    character = AvatarCatalog.findById(activePlayer.avatarId),
                                    pose = activePlayer.avatarPose,
                                    size = 108.dp,
                                    bobOffsetDp = 4f,
                                    isFocal = true,
                                    modifier = Modifier.weight(0.9f)
                                )
                            }

                            Column(
                                modifier = Modifier.weight(1.25f),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                if (state.timerEnabled) {
                                    androidx.compose.runtime.key(state.currentTurnIndex, state.roundNumber) {
                                        CircularCountdownTimer(
                                            remainingSeconds = state.remainingTurnSeconds,
                                            totalSeconds = state.turnDurationSeconds,
                                            isPaused = state.isTimerPaused,
                                            size = 112.dp,
                                            strokeWidth = 9.dp
                                        )
                                    }
                                } else {
                                    Surface(
                                        color = palette.goldSurface,
                                        shape = RoundedCornerShape(16.dp),
                                        border = BorderStroke(1.dp, palette.goldAccent.copy(alpha = 0.7f))
                                    ) {
                                        Text(
                                            text = "FREE TALK",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = palette.goldAccent,
                                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = activePlayer?.name ?: "Speaker",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = palette.goldAccent,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "Turn ${state.currentTurnIndex + 1} / ${state.turnOrderIds.size.coerceAtLeast(1)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            if (nextPlayer != null) {
                                FloatingHeroSprite(
                                    character = AvatarCatalog.findById(nextPlayer.avatarId),
                                    pose = nextPlayer.avatarPose,
                                    size = 108.dp,
                                    bobOffsetDp = 4f,
                                    phaseOffset = 1.8f,
                                    isFocal = false,
                                    modifier = Modifier.weight(0.9f)
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (state.timerEnabled) {
                                OutlinedButton(
                                    onClick = onTogglePause,
                                    shape = RoundedCornerShape(14.dp),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                                    contentPadding = PaddingValues(horizontal = 10.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                ) {
                                    Icon(
                                        imageVector = if (state.isTimerPaused) Icons.Filled.PlayArrow else Icons.Filled.Pause,
                                        contentDescription = if (state.isTimerPaused) "Resume timer" else "Pause timer",
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(if (state.isTimerPaused) "Resume" else "Pause", maxLines = 1)
                                }
                                OutlinedButton(
                                    onClick = onAddSeconds,
                                    shape = RoundedCornerShape(14.dp),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                                    contentPadding = PaddingValues(horizontal = 8.dp),
                                    modifier = Modifier
                                        .weight(0.75f)
                                        .height(48.dp)
                                ) {
                                    Text("+10s", maxLines = 1)
                                }
                            }
                            Button(
                                onClick = onNextTurn,
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = palette.goldSurface,
                                    contentColor = palette.goldAccent
                                ),
                                contentPadding = PaddingValues(horizontal = 10.dp),
                                modifier = Modifier
                                    .weight(1.1f)
                                    .height(48.dp)
                                    .testTag("local_next_turn_button")
                            ) {
                                Text(
                                    text = if (state.currentTurnIndex + 1 >= state.turnOrderIds.size) "Finish Turns" else "Next Turn",
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }

            if (lastBroadcast != null && state.timerEnabled && state.remainingTurnSeconds == 0 && !state.isTimerPaused) {
                item {
                    val broadcast = lastBroadcast!!
                    Surface(
                        color = palette.impostorSurface,
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, palette.impostorGlow),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.NotificationsActive,
                                contentDescription = "Turn timer expired alert",
                                tint = palette.impostorGlow,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Time's up for ${broadcast.playerName} — tap Next Turn or ready to vote!",
                                style = MaterialTheme.typography.labelMedium,
                                color = palette.impostorGlow,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // Table Briefing + Optional Category/Genre Info
            item {
                val wordEntry = state.activeWord
                if (wordEntry != null) {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceContainer,
                        shape = RoundedCornerShape(18.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            if (state.categoryCluesEnabled) {
                                Text(
                                    text = "${wordEntry.category.uppercase()} • ${wordEntry.genre.uppercase()}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = palette.goldAccent,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = "Give one honest or clever clue on your turn. Impostors listen to blend in or guess the word.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Horizontal Private Card Peek Row
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "FORGOT YOUR CARD? TAP YOUR AVATAR PRIVATELY",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        state.players.forEach { player ->
                            val avatarChar = AvatarCatalog.findById(player.avatarId)
                            Surface(
                                onClick = { peekPlayer = player },
                                color = MaterialTheme.colorScheme.surfaceContainer,
                                shape = RoundedCornerShape(16.dp),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    CharacterAvatarThumbnail(
                                        character = avatarChar,
                                        pose = player.avatarPose,
                                        size = 34.dp,
                                        borderColor = palette.goldAccent
                                    )
                                    Text(
                                        text = player.name,
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Icon(
                                        imageVector = Icons.Filled.Visibility,
                                        contentDescription = "Peek ${player.name}'s card",
                                        tint = palette.goldAccent,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Optional Impostor Blend-in Guess
            item {
                Surface(
                    color = palette.impostorSurface.copy(alpha = 0.65f),
                    shape = RoundedCornerShape(18.dp),
                    border = BorderStroke(1.dp, palette.impostorAccent.copy(alpha = 0.7f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "IMPOSTOR WORD GUESS",
                                style = MaterialTheme.typography.labelMedium,
                                color = palette.impostorGlow
                            )
                            TextButton(onClick = { showGuessInput = !showGuessInput }) {
                                Text(if (showGuessInput) "Hide" else "Enter Guess", color = palette.impostorGlow)
                            }
                        }
                        AnimatedVisibility(visible = showGuessInput) {
                            OutlinedTextField(
                                value = state.impostorWordGuess,
                                onValueChange = onUpdateGuess,
                                label = { Text("Secret Word Guess") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }

            item {
                Button(
                    onClick = onTriggerEmergencyMeeting,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = palette.impostorAccent,
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("local_emergency_meeting_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Campaign,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text("Call Emergency Meeting", style = MaterialTheme.typography.labelLarge)
                }
            }
        }

        // Sticky Video-Matched "We're ready to vote" Gold CTA Footer
        Surface(
            color = MaterialTheme.colorScheme.background.copy(alpha = 0.96f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                GoldCtaButton(
                    text = "We're ready to vote",
                    onClick = onGoToVoting,
                    modifier = Modifier.testTag("local_go_to_voting_button")
                )
            }
        }
    }

    val target = peekPlayer
    val wordEntry = state.activeWord
    if (target != null && wordEntry != null) {
        val targetRole = roleForPlayer(target.id)
        val targetIdentity = GameRoleIdentity.resolve(
            role = targetRole,
            isUltimateKiller = state.impostorAbilityEnabled && state.ultimateKillerId == target.id,
            isSavior = state.saviorAgentEnabled && state.saviorAgentId == target.id,
            isDirector = state.agentDirectorEnabled && state.agentDirectorId == target.id
        )
        val otherPlayers = remember(state.players, target.id) {
            state.players.filter { it.id != target.id }.map { it.id to it.name }
        }
        AlertDialog(
            onDismissRequest = { peekPlayer = null },
            title = { Text("${target.name}'s Private Card") },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    RoleWordCard(
                        role = targetRole,
                        category = wordEntry.category,
                        genre = wordEntry.genre,
                        secretWord = wordEntry.word,
                        hint = state.activeHint,
                        showCategoryAndGenre = state.categoryCluesEnabled,
                        showHint = state.hintsEnabled,
                        roleIdentity = targetIdentity,
                        avatarId = target.avatarId,
                        avatarPose = target.avatarPose
                    )
                    if (state.impostorAbilityEnabled && targetRole == PlayerRole.IMPOSTOR) {
                        if (state.ultimateKillerId == target.id) {
                            UltimateKillerAbilityCard(
                                candidates = otherPlayers,
                                selectedTargetId = state.nullifiedVoterId,
                                onSelectTarget = onSelectKillTarget
                            )
                        } else {
                            OtherImpostorAllyNote()
                        }
                    }
                    if (state.agentDirectorEnabled && targetRole == PlayerRole.NORMAL && state.agentDirectorId == target.id) {
                        AgentDirectorBadge()
                    }
                    if (state.saviorAgentEnabled && targetRole == PlayerRole.NORMAL && state.saviorAgentId == target.id) {
                        SaviorAbilityCard(
                            candidates = otherPlayers,
                            selectedProtectedId = state.protectedPlayerId,
                            onSelectProtected = onSelectProtectedTarget
                        )
                    }
                    if (state.agentPredictionEnabled && targetRole == PlayerRole.NORMAL) {
                        AgentPredictionCard(
                            candidates = otherPlayers,
                            selectedSuspectId = state.agentPredictions[target.id],
                            onSelectSuspect = { suspectId -> onSelectPrediction(target.id, suspectId) }
                        )
                    }
                }
            },
            confirmButton = {
                Button(onClick = { peekPlayer = null }) { Text("Hide Card") }
            }
        )
    }
}

@Composable
internal fun LocalVotingContent(
    state: LocalGameUiState,
    onRevealBallot: () -> Unit,
    onCastVote: (String, String) -> Unit,
    onConfirmVoteAndNext: () -> Unit,
    onUpdateGuess: (String) -> Unit,
    onSubmitResults: () -> Unit
) {
    val palette = LocalImpostorPalette.current
    val allVoted = state.votingPlayerIndex >= state.players.size

    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Illustrated Suspect Lineup Hero Banner from reference style
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                        .clip(RoundedCornerShape(22.dp))
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_voting_table_banner),
                        contentDescription = "Suspect lineup around the table",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, Color(0xCC1D1626))
                                )
                            )
                    )
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(16.dp)
                    ) {
                        Text(
                            text = if (!allVoted) {
                                "PRIVATE VOTING (${state.votingPlayerIndex + 1} / ${state.players.size})"
                            } else {
                                "ALL ${state.players.size} VOTES LOCKED"
                            },
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = palette.goldAccent
                        )
                        HighlightedHeadline(
                            prefix = "Who is the ",
                            highlighted = "imposter?",
                            style = MaterialTheme.typography.headlineMedium,
                            textAlign = TextAlign.Start
                        )
                    }
                }
            }

            if (!allVoted) {
                val voter = state.players[state.votingPlayerIndex]
                val selectedSuspectId = state.votesByPlayerId[voter.id]
                val candidates = state.players.filter { it.id != voter.id }

                if (!state.isVoteCardRevealed) {
                    item {
                        Surface(
                            onClick = onRevealBallot,
                            color = MaterialTheme.colorScheme.surfaceContainer,
                            shape = RoundedCornerShape(24.dp),
                            border = BorderStroke(2.dp, palette.goldAccent.copy(alpha = 0.85f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("local_open_ballot_card")
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                FloatingHeroSprite(
                                    character = AvatarCatalog.findById(voter.avatarId),
                                    pose = voter.avatarPose,
                                    size = 132.dp,
                                    isFocal = true
                                )
                                Text(
                                    text = "PASS PHONE TO ${voter.name.uppercase()}",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = palette.goldAccent,
                                    textAlign = TextAlign.Center
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.TouchApp,
                                        contentDescription = null,
                                        tint = palette.goldAccent,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = "Tap to open secret ballot",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                } else {
                    item {
                        Text(
                            text = "${voter.name}, tap a suspect below to lock your vote:",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // 2-Column Suspect Grid with ButterGold active selection matching video
                    val rows = candidates.chunked(2)
                    items(rows) { rowPlayers ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            rowPlayers.forEach { candidate ->
                                val selected = selectedSuspectId == candidate.id
                                SuspectVoteTile(
                                    player = candidate,
                                    isSelected = selected,
                                    onClick = { onCastVote(voter.id, candidate.id) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("local_vote_player_${candidate.id}")
                                )
                            }
                            if (rowPlayers.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            } else {
                item {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceContainer,
                        shape = RoundedCornerShape(20.dp),
                        border = BorderStroke(1.2.dp, palette.goldAccent.copy(alpha = 0.65f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "IMPOSTOR'S LAST-CHANCE WORD GUESS (OPTIONAL)",
                                style = MaterialTheme.typography.labelMedium,
                                color = palette.goldAccent,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "If the voted-out impostor names the secret word, they steal the win!",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            OutlinedTextField(
                                value = state.impostorWordGuess,
                                onValueChange = onUpdateGuess,
                                label = { Text("Secret Word Guess") },
                                singleLine = true,
                                shape = RoundedCornerShape(14.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                                    focusedBorderColor = palette.goldAccent,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("local_impostor_guess_input")
                            )
                        }
                    }
                }
            }
        }

        // Sticky Voting Footer CTA
        if (!allVoted && state.isVoteCardRevealed) {
            val voter = state.players[state.votingPlayerIndex]
            val hasSelection = state.votesByPlayerId[voter.id] != null
            Surface(
                color = MaterialTheme.colorScheme.background.copy(alpha = 0.96f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
                    GoldCtaButton(
                        text = if (state.votingPlayerIndex >= state.players.size - 1) {
                            "Lock Final Vote"
                        } else {
                            "Lock Vote & Pass Phone"
                        },
                        enabled = hasSelection,
                        onClick = onConfirmVoteAndNext,
                        modifier = Modifier.testTag("local_lock_vote_button")
                    )
                }
            }
        } else if (allVoted) {
            Surface(
                color = MaterialTheme.colorScheme.background.copy(alpha = 0.96f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
                    GoldCtaButton(
                        text = "Reveal the imposter",
                        onClick = onSubmitResults,
                        containerColor = palette.impostorAccent,
                        contentColor = Color.White,
                        modifier = Modifier.testTag("local_reveal_results_button")
                    )
                }
            }
        }
    }
}

@Composable
private fun SuspectVoteTile(
    player: LocalPlayer,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalImpostorPalette.current
    val bg by animateColorAsState(
        targetValue = if (isSelected) palette.goldAccent else MaterialTheme.colorScheme.surfaceContainer,
        animationSpec = tween(200),
        label = "suspect_tile_bg"
    )
    val ink by animateColorAsState(
        targetValue = if (isSelected) palette.goldDarkInk else MaterialTheme.colorScheme.onSurface,
        animationSpec = tween(200),
        label = "suspect_tile_ink"
    )
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 0.97f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "suspect_tile_scale"
    )

    Surface(
        onClick = onClick,
        color = bg,
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) palette.goldGlow else MaterialTheme.colorScheme.outline
        ),
        modifier = modifier
            .height(104.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box {
                CharacterAvatarThumbnail(
                    character = AvatarCatalog.findById(player.avatarId),
                    pose = player.avatarPose,
                    size = 44.dp,
                    borderColor = if (isSelected) palette.goldDarkInk else palette.goldAccent,
                    isHighlighted = isSelected
                )
                if (isSelected) {
                    Surface(
                        color = palette.goldDarkInk,
                        shape = CircleShape,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(18.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = null,
                                tint = palette.goldAccent,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = player.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = ink,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
internal fun LocalResultsContent(
    state: LocalGameUiState,
    onNextRound: () -> Unit,
    onReturnSetup: () -> Unit
) {
    val palette = LocalImpostorPalette.current
    val wordEntry = state.activeWord
    val impostorPlayers = state.players.filter { it.id in state.impostorIds }
    val nameMap = remember(state.players) { state.players.associate { it.id to it.name } }

    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                val winnerIdentity = if (state.crewWon) GameRoleIdentity.AGENT else GameRoleIdentity.IMPOSTOR
                val winnerStyle = winnerIdentity.style(palette)

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "THE BIG REVEAL",
                        style = MaterialTheme.typography.labelMedium,
                        color = palette.goldAccent,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (state.crewWon) "BUSTED!" else "FOOLED YOU!",
                        style = MaterialTheme.typography.displayLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (state.crewWon) palette.normalGlow else palette.impostorGlow,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = when {
                            state.impostorGuessCorrect -> "IMPOSTORS WIN! Correctly deduced the secret word!"
                            state.crewWon -> "CREWMATES WIN! The impostor was exposed!"
                            else -> "IMPOSTORS WIN! Blended right past the crew!"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        color = winnerStyle.glow,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Generated Outcome Hero Illustration Banner (16:9)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(16f / 9f)
                            .clip(RoundedCornerShape(22.dp))
                    ) {
                        Image(
                            painter = painterResource(
                                id = if (state.crewWon) R.drawable.img_outcome_crew_win else R.drawable.img_outcome_impostor_win
                            ),
                            contentDescription = if (state.crewWon) "Agents win spotlight" else "Impostor wins spotlight",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(Color.Transparent, Color(0xBB1D1626))
                                    )
                                )
                        )
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.92f),
                            shape = RoundedCornerShape(50),
                            border = BorderStroke(1.dp, winnerStyle.glow.copy(alpha = 0.7f)),
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(12.dp)
                        ) {
                            Text(
                                text = "${impostorPlayers.joinToString(" & ") { it.name }} ${if (impostorPlayers.size > 1) "were" else "was"} the impostor!",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = palette.goldAccent,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 7.dp)
                            )
                        }
                    }
                }
            }

            // Tilted Secret Word Card matching the reference video reveal
            if (wordEntry != null) {
                item {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceContainer,
                        shape = RoundedCornerShape(22.dp),
                        border = BorderStroke(1.5.dp, palette.goldAccent.copy(alpha = 0.7f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .graphicsLayer { rotationZ = -1.4f }
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "THE SECRET WORD",
                                style = MaterialTheme.typography.labelSmall,
                                color = palette.goldAccent,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = wordEntry.word,
                                style = MaterialTheme.typography.displayMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onBackground,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "${wordEntry.category} • ${wordEntry.genre}" +
                                    if (state.hintsEnabled && state.activeHint.isNotBlank()) " • Hint: ${state.activeHint}" else "",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            // Special Role Outcome Callouts
            if (state.impostorAbilityEnabled && state.ultimateKillerId != null) {
                item {
                    val killerStyle = GameRoleIdentity.AGENT_KILLER.style(palette)
                    val killerName = nameMap[state.ultimateKillerId] ?: "Impostor"
                    val targetedId = state.rawKillerTargetId ?: state.nullifiedVoterId
                    val targetedName = targetedId?.let { nameMap[it] }
                    val targetedVotedImpostor = targetedId?.let { state.votesByPlayerId[it] } in state.impostorIds
                    SpecialRoleSummaryCard(
                        identity = GameRoleIdentity.AGENT_KILLER,
                        title = "AGENT KILLER: $killerName",
                        body = when {
                            targetedName == null -> "$killerName chose not to silence anyone's vote."
                            state.saviorSavedTarget -> "$killerName targeted $targetedName — BLOCKED by Savior Shield!"
                            targetedVotedImpostor -> "$killerName silenced $targetedName — vote for impostor was VOIDED!"
                            else -> "$killerName silenced $targetedName's vote."
                        },
                        accent = killerStyle.glow
                    )
                }
            }

            if (state.saviorAgentEnabled && state.saviorAgentId != null) {
                item {
                    val saviorStyle = GameRoleIdentity.SAVIOR_AGENT.style(palette)
                    val saviorName = nameMap[state.saviorAgentId] ?: "Savior"
                    val protectedName = state.protectedPlayerId?.let { nameMap[it] }
                    SpecialRoleSummaryCard(
                        identity = GameRoleIdentity.SAVIOR_AGENT,
                        title = "SAVIOR AGENT: $saviorName",
                        body = when {
                            protectedName == null -> "$saviorName held their shield."
                            state.saviorSavedTarget -> "$saviorName shielded $protectedName & blocked the Agent Killer! (+1)"
                            else -> "$saviorName shielded $protectedName."
                        },
                        accent = saviorStyle.glow
                    )
                }
            }

            if (state.agentDirectorEnabled && state.agentDirectorId != null) {
                item {
                    val directorStyle = GameRoleIdentity.AGENT_DIRECTOR.style(palette)
                    val directorName = nameMap[state.agentDirectorId] ?: "Director"
                    val directorVoided = state.agentDirectorId == state.nullifiedVoterId
                    SpecialRoleSummaryCard(
                        identity = GameRoleIdentity.AGENT_DIRECTOR,
                        title = "AGENT DIRECTOR: $directorName",
                        body = if (directorVoided) {
                            "$directorName's 2x vote was voided by the Agent Killer."
                        } else {
                            "$directorName's vote counted as 2 votes."
                        },
                        accent = directorStyle.glow
                    )
                }
            }

            // Scoreboard & Votes Breakdown
            item {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "SCOREBOARD & VOTES",
                            style = MaterialTheme.typography.labelMedium,
                            color = palette.goldAccent,
                            fontWeight = FontWeight.Bold
                        )
                        state.players.sortedByDescending { it.score }.forEach { player ->
                            val isImp = player.id in state.impostorIds
                            val isKiller = state.impostorAbilityEnabled && player.id == state.ultimateKillerId
                            val isSavior = state.saviorAgentEnabled && player.id == state.saviorAgentId
                            val isDirector = state.agentDirectorEnabled && player.id == state.agentDirectorId
                            val identity = GameRoleIdentity.resolve(
                                role = if (isImp) PlayerRole.IMPOSTOR else PlayerRole.NORMAL,
                                isUltimateKiller = isKiller,
                                isSavior = isSavior,
                                isDirector = isDirector
                            )
                            val pStyle = identity.style(palette)
                            val delta = state.roundDeltas[player.id] ?: 0
                            val deltaText = if (delta >= 0) "+$delta" else "$delta"
                            val votedForId = state.votesByPlayerId[player.id]
                            val votedForName = votedForId?.let { nameMap[it] } ?: "None"
                            val isVoided = player.id == state.nullifiedVoterId
                            val isProtected = state.saviorAgentEnabled && player.id == state.protectedPlayerId
                            val predictedId = state.agentPredictions[player.id]
                            val predictedName = predictedId?.let { nameMap[it] }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                CharacterAvatarThumbnail(
                                    character = AvatarCatalog.findById(player.avatarId),
                                    pose = player.avatarPose,
                                    size = 40.dp,
                                    borderColor = pStyle.glow,
                                    isHighlighted = true
                                )
                                RoleEmblemBadge(
                                    identity = identity,
                                    size = 24.dp,
                                    animated = false
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = player.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = pStyle.glow
                                    )
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        if (isVoided) {
                                            Icon(Icons.Filled.Block, contentDescription = null, tint = palette.impostorGlow, modifier = Modifier.size(13.dp))
                                        } else if (isProtected) {
                                            Icon(Icons.Filled.Shield, contentDescription = null, tint = palette.saviorGlow, modifier = Modifier.size(13.dp))
                                        }
                                        Text(
                                            text = buildString {
                                                append("Voted: $votedForName")
                                                if (isDirector && !isVoided && votedForId != null) append(" [2x]")
                                                if (isVoided) append(" [VOID]")
                                                if (!isImp && state.agentPredictionEnabled && predictedName != null) {
                                                    val hit = predictedId in state.impostorIds
                                                    append(" • Call: $predictedName (${if (hit) "+1" else "-2"})")
                                                }
                                            },
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "${player.score} pts",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = palette.goldAccent
                                    )
                                    Text(
                                        text = deltaText,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = when {
                                            delta > 0 -> palette.normalGlow
                                            delta < 0 -> palette.impostorGlow
                                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Sticky Footer
        Surface(
            color = MaterialTheme.colorScheme.background.copy(alpha = 0.96f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onReturnSetup,
                    shape = RoundedCornerShape(18.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier
                        .weight(0.75f)
                        .height(56.dp)
                ) {
                    Text("Edit Setup")
                }
                GoldCtaButton(
                    text = "One more round",
                    onClick = onNextRound,
                    showArrow = false,
                    modifier = Modifier
                        .weight(1.25f)
                        .testTag("local_next_round_button")
                )
            }
        }
    }
}

@Composable
private fun SpecialRoleSummaryCard(
    identity: GameRoleIdentity,
    title: String,
    body: String,
    accent: Color
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainer,
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.65f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            RoleEmblemBadge(identity = identity, size = 34.dp, animated = true)
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelLarge,
                    color = accent,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = body,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}
