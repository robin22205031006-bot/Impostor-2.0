package com.example.data

data class RoundEvaluationResult(
    val countedVotes: Map<String, String>,
    val voteCounts: Map<String, Int>,
    val topSuspectIds: Set<String>,
    val caughtImpostor: Boolean,
    val crewWins: Boolean,
    val playerDeltas: Map<String, Int>,
    val rawKillerTargetId: String?,
    val activeNullifiedVoterId: String?,
    val activeProtectedId: String?,
    val saviorSavedTarget: Boolean,
    val activeDirectorId: String?
)

object RoundScoring {

    fun evaluate(
        playerIds: List<String>,
        impostorIds: Set<String>,
        votes: Map<String, String>,
        agentPredictions: Map<String, String>,
        nullifiedVoterId: String?,
        protectedPlayerId: String? = null,
        saviorAgentId: String? = null,
        agentDirectorId: String? = null,
        impostorAbilityEnabled: Boolean,
        saviorAgentEnabled: Boolean = false,
        agentDirectorEnabled: Boolean = false,
        agentPredictionEnabled: Boolean,
        impostorGuessCorrect: Boolean
    ): RoundEvaluationResult {
        val rawKillerTarget = if (impostorAbilityEnabled && !nullifiedVoterId.isNullOrBlank()) {
            nullifiedVoterId.takeIf { it in playerIds }
        } else {
            null
        }

        val activeProtected = if (saviorAgentEnabled && !protectedPlayerId.isNullOrBlank()) {
            protectedPlayerId.takeIf { it in playerIds }
        } else {
            null
        }

        val activeSavior = if (saviorAgentEnabled && !saviorAgentId.isNullOrBlank()) {
            saviorAgentId.takeIf { it in playerIds && it !in impostorIds }
        } else {
            null
        }

        val activeDirector = if (agentDirectorEnabled && !agentDirectorId.isNullOrBlank()) {
            agentDirectorId.takeIf { it in playerIds && it !in impostorIds }
        } else {
            null
        }

        // Savior shield blocks the Ultimate Killer from voiding the protected player's vote
        val saviorSavedTarget = rawKillerTarget != null &&
            activeProtected != null &&
            rawKillerTarget == activeProtected

        val activeNullified = if (saviorSavedTarget) null else rawKillerTarget

        // Exclude the silenced player's vote from the tally (unless saved by Savior shield)
        val countedVotes = if (activeNullified != null) {
            votes.filterKeys { voterId -> voterId != activeNullified }
        } else {
            votes
        }

        // Agent Director's vote counts as 2 votes
        val voteCounts = mutableMapOf<String, Int>()
        countedVotes.forEach { (voterId, suspectId) ->
            val weight = if (voterId == activeDirector) 2 else 1
            voteCounts[suspectId] = (voteCounts[suspectId] ?: 0) + weight
        }

        val topVoteCount = voteCounts.values.maxOrNull() ?: 0
        val topSuspects = if (topVoteCount > 0) {
            voteCounts.filterValues { it == topVoteCount }.keys
        } else {
            emptySet()
        }
        val caughtImpostor = topSuspects.any { it in impostorIds }
        val crewWins = caughtImpostor && !impostorGuessCorrect

        val deltas = buildMap {
            playerIds.forEach { pid ->
                val isImp = pid in impostorIds
                val delta = if (isImp) {
                    if (!crewWins) {
                        if (impostorGuessCorrect) 2 else 1
                    } else {
                        0
                    }
                } else {
                    val base = if (agentPredictionEnabled) {
                        val predicted = agentPredictions[pid]?.takeIf { it.isNotBlank() }
                        val predictionPts = when {
                            predicted == null -> 0
                            predicted in impostorIds -> 1
                            else -> -2
                        }
                        val votedFor = votes[pid]
                        val voteNotVoided = pid != activeNullified
                        val votePts = if (voteNotVoided && votedFor != null && votedFor in impostorIds) {
                            2
                        } else {
                            0
                        }
                        predictionPts + votePts
                    } else {
                        if (crewWins) 1 else 0
                    }
                    // Savior earns +1 extra point when protecting the exact player the impostor tried to kill (no deduction if missed)
                    val saviorBonus = if (pid == activeSavior && saviorSavedTarget) 1 else 0
                    // Agent Director suffers no deduction for wrong vote; prediction penalty (-2) still applies if predicted wrong
                    base + saviorBonus
                }
                put(pid, delta)
            }
        }

        return RoundEvaluationResult(
            countedVotes = countedVotes,
            voteCounts = voteCounts,
            topSuspectIds = topSuspects,
            caughtImpostor = caughtImpostor,
            crewWins = crewWins,
            playerDeltas = deltas,
            rawKillerTargetId = rawKillerTarget,
            activeNullifiedVoterId = activeNullified,
            activeProtectedId = activeProtected,
            saviorSavedTarget = saviorSavedTarget,
            activeDirectorId = activeDirector
        )
    }
}
