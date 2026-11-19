package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.SoundCuePlayer
import com.example.data.AvatarCatalog
import com.example.data.AvatarPose
import com.example.data.CategoryWordGenerator
import com.example.data.CryptoRandom
import com.example.data.EmergencyMeetingAlert
import com.example.data.GamePhase
import com.example.data.GameState
import com.example.data.LocalPlayer
import com.example.data.LocationWordRepository
import com.example.data.PlayerRole
import com.example.data.RoundOutcome
import com.example.data.TurnStatus
import com.example.data.WordBank
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

class GameViewModel(
    private val locationRepository: LocationWordRepository = LocationWordRepository
) : ViewModel() {

    private val _gameState = MutableStateFlow(GameState())
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    private var timerJob: Job? = null
    private var emergencyJob: Job? = null

    init {
        assignRoles()
        selectNewLocationWord()
        _gameState.update { curr ->
            curr.copy(
                totalVotesPerPlayer = computeTotalVotesPerPlayer(curr.players, curr.votesByPlayerId),
                emergencyAlertSoundEnabled = SoundCuePlayer.emergencyAlertSoundEnabled.value,
                soundEffectsEnabled = SoundCuePlayer.soundEnabled.value
            )
        }
    }

    fun setPlayerCount(targetCount: Int) {
        val clampedCount = targetCount.coerceIn(2, 16)
        _gameState.update { curr ->
            val updatedPlayers = if (clampedCount == curr.players.size) {
                curr.players
            } else if (clampedCount < curr.players.size) {
                curr.players.take(clampedCount)
            } else {
                val used = curr.players.map { it.avatarId }.toMutableSet()
                val extras = (curr.players.size + 1..clampedCount).map { idx ->
                    val nextAvatar = AvatarCatalog.nextAvailableAvatar(used, idx - 1)
                    used.add(nextAvatar.id)
                    LocalPlayer(
                        id = UUID.randomUUID().toString().take(8),
                        name = "Player $idx",
                        avatarId = nextAvatar.id,
                        avatarPose = AvatarPose.ORIGINAL
                    )
                }
                curr.players + extras
            }
            val validIds = updatedPlayers.map { it.id }.toSet()
            val maxImpostors = (updatedPlayers.size - 1).coerceAtLeast(1)
            val clampedImp = curr.impostorCount.coerceIn(1, maxImpostors)
            curr.copy(
                players = updatedPlayers,
                impostorCount = clampedImp,
                playerRoles = buildRoleAssignments(updatedPlayers, clampedImp),
                revealedRolePlayerIds = curr.revealedRolePlayerIds.intersect(validIds),
                totalVotesPerPlayer = computeTotalVotesPerPlayer(updatedPlayers, emptyMap())
            )
        }
    }

    fun setImpostorCount(count: Int) {
        _gameState.update { curr ->
            val maxImpostors = (curr.players.size - 1).coerceAtLeast(1)
            val clamped = count.coerceIn(1, maxImpostors)
            curr.copy(
                impostorCount = clamped,
                playerRoles = buildRoleAssignments(curr.players, clamped)
            )
        }
    }

    fun assignRoles() {
        _gameState.update { curr ->
            val maxImpostors = (curr.players.size - 1).coerceAtLeast(1)
            val clamped = curr.impostorCount.coerceIn(1, maxImpostors)
            curr.copy(
                impostorCount = clamped,
                sessionSeedNumber = curr.sessionSeedNumber + 1,
                playerRoles = buildRoleAssignments(curr.players, clamped),
                revealedRolePlayerIds = emptySet()
            )
        }
    }

    fun setCategory(category: String) {
        _gameState.update { curr ->
            val exclude = curr.locationWord?.word?.let { curr.usedWords + it } ?: curr.usedWords
            val generated = if (category == "Locations" && locationRepository === LocationWordRepository) {
                CategoryWordGenerator.generate(
                    category = category,
                    usedWords = exclude,
                    hintsEnabled = curr.hintsEnabled
                ).entry
            } else {
                CategoryWordGenerator.generate(
                    category = category,
                    usedWords = exclude,
                    hintsEnabled = curr.hintsEnabled
                ).entry
            }
            curr.copy(
                selectedCategory = category,
                locationWord = generated,
                usedWords = curr.usedWords + generated.word
            )
        }
    }

    fun setHintsEnabled(enabled: Boolean) {
        _gameState.update { it.copy(hintsEnabled = enabled) }
    }

    fun selectNewLocationWord() {
        _gameState.update { curr ->
            val exclude = curr.locationWord?.word?.let { setOf(it) } ?: emptySet()
            val generated = if (curr.selectedCategory == "Locations") {
                locationRepository.pickRandomLocationWord(excludeWords = exclude)
            } else {
                CategoryWordGenerator.generate(
                    category = curr.selectedCategory,
                    usedWords = curr.usedWords + exclude,
                    hintsEnabled = curr.hintsEnabled
                ).entry
            }
            curr.copy(
                locationWord = generated,
                usedWords = curr.usedWords + generated.word
            )
        }
    }

    fun toggleRoleReveal(playerId: String) {
        _gameState.update { curr ->
            val next = if (playerId in curr.revealedRolePlayerIds) {
                curr.revealedRolePlayerIds - playerId
            } else {
                curr.revealedRolePlayerIds + playerId
            }
            curr.copy(revealedRolePlayerIds = next)
        }
    }

    fun revealAllRoles() {
        _gameState.update { curr ->
            curr.copy(revealedRolePlayerIds = curr.players.map { it.id }.toSet())
        }
    }

    fun hideAllRoles() {
        _gameState.update { curr ->
            curr.copy(revealedRolePlayerIds = emptySet())
        }
    }

    fun addPlayer(rawName: String) {
        val trimmed = rawName.trim().take(24)
        _gameState.update { curr ->
            val finalName = trimmed.ifEmpty { "Player ${curr.players.size + 1}" }
            val used = curr.players.map { it.avatarId }.toSet()
            val nextAvatar = AvatarCatalog.nextAvailableAvatar(used, curr.players.size)
            val newPlayer = LocalPlayer(
                id = UUID.randomUUID().toString().take(8),
                name = finalName,
                avatarId = nextAvatar.id,
                avatarPose = AvatarPose.ORIGINAL
            )
            val updatedPlayers = curr.players + newPlayer
            val maxImpostors = (updatedPlayers.size - 1).coerceAtLeast(1)
            val clampedImp = curr.impostorCount.coerceIn(1, maxImpostors)
            curr.copy(
                players = updatedPlayers,
                impostorCount = clampedImp,
                playerRoles = buildRoleAssignments(updatedPlayers, clampedImp),
                totalVotesPerPlayer = computeTotalVotesPerPlayer(updatedPlayers, emptyMap())
            )
        }
    }

    fun setPlayerAvatar(playerId: String, avatarId: String, pose: AvatarPose? = null) {
        val validId = AvatarCatalog.findById(avatarId).id
        _gameState.update { curr ->
            curr.copy(
                players = curr.players.map { player ->
                    if (player.id == playerId) {
                        player.copy(
                            avatarId = validId,
                            avatarPose = pose ?: player.avatarPose
                        )
                    } else {
                        player
                    }
                }
            )
        }
    }

    fun cyclePlayerPose(playerId: String) {
        _gameState.update { curr ->
            curr.copy(
                players = curr.players.map { player ->
                    if (player.id == playerId) {
                        player.copy(avatarPose = player.avatarPose.next())
                    } else {
                        player
                    }
                }
            )
        }
    }

    fun removePlayer(playerId: String) {
        _gameState.update { curr ->
            if (curr.players.size <= 2) return@update curr
            val updatedPlayers = curr.players.filterNot { it.id == playerId }
            val maxImpostors = (updatedPlayers.size - 1).coerceAtLeast(1)
            val clampedImp = curr.impostorCount.coerceIn(1, maxImpostors)
            curr.copy(
                players = updatedPlayers,
                impostorCount = clampedImp,
                playerRoles = buildRoleAssignments(updatedPlayers, clampedImp),
                revealedRolePlayerIds = curr.revealedRolePlayerIds - playerId,
                totalVotesPerPlayer = computeTotalVotesPerPlayer(updatedPlayers, emptyMap())
            )
        }
    }

    fun setTurnDuration(seconds: Int) {
        val clamped = seconds.coerceIn(5, 300)
        _gameState.update { curr ->
            curr.copy(
                turnDurationSeconds = clamped,
                currentTimerCount = if (curr.phase == GamePhase.LOBBY) clamped else curr.currentTimerCount.coerceAtMost(clamped)
            )
        }
    }

    fun setEmergencyAlertSoundEnabled(enabled: Boolean) {
        SoundCuePlayer.setEmergencyAlertSoundEnabled(enabled)
        _gameState.update { it.copy(emergencyAlertSoundEnabled = enabled) }
    }

    fun setSoundEffectsEnabled(enabled: Boolean) {
        SoundCuePlayer.setSoundEnabled(enabled)
        _gameState.update { it.copy(soundEffectsEnabled = enabled) }
    }

    fun startDiscussion() {
        stopTimer()
        stopEmergencyJob()
        _gameState.update { curr ->
            val maxImpostors = (curr.players.size - 1).coerceAtLeast(1)
            val clampedImp = curr.impostorCount.coerceIn(1, maxImpostors)
            val roles = if (curr.playerRoles.keys == curr.players.map { it.id }.toSet()) {
                curr.playerRoles
            } else {
                buildRoleAssignments(curr.players, clampedImp)
            }
            val selectedLocation = curr.locationWord ?: CategoryWordGenerator.generate(
                category = curr.selectedCategory,
                usedWords = curr.usedWords,
                hintsEnabled = curr.hintsEnabled
            ).entry
            val turnOrder = CryptoRandom.shuffled(curr.players.map { it.id })
            curr.copy(
                phase = GamePhase.DISCUSSION,
                impostorCount = clampedImp,
                playerRoles = roles,
                locationWord = selectedLocation,
                turnOrderIds = turnOrder,
                currentTurnIndex = 0,
                turnStatus = TurnStatus.ACTIVE,
                currentTimerCount = curr.turnDurationSeconds,
                votesByPlayerId = emptyMap(),
                totalVotesPerPlayer = computeTotalVotesPerPlayer(curr.players, emptyMap()),
                votedOutPlayerIds = emptyList(),
                roundOutcome = RoundOutcome.PENDING,
                outcomeReason = "",
                emergencyMeeting = EmergencyMeetingAlert()
            )
        }
        startTimerLoop()
    }

    fun toggleTimerPause() {
        _gameState.update { curr ->
            if (curr.phase != GamePhase.DISCUSSION || curr.emergencyMeeting.isActive) return@update curr
            val nextStatus = when (curr.turnStatus) {
                TurnStatus.ACTIVE -> TurnStatus.PAUSED
                TurnStatus.PAUSED -> if (curr.currentTimerCount > 0) TurnStatus.ACTIVE else TurnStatus.EXPIRED
                TurnStatus.EXPIRED, TurnStatus.IDLE -> curr.turnStatus
            }
            curr.copy(turnStatus = nextStatus)
        }
    }

    fun nextTurn() {
        val curr = _gameState.value
        if (curr.phase != GamePhase.DISCUSSION) return
        val nextIndex = curr.currentTurnIndex + 1
        if (nextIndex >= curr.turnOrderIds.size) {
            startVoting()
        } else {
            _gameState.update { state ->
                state.copy(
                    currentTurnIndex = nextIndex,
                    turnStatus = TurnStatus.ACTIVE,
                    currentTimerCount = state.turnDurationSeconds
                )
            }
        }
    }

    fun triggerEmergencyMeeting(
        reporterId: String? = null,
        reason: String = "Suspicious activity reported!"
    ) {
        val curr = _gameState.value
        if (curr.phase != GamePhase.DISCUSSION || curr.emergencyMeeting.isActive) return
        stopTimer()
        stopEmergencyJob()

        val reporterName = curr.players.firstOrNull { it.id == reporterId }?.name
            ?: curr.activeTurnPlayer?.name
            ?: "Agent"

        SoundCuePlayer.playEmergencyMeetingCue()

        _gameState.update { state ->
            state.copy(
                turnStatus = TurnStatus.PAUSED,
                emergencyMeeting = EmergencyMeetingAlert(
                    isActive = true,
                    reporterName = reporterName,
                    reason = reason,
                    countdownSeconds = 2
                )
            )
        }

        emergencyJob = viewModelScope.launch {
            delay(1000L)
            if (_gameState.value.emergencyMeeting.isActive) {
                _gameState.update { state ->
                    state.copy(emergencyMeeting = state.emergencyMeeting.copy(countdownSeconds = 1))
                }
            }
            delay(1000L)
            if (_gameState.value.emergencyMeeting.isActive) {
                startVoting()
            }
        }
    }

    fun dismissEmergencyMeetingAndVote() {
        if (!_gameState.value.emergencyMeeting.isActive) return
        startVoting()
    }

    fun startVoting() {
        stopTimer()
        stopEmergencyJob()
        _gameState.update { curr ->
            curr.copy(
                phase = GamePhase.VOTING,
                turnStatus = TurnStatus.IDLE,
                votesByPlayerId = emptyMap(),
                totalVotesPerPlayer = computeTotalVotesPerPlayer(curr.players, emptyMap()),
                votedOutPlayerIds = emptyList(),
                roundOutcome = RoundOutcome.PENDING,
                outcomeReason = "",
                emergencyMeeting = EmergencyMeetingAlert()
            )
        }
    }

    fun castVote(voterId: String, suspectId: String) {
        if (voterId == suspectId) return
        _gameState.update { curr ->
            if (curr.phase != GamePhase.VOTING) return@update curr
            val updatedVotes = curr.votesByPlayerId + (voterId to suspectId)
            curr.copy(
                votesByPlayerId = updatedVotes,
                totalVotesPerPlayer = computeTotalVotesPerPlayer(curr.players, updatedVotes)
            )
        }
    }

    fun finalizeVoting() {
        stopTimer()
        stopEmergencyJob()
        var impostorCaughtForCue = false
        _gameState.update { curr ->
            val tally = computeTotalVotesPerPlayer(curr.players, curr.votesByPlayerId)
            val maxVotes = tally.values.maxOrNull() ?: 0
            val topIds = if (maxVotes > 0) {
                tally.filterValues { it == maxVotes }.keys.toList()
            } else {
                emptyList()
            }
            val impostorVotedOut = topIds.any { it in curr.impostorIds }
            impostorCaughtForCue = impostorVotedOut
            val outcome = if (impostorVotedOut) RoundOutcome.VICTORY else RoundOutcome.DEFEAT
            val votedNames = curr.players.filter { it.id in topIds }.joinToString(", ") { it.name }
            val reason = when {
                topIds.isEmpty() -> "No votes were cast — the Impostors achieved their goal!"
                impostorVotedOut -> "$votedNames was voted out — the Impostors have been eliminated!"
                topIds.size > 1 -> "Tie vote ($votedNames) — the Impostors achieved their goal!"
                else -> "$votedNames was ejected, but was innocent — the Impostors achieved their goal!"
            }
            curr.copy(
                phase = GamePhase.RESULTS,
                turnStatus = TurnStatus.IDLE,
                totalVotesPerPlayer = tally,
                votedOutPlayerIds = topIds,
                roundOutcome = outcome,
                outcomeReason = reason
            )
        }
        SoundCuePlayer.playVotingResultsCue(crewWon = impostorCaughtForCue)
    }

    fun playNextRound() {
        assignRoles()
        selectNewLocationWord()
        startDiscussion()
    }

    fun returnToLobby() {
        stopTimer()
        stopEmergencyJob()
        _gameState.update { curr ->
            curr.copy(
                phase = GamePhase.LOBBY,
                turnStatus = TurnStatus.IDLE,
                currentTurnIndex = 0,
                currentTimerCount = curr.turnDurationSeconds,
                votesByPlayerId = emptyMap(),
                totalVotesPerPlayer = computeTotalVotesPerPlayer(curr.players, emptyMap()),
                votedOutPlayerIds = emptyList(),
                roundOutcome = RoundOutcome.PENDING,
                outcomeReason = "",
                emergencyMeeting = EmergencyMeetingAlert()
            )
        }
    }

    fun tickTimerOnceForTest() {
        _gameState.update { curr ->
            if (curr.phase != GamePhase.DISCUSSION || curr.turnStatus != TurnStatus.ACTIVE) {
                return@update curr
            }
            val nextSec = (curr.currentTimerCount - 1).coerceAtLeast(0)
            curr.copy(
                currentTimerCount = nextSec,
                turnStatus = if (nextSec == 0) TurnStatus.EXPIRED else TurnStatus.ACTIVE
            )
        }
    }

    private fun computeTotalVotesPerPlayer(
        players: List<LocalPlayer>,
        votesByPlayerId: Map<String, String>
    ): Map<String, Int> {
        val counts = votesByPlayerId.values.groupingBy { it }.eachCount()
        return players.associate { player -> player.id to (counts[player.id] ?: 0) }
    }

    private fun buildRoleAssignments(
        players: List<LocalPlayer>,
        impostorCount: Int
    ): Map<String, PlayerRole> {
        if (players.isEmpty()) return emptyMap()
        val maxImp = (players.size - 1).coerceAtLeast(1)
        val targetImpostors = impostorCount.coerceIn(1, maxImp)
        val impostorIds = CryptoRandom.shuffled(players.map { it.id })
            .take(targetImpostors)
            .toSet()
        return players.associate { player ->
            player.id to if (player.id in impostorIds) PlayerRole.IMPOSTOR else PlayerRole.NORMAL
        }
    }

    private fun startTimerLoop() {
        stopTimer()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000L)
                val curr = _gameState.value
                if (curr.phase != GamePhase.DISCUSSION || curr.emergencyMeeting.isActive) break
                if (curr.turnStatus == TurnStatus.ACTIVE && curr.currentTimerCount > 0) {
                    val nextSec = (curr.currentTimerCount - 1).coerceAtLeast(0)
                    _gameState.update { state ->
                        state.copy(
                            currentTimerCount = nextSec,
                            turnStatus = if (nextSec == 0) TurnStatus.EXPIRED else TurnStatus.ACTIVE
                        )
                    }
                    if (nextSec == 0) {
                        SoundCuePlayer.playTimerExpiredCue()
                        delay(1200L)
                        val afterWait = _gameState.value
                        if (afterWait.phase == GamePhase.DISCUSSION &&
                            afterWait.turnStatus == TurnStatus.EXPIRED &&
                            !afterWait.emergencyMeeting.isActive
                        ) {
                            nextTurn()
                        }
                    }
                }
            }
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
    }

    private fun stopEmergencyJob() {
        emergencyJob?.cancel()
        emergencyJob = null
    }

    override fun onCleared() {
        super.onCleared()
        stopTimer()
        stopEmergencyJob()
    }
}
