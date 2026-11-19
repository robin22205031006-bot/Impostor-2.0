package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.SoundCuePlayer
import com.example.data.AvatarCatalog
import com.example.data.AvatarPose
import com.example.data.CategoryWordGenerator
import com.example.data.CryptoRandom
import com.example.data.EmergencyMeetingAlert
import com.example.data.GameMode
import com.example.data.LocalPlayer
import com.example.data.PlayerRole
import com.example.data.PlayerWordBriefing
import com.example.data.RoundScoring
import com.example.data.WordBank
import com.example.data.WordEntry
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

enum class SetupWizardStep {
    WELCOME,
    MODE_PICK,
    PLAYERS,
    RULES,
    PACKS
}

enum class LocalPhase {
    SETUP,
    REVEAL,
    PLAYING,
    VOTING,
    RESULTS
}

data class LocalGameUiState(
    val phase: LocalPhase = LocalPhase.SETUP,
    val setupStep: SetupWizardStep = SetupWizardStep.WELCOME,
    val players: List<LocalPlayer> = listOf(
        LocalPlayer(id = "p1", name = "Player 1", avatarId = "avatar_05", avatarPose = AvatarPose.ORIGINAL),
        LocalPlayer(id = "p2", name = "Player 2", avatarId = "avatar_08", avatarPose = AvatarPose.ORIGINAL),
        LocalPlayer(id = "p3", name = "Player 3", avatarId = "avatar_01", avatarPose = AvatarPose.ORIGINAL),
        LocalPlayer(id = "p4", name = "Player 4", avatarId = "avatar_09", avatarPose = AvatarPose.ORIGINAL)
    ),
    val gameMode: GameMode = GameMode.CLASSIC,
    val impostorCount: Int = 1,
    val turnDurationSeconds: Int = 45,
    val selectedCategory: String = WordBank.CATEGORY_ALL,
    val favoritePacks: Set<String> = emptySet(),
    val hintsEnabled: Boolean = true,
    val categoryCluesEnabled: Boolean = true,
    val timerEnabled: Boolean = true,
    val impostorAbilityEnabled: Boolean = false,
    val saviorAgentEnabled: Boolean = false,
    val agentDirectorEnabled: Boolean = false,
    val agentPredictionEnabled: Boolean = false,
    val roundNumber: Int = 0,
    val activeWord: WordEntry? = null,
    val activeHint: String = "",
    val impostorIds: Set<String> = emptySet(),
    val ultimateKillerId: String? = null,
    val rawKillerTargetId: String? = null,
    val nullifiedVoterId: String? = null,
    val saviorAgentId: String? = null,
    val protectedPlayerId: String? = null,
    val saviorSavedTarget: Boolean = false,
    val agentDirectorId: String? = null,
    val agentPredictions: Map<String, String> = emptyMap(),
    val revealPlayerIndex: Int = 0,
    val isRoleCardVisible: Boolean = false,
    val turnOrderIds: List<String> = emptyList(),
    val currentTurnIndex: Int = 0,
    val remainingTurnSeconds: Int = 45,
    val isTimerPaused: Boolean = false,
    val votesByPlayerId: Map<String, String> = emptyMap(),
    val votingPlayerIndex: Int = 0,
    val isVoteCardRevealed: Boolean = false,
    val impostorWordGuess: String = "",
    val impostorGuessCorrect: Boolean = false,
    val crewWon: Boolean = false,
    val roundDeltas: Map<String, Int> = emptyMap(),
    val roundVoteCounts: Map<String, Int> = emptyMap(),
    val usedWords: Set<String> = emptySet(),
    val emergencyMeeting: EmergencyMeetingAlert = EmergencyMeetingAlert(),
    val errorBanner: String? = null
) {
    fun roleFor(playerId: String): PlayerRole =
        if (playerId in impostorIds) PlayerRole.IMPOSTOR else PlayerRole.NORMAL

    fun wordBriefingFor(playerId: String): PlayerWordBriefing? {
        val word = activeWord ?: return null
        val role = roleFor(playerId)
        val isImp = role == PlayerRole.IMPOSTOR
        return PlayerWordBriefing(
            role = role,
            secretWord = if (isImp) null else word.word,
            agentHint = if (isImp || !hintsEnabled) null else activeHint.ifBlank { null },
            category = if (categoryCluesEnabled) word.category else null,
            genre = if (categoryCluesEnabled) word.genre else null,
            isSecretWordHidden = isImp
        )
    }
}

class LocalGameViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(LocalGameUiState())
    val uiState: StateFlow<LocalGameUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null
    private var emergencyJob: Job? = null

    fun clearError() {
        _uiState.update { it.copy(errorBanner = null) }
    }

    fun setSetupStep(step: SetupWizardStep) {
        _uiState.update { it.copy(setupStep = step, errorBanner = null) }
    }

    fun toggleFavoritePack(category: String) {
        _uiState.update { curr ->
            val nextFavs = if (category in curr.favoritePacks) {
                curr.favoritePacks - category
            } else {
                curr.favoritePacks + category
            }
            curr.copy(favoritePacks = nextFavs)
        }
    }

    fun addPlayer(rawName: String) {
        val trimmed = rawName.trim().take(24)
        val state = _uiState.value
        val finalName = trimmed.ifEmpty { "Player ${state.players.size + 1}" }
        val usedIds = state.players.map { it.avatarId }.toSet()
        val pickedAvatar = AvatarCatalog.nextAvailableAvatar(usedIds, state.players.size)
        val newPlayer = LocalPlayer(
            id = UUID.randomUUID().toString().take(8),
            name = finalName,
            avatarId = pickedAvatar.id,
            avatarPose = AvatarPose.ORIGINAL
        )
        _uiState.update { curr ->
            curr.copy(
                players = curr.players + newPlayer,
                errorBanner = null
            )
        }
    }

    fun renamePlayer(playerId: String, rawName: String) {
        val trimmed = rawName.trim().take(24)
        if (trimmed.isEmpty()) return
        _uiState.update { curr ->
            curr.copy(
                players = curr.players.map {
                    if (it.id == playerId) it.copy(name = trimmed) else it
                }
            )
        }
    }

    fun setPlayerAvatar(playerId: String, avatarId: String, pose: AvatarPose? = null) {
        val validAvatarId = AvatarCatalog.findById(avatarId).id
        _uiState.update { curr ->
            curr.copy(
                players = curr.players.map { player ->
                    if (player.id == playerId) {
                        player.copy(
                            avatarId = validAvatarId,
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
        _uiState.update { curr ->
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
        _uiState.update { curr ->
            if (curr.players.size <= 2) {
                return@update curr.copy(errorBanner = "At least 2 players are required.")
            }
            val updatedPlayers = curr.players.filterNot { it.id == playerId }
            val maxImp = (updatedPlayers.size - 1).coerceAtLeast(1)
            curr.copy(
                players = updatedPlayers,
                impostorCount = curr.impostorCount.coerceIn(1, maxImp),
                errorBanner = null
            )
        }
    }

    fun setPlayerCount(targetCount: Int) {
        val clampedCount = targetCount.coerceIn(2, 16)
        _uiState.update { curr ->
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
            val maxImp = (updatedPlayers.size - 1).coerceAtLeast(1)
            curr.copy(
                players = updatedPlayers,
                impostorCount = curr.impostorCount.coerceIn(1, maxImp),
                errorBanner = null
            )
        }
    }

    fun setGameMode(mode: GameMode) {
        _uiState.update { curr ->
            curr.copy(
                gameMode = mode,
                turnDurationSeconds = mode.defaultTurnSeconds
            )
        }
    }

    fun setImpostorCount(count: Int) {
        _uiState.update { curr ->
            val maxImp = (curr.players.size - 1).coerceAtLeast(1)
            curr.copy(impostorCount = count.coerceIn(1, maxImp))
        }
    }

    fun setTurnDuration(seconds: Int) {
        _uiState.update { it.copy(turnDurationSeconds = seconds.coerceIn(5, 300)) }
    }

    fun setCategory(category: String) {
        _uiState.update { it.copy(selectedCategory = category) }
    }

    fun setHintsEnabled(enabled: Boolean) {
        _uiState.update { it.copy(hintsEnabled = enabled) }
    }

    fun setCategoryCluesEnabled(enabled: Boolean) {
        _uiState.update { it.copy(categoryCluesEnabled = enabled) }
    }

    fun setTimerEnabled(enabled: Boolean) {
        _uiState.update { it.copy(timerEnabled = enabled) }
    }

    fun setImpostorAbilityEnabled(enabled: Boolean) {
        _uiState.update { it.copy(impostorAbilityEnabled = enabled) }
    }

    fun setSaviorAgentEnabled(enabled: Boolean) {
        _uiState.update { it.copy(saviorAgentEnabled = enabled) }
    }

    fun setAgentDirectorEnabled(enabled: Boolean) {
        _uiState.update { it.copy(agentDirectorEnabled = enabled) }
    }

    fun setAgentPredictionEnabled(enabled: Boolean) {
        _uiState.update { it.copy(agentPredictionEnabled = enabled) }
    }

    fun startRound() {
        stopTimer()
        val curr = _uiState.value
        if (curr.players.size < 2) {
            _uiState.update { it.copy(errorBanner = "Add at least 2 players to start.") }
            return
        }
        if (WordBank.isCustomCategory(curr.selectedCategory) && WordBank.customEntries.isEmpty()) {
            _uiState.update { it.copy(errorBanner = "Add at least 1 custom word first, or choose another category.") }
            return
        }
        val maxImp = (curr.players.size - 1).coerceAtLeast(1)
        val actualImpCount = curr.impostorCount.coerceIn(1, maxImp)
        val generatedRound = CategoryWordGenerator.generate(
            category = curr.selectedCategory,
            usedWords = curr.usedWords,
            mode = curr.gameMode,
            hintsEnabled = curr.hintsEnabled,
            categoryCluesEnabled = curr.categoryCluesEnabled
        )
        val pickedWord = generatedRound.entry
        val shuffledPlayers = CryptoRandom.shuffled(curr.players)
        val chosenImpostorList = shuffledPlayers.take(actualImpCount).map { it.id }
        val chosenImpostors = chosenImpostorList.toSet()
        val chosenKiller = if (curr.impostorAbilityEnabled) {
            CryptoRandom.pickOne(chosenImpostorList)
        } else {
            null
        }
        val regularAgents = CryptoRandom.shuffled(
            curr.players.filterNot { it.id in chosenImpostors }.map { it.id }
        )
        val chosenSavior = if (curr.saviorAgentEnabled && regularAgents.isNotEmpty()) {
            regularAgents.first()
        } else {
            null
        }
        val chosenDirector = if (curr.agentDirectorEnabled && regularAgents.isNotEmpty()) {
            when {
                regularAgents.size >= 2 -> regularAgents[1]
                chosenSavior == null -> regularAgents[0]
                else -> null
            }
        } else {
            null
        }
        val turnOrder = CryptoRandom.shuffled(curr.players.map { it.id })

        _uiState.update {
            it.copy(
                phase = LocalPhase.REVEAL,
                impostorCount = actualImpCount,
                roundNumber = it.roundNumber + 1,
                activeWord = pickedWord,
                activeHint = generatedRound.agentHintText,
                impostorIds = chosenImpostors,
                ultimateKillerId = chosenKiller,
                rawKillerTargetId = null,
                nullifiedVoterId = null,
                saviorAgentId = chosenSavior,
                protectedPlayerId = null,
                saviorSavedTarget = false,
                agentDirectorId = chosenDirector,
                agentPredictions = emptyMap(),
                revealPlayerIndex = 0,
                isRoleCardVisible = false,
                turnOrderIds = turnOrder,
                currentTurnIndex = 0,
                remainingTurnSeconds = it.turnDurationSeconds,
                isTimerPaused = !it.timerEnabled,
                votesByPlayerId = emptyMap(),
                votingPlayerIndex = 0,
                isVoteCardRevealed = false,
                impostorWordGuess = "",
                impostorGuessCorrect = false,
                crewWon = false,
                roundDeltas = emptyMap(),
                roundVoteCounts = emptyMap(),
                usedWords = it.usedWords + pickedWord.word,
                errorBanner = null
            )
        }
    }

    fun showCurrentPlayerCard() {
        _uiState.update { it.copy(isRoleCardVisible = true) }
    }

    fun reshuffleSessionRoles() {
        val curr = _uiState.value
        if (curr.phase != LocalPhase.REVEAL || curr.revealPlayerIndex > 0 || curr.isRoleCardVisible) return
        val maxImp = (curr.players.size - 1).coerceAtLeast(1)
        val actualImpCount = curr.impostorCount.coerceIn(1, maxImp)
        val shuffledPlayers = CryptoRandom.shuffled(curr.players)
        val chosenImpostorList = shuffledPlayers.take(actualImpCount).map { it.id }
        val chosenImpostors = chosenImpostorList.toSet()
        val chosenKiller = if (curr.impostorAbilityEnabled) CryptoRandom.pickOne(chosenImpostorList) else null
        val regularAgents = CryptoRandom.shuffled(
            curr.players.filterNot { it.id in chosenImpostors }.map { it.id }
        )
        val chosenSavior = if (curr.saviorAgentEnabled && regularAgents.isNotEmpty()) regularAgents.first() else null
        val chosenDirector = if (curr.agentDirectorEnabled && regularAgents.isNotEmpty()) {
            when {
                regularAgents.size >= 2 -> regularAgents[1]
                chosenSavior == null -> regularAgents[0]
                else -> null
            }
        } else {
            null
        }
        _uiState.update {
            it.copy(
                impostorIds = chosenImpostors,
                ultimateKillerId = chosenKiller,
                saviorAgentId = chosenSavior,
                agentDirectorId = chosenDirector,
                turnOrderIds = CryptoRandom.shuffled(curr.players.map { p -> p.id })
            )
        }
    }

    fun hideAndAdvanceReveal() {
        val curr = _uiState.value
        val nextIndex = curr.revealPlayerIndex + 1
        if (nextIndex >= curr.players.size) {
            _uiState.update {
                it.copy(
                    phase = LocalPhase.PLAYING,
                    isRoleCardVisible = false,
                    currentTurnIndex = 0,
                    remainingTurnSeconds = it.turnDurationSeconds,
                    isTimerPaused = !it.timerEnabled
                )
            }
            if (curr.timerEnabled) {
                startTimerLoop()
            }
        } else {
            _uiState.update {
                it.copy(
                    revealPlayerIndex = nextIndex,
                    isRoleCardVisible = false
                )
            }
        }
    }

    fun toggleTimerPause() {
        _uiState.update { it.copy(isTimerPaused = !it.isTimerPaused) }
    }

    fun addExtraSeconds(seconds: Int = 10) {
        _uiState.update {
            it.copy(remainingTurnSeconds = (it.remainingTurnSeconds + seconds).coerceAtMost(300))
        }
    }

    fun nextTurn() {
        val curr = _uiState.value
        val nextIndex = curr.currentTurnIndex + 1
        if (nextIndex >= curr.turnOrderIds.size) {
            goToVoting()
        } else {
            _uiState.update {
                it.copy(
                    currentTurnIndex = nextIndex,
                    remainingTurnSeconds = it.turnDurationSeconds,
                    isTimerPaused = !it.timerEnabled
                )
            }
        }
    }

    fun setNullifiedVoter(targetPlayerId: String?) {
        _uiState.update { curr ->
            val next = if (targetPlayerId == null || targetPlayerId == curr.nullifiedVoterId) {
                null
            } else {
                targetPlayerId
            }
            curr.copy(nullifiedVoterId = next)
        }
    }

    fun setProtectedPlayer(targetPlayerId: String?) {
        _uiState.update { curr ->
            val next = if (targetPlayerId == null || targetPlayerId == curr.protectedPlayerId) {
                null
            } else {
                targetPlayerId
            }
            curr.copy(protectedPlayerId = next)
        }
    }

    fun setAgentPrediction(agentId: String, predictedSuspectId: String?) {
        _uiState.update { curr ->
            val nextMap = curr.agentPredictions.toMutableMap()
            if (predictedSuspectId == null || nextMap[agentId] == predictedSuspectId) {
                nextMap.remove(agentId)
            } else {
                nextMap[agentId] = predictedSuspectId
            }
            curr.copy(agentPredictions = nextMap)
        }
    }

    fun triggerEmergencyMeeting(
        reporterId: String? = null,
        reason: String = "Suspicious activity reported!"
    ) {
        val curr = _uiState.value
        if (curr.phase != LocalPhase.PLAYING || curr.emergencyMeeting.isActive) return
        stopTimer()
        emergencyJob?.cancel()
        val activeUid = curr.turnOrderIds.getOrNull(curr.currentTurnIndex)
        val reporterName = curr.players.firstOrNull { it.id == reporterId }?.name
            ?: curr.players.firstOrNull { it.id == activeUid }?.name
            ?: "Agent"
        SoundCuePlayer.playEmergencyMeetingCue()
        _uiState.update {
            it.copy(
                isTimerPaused = true,
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
            if (_uiState.value.emergencyMeeting.isActive) {
                _uiState.update { state ->
                    state.copy(emergencyMeeting = state.emergencyMeeting.copy(countdownSeconds = 1))
                }
            }
            delay(1000L)
            if (_uiState.value.emergencyMeeting.isActive) {
                goToVoting()
            }
        }
    }

    fun dismissEmergencyMeetingAndVote() {
        if (!_uiState.value.emergencyMeeting.isActive) return
        goToVoting()
    }

    fun goToVoting() {
        stopTimer()
        emergencyJob?.cancel()
        emergencyJob = null
        _uiState.update {
            it.copy(
                phase = LocalPhase.VOTING,
                isTimerPaused = true,
                votesByPlayerId = emptyMap(),
                votingPlayerIndex = 0,
                isVoteCardRevealed = false,
                emergencyMeeting = EmergencyMeetingAlert()
            )
        }
    }

    fun revealVoterBallot() {
        _uiState.update { it.copy(isVoteCardRevealed = true) }
    }

    fun castLocalVote(voterId: String, suspectId: String) {
        _uiState.update { curr ->
            curr.copy(votesByPlayerId = curr.votesByPlayerId + (voterId to suspectId))
        }
    }

    fun confirmVoteAndAdvance() {
        _uiState.update { curr ->
            curr.copy(
                votingPlayerIndex = (curr.votingPlayerIndex + 1).coerceAtMost(curr.players.size),
                isVoteCardRevealed = false
            )
        }
    }

    fun updateImpostorWordGuess(guess: String) {
        _uiState.update { it.copy(impostorWordGuess = guess.take(80)) }
    }

    fun submitResults() {
        stopTimer()
        val curr = _uiState.value
        val word = curr.activeWord?.word.orEmpty()
        val guessMatches = WordBank.isGuessMatch(curr.impostorWordGuess, word)

        val eval = RoundScoring.evaluate(
            playerIds = curr.players.map { it.id },
            impostorIds = curr.impostorIds,
            votes = curr.votesByPlayerId,
            agentPredictions = curr.agentPredictions,
            nullifiedVoterId = curr.nullifiedVoterId,
            protectedPlayerId = curr.protectedPlayerId,
            saviorAgentId = curr.saviorAgentId,
            agentDirectorId = curr.agentDirectorId,
            impostorAbilityEnabled = curr.impostorAbilityEnabled,
            saviorAgentEnabled = curr.saviorAgentEnabled,
            agentDirectorEnabled = curr.agentDirectorEnabled,
            agentPredictionEnabled = curr.agentPredictionEnabled,
            impostorGuessCorrect = guessMatches
        )

        val updatedPlayers = curr.players.map { player ->
            val delta = eval.playerDeltas[player.id] ?: 0
            player.copy(score = player.score + delta)
        }

        _uiState.update {
            it.copy(
                phase = LocalPhase.RESULTS,
                players = updatedPlayers,
                impostorGuessCorrect = guessMatches,
                crewWon = eval.crewWins,
                roundDeltas = eval.playerDeltas,
                roundVoteCounts = eval.voteCounts,
                rawKillerTargetId = eval.rawKillerTargetId,
                nullifiedVoterId = eval.activeNullifiedVoterId,
                protectedPlayerId = eval.activeProtectedId,
                saviorSavedTarget = eval.saviorSavedTarget,
                agentDirectorId = eval.activeDirectorId
            )
        }
    }

    fun returnToSetup(step: SetupWizardStep = SetupWizardStep.PLAYERS) {
        stopTimer()
        _uiState.update {
            it.copy(
                phase = LocalPhase.SETUP,
                setupStep = step,
                isRoleCardVisible = false,
                errorBanner = null
            )
        }
    }

    fun roleFor(playerId: String): PlayerRole =
        if (playerId in _uiState.value.impostorIds) PlayerRole.IMPOSTOR else PlayerRole.NORMAL

    private fun startTimerLoop() {
        stopTimer()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000L)
                val curr = _uiState.value
                if (curr.phase != LocalPhase.PLAYING) break
                if (!curr.isTimerPaused && curr.remainingTurnSeconds > 0) {
                    val nextSec = (curr.remainingTurnSeconds - 1).coerceAtLeast(0)
                    _uiState.update { state ->
                        state.copy(remainingTurnSeconds = nextSec)
                    }
                    if (nextSec == 0) {
                        SoundCuePlayer.playTimerExpiredCue()
                    }
                }
            }
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
    }

    override fun onCleared() {
        super.onCleared()
        stopTimer()
        emergencyJob?.cancel()
    }
}
