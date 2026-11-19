package com.example.data

enum class PlayerRole(val displayName: String) {
    NORMAL("Agent"),
    IMPOSTOR("Impostor")
}

enum class GamePhase(val title: String) {
    LOBBY("Lobby"),
    DISCUSSION("Discussion"),
    VOTING("Voting"),
    RESULTS("Results")
}

enum class RoundOutcome(val headline: String) {
    PENDING("Pending"),
    VICTORY("Victory"),
    DEFEAT("Defeat")
}

enum class TurnStatus(val label: String) {
    IDLE("Standby"),
    ACTIVE("Active Turn"),
    PAUSED("Paused"),
    EXPIRED("Time Expired")
}

data class LocalPlayer(
    val id: String,
    val name: String,
    val score: Int = 0,
    val avatarId: String = AvatarCatalog.defaultIdForSeed(id),
    val avatarPose: AvatarPose = AvatarPose.ORIGINAL
)

data class EmergencyMeetingAlert(
    val isActive: Boolean = false,
    val reporterName: String = "",
    val reason: String = "Emergency Meeting Initiated",
    val countdownSeconds: Int = 2
)

data class GameState(
    val phase: GamePhase = GamePhase.LOBBY,
    val players: List<LocalPlayer> = listOf(
        LocalPlayer(id = "p1", name = "Player 1", avatarId = "avatar_05", avatarPose = AvatarPose.ORIGINAL),
        LocalPlayer(id = "p2", name = "Player 2", avatarId = "avatar_08", avatarPose = AvatarPose.ORIGINAL),
        LocalPlayer(id = "p3", name = "Player 3", avatarId = "avatar_01", avatarPose = AvatarPose.ORIGINAL),
        LocalPlayer(id = "p4", name = "Player 4", avatarId = "avatar_09", avatarPose = AvatarPose.ORIGINAL)
    ),
    val impostorCount: Int = 1,
    val selectedCategory: String = "Locations",
    val hintsEnabled: Boolean = true,
    val sessionSeedNumber: Int = 1,
    val playerRoles: Map<String, PlayerRole> = emptyMap(),
    val revealedRolePlayerIds: Set<String> = emptySet(),
    val locationWord: WordEntry? = null,
    val usedWords: Set<String> = emptySet(),
    val turnOrderIds: List<String> = emptyList(),
    val currentTurnIndex: Int = 0,
    val turnStatus: TurnStatus = TurnStatus.IDLE,
    val currentTimerCount: Int = 45,
    val turnDurationSeconds: Int = 45,
    val votesByPlayerId: Map<String, String> = emptyMap(),
    val totalVotesPerPlayer: Map<String, Int> = emptyMap(),
    val votedOutPlayerIds: List<String> = emptyList(),
    val roundOutcome: RoundOutcome = RoundOutcome.PENDING,
    val outcomeReason: String = "",
    val emergencyAlertSoundEnabled: Boolean = true,
    val soundEffectsEnabled: Boolean = true,
    val emergencyMeeting: EmergencyMeetingAlert = EmergencyMeetingAlert()
) {
    val activeTurnPlayer: LocalPlayer?
        get() {
            val activeId = turnOrderIds.getOrNull(currentTurnIndex) ?: return null
            return players.firstOrNull { it.id == activeId }
        }

    val impostorIds: Set<String>
        get() = playerRoles.filterValues { it == PlayerRole.IMPOSTOR }.keys

    val agentIds: Set<String>
        get() = playerRoles.filterValues { it == PlayerRole.NORMAL }.keys

    fun referenceWordFor(playerId: String): String? {
        val role = playerRoles[playerId] ?: PlayerRole.NORMAL
        return if (role == PlayerRole.IMPOSTOR) null else locationWord?.word
    }

    fun referenceHintFor(playerId: String): String? {
        if (!hintsEnabled) return null
        val role = playerRoles[playerId] ?: PlayerRole.NORMAL
        return if (role == PlayerRole.IMPOSTOR) null else locationWord?.hint?.ifBlank { null }
    }
}
