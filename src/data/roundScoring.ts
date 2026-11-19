export interface RoundEvaluationResult {
  countedVotes: Record<string, string>;
  voteCounts: Record<string, number>;
  topSuspectIds: Set<string>;
  caughtImpostor: boolean;
  crewWins: boolean;
  playerDeltas: Record<string, number>;
  rawKillerTargetId: string | null;
  activeNullifiedVoterId: string | null;
  activeProtectedId: string | null;
  saviorSavedTarget: boolean;
  activeDirectorId: string | null;
}

export interface RoundEvaluationInput {
  playerIds: string[];
  impostorIds: Set<string>;
  votes: Record<string, string>;
  agentPredictions: Record<string, string>;
  nullifiedVoterId: string | null;
  protectedPlayerId?: string | null;
  saviorAgentId?: string | null;
  agentDirectorId?: string | null;
  impostorAbilityEnabled: boolean;
  saviorAgentEnabled?: boolean;
  agentDirectorEnabled?: boolean;
  agentPredictionEnabled: boolean;
  impostorGuessCorrect: boolean;
}

export const RoundScoring = {
  evaluate({
    playerIds,
    impostorIds,
    votes,
    agentPredictions,
    nullifiedVoterId,
    protectedPlayerId = null,
    saviorAgentId = null,
    agentDirectorId = null,
    impostorAbilityEnabled,
    saviorAgentEnabled = false,
    agentDirectorEnabled = false,
    agentPredictionEnabled,
    impostorGuessCorrect,
  }: RoundEvaluationInput): RoundEvaluationResult {
    const playerIdSet = new Set(playerIds);

    const rawKillerTarget =
      impostorAbilityEnabled && nullifiedVoterId && playerIdSet.has(nullifiedVoterId)
        ? nullifiedVoterId
        : null;

    const activeProtected =
      saviorAgentEnabled && protectedPlayerId && playerIdSet.has(protectedPlayerId)
        ? protectedPlayerId
        : null;

    const activeSavior =
      saviorAgentEnabled &&
      saviorAgentId &&
      playerIdSet.has(saviorAgentId) &&
      !impostorIds.has(saviorAgentId)
        ? saviorAgentId
        : null;

    const activeDirector =
      agentDirectorEnabled &&
      agentDirectorId &&
      playerIdSet.has(agentDirectorId) &&
      !impostorIds.has(agentDirectorId)
        ? agentDirectorId
        : null;

    // Savior shield blocks the Ultimate Killer from voiding the protected player's vote
    const saviorSavedTarget =
      rawKillerTarget !== null &&
      activeProtected !== null &&
      rawKillerTarget === activeProtected;

    const activeNullified = saviorSavedTarget ? null : rawKillerTarget;

    // Exclude the silenced player's vote from the tally (unless saved by Savior shield)
    const countedVotes: Record<string, string> = {};
    for (const [voterId, suspectId] of Object.entries(votes)) {
      if (activeNullified === null || voterId !== activeNullified) {
        countedVotes[voterId] = suspectId;
      }
    }

    // Agent Director's vote counts as 2 votes
    const voteCounts: Record<string, number> = {};
    for (const [voterId, suspectId] of Object.entries(countedVotes)) {
      const weight = voterId === activeDirector ? 2 : 1;
      voteCounts[suspectId] = (voteCounts[suspectId] ?? 0) + weight;
    }

    const counts = Object.values(voteCounts);
    const topVoteCount = counts.length > 0 ? Math.max(...counts) : 0;
    const topSuspectIds = new Set<string>();
    if (topVoteCount > 0) {
      for (const [suspectId, count] of Object.entries(voteCounts)) {
        if (count === topVoteCount) {
          topSuspectIds.add(suspectId);
        }
      }
    }

    let caughtImpostor = false;
    for (const id of topSuspectIds) {
      if (impostorIds.has(id)) {
        caughtImpostor = true;
        break;
      }
    }
    const crewWins = caughtImpostor && !impostorGuessCorrect;

    const playerDeltas: Record<string, number> = {};
    for (const pid of playerIds) {
      const isImp = impostorIds.has(pid);
      if (isImp) {
        if (!crewWins) {
          playerDeltas[pid] = impostorGuessCorrect ? 2 : 1;
        } else {
          playerDeltas[pid] = 0;
        }
      } else {
        let base: number;
        if (agentPredictionEnabled) {
          const rawPredicted = agentPredictions[pid];
          const predicted = rawPredicted && rawPredicted.trim().length > 0 ? rawPredicted : null;
          const predictionPts =
            predicted === null ? 0 : impostorIds.has(predicted) ? 1 : -2;
          const votedFor = votes[pid];
          const voteNotVoided = pid !== activeNullified;
          const votePts =
            voteNotVoided && votedFor && impostorIds.has(votedFor) ? 2 : 0;
          base = predictionPts + votePts;
        } else {
          base = crewWins ? 1 : 0;
        }
        const saviorBonus = pid === activeSavior && saviorSavedTarget ? 1 : 0;
        playerDeltas[pid] = base + saviorBonus;
      }
    }

    return {
      countedVotes,
      voteCounts,
      topSuspectIds,
      caughtImpostor,
      crewWins,
      playerDeltas,
      rawKillerTargetId: rawKillerTarget,
      activeNullifiedVoterId: activeNullified,
      activeProtectedId: activeProtected,
      saviorSavedTarget,
      activeDirectorId: activeDirector,
    };
  },
};
