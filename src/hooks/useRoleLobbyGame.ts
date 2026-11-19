import { useCallback, useEffect, useRef, useState } from 'react';
import { SoundCuePlayer } from '../audio/soundCuePlayer';
import { AvatarCatalog } from '../data/avatarCatalog';
import { CategoryWordGenerator } from '../data/categoryWordGenerator';
import { CryptoRandom } from '../data/cryptoRandom';
import { LocationWordRepository } from '../data/locationWordRepository';
import { CATEGORY_ALL } from '../data/wordBank';
import {
  AvatarPose,
  GamePhase,
  GameState,
  LocalPlayer,
  PlayerRole,
  RoundOutcome,
  TurnStatus,
} from '../types/game';

function makePlayerId(): string {
  if (typeof crypto !== 'undefined' && typeof crypto.randomUUID === 'function') {
    return crypto.randomUUID().slice(0, 8);
  }
  return Math.random().toString(36).slice(2, 10);
}

function computeTotalVotesPerPlayer(
  players: LocalPlayer[],
  votesByPlayerId: Record<string, string>
): Record<string, number> {
  const counts: Record<string, number> = {};
  Object.values(votesByPlayerId).forEach((targetId) => {
    counts[targetId] = (counts[targetId] ?? 0) + 1;
  });
  const result: Record<string, number> = {};
  players.forEach((p) => {
    result[p.id] = counts[p.id] ?? 0;
  });
  return result;
}

function buildRoleAssignments(
  players: LocalPlayer[],
  impostorCount: number
): Record<string, PlayerRole> {
  if (players.length === 0) return {};
  const maxImp = Math.max(1, players.length - 1);
  const targetImpostors = Math.max(1, Math.min(impostorCount, maxImp));
  const impostorIds = new Set(
    CryptoRandom.shuffled(players.map((p) => p.id)).slice(0, targetImpostors)
  );
  const roles: Record<string, PlayerRole> = {};
  players.forEach((p) => {
    roles[p.id] = impostorIds.has(p.id) ? PlayerRole.IMPOSTOR : PlayerRole.NORMAL;
  });
  return roles;
}

function createInitialLobbyState(): GameState {
  const defaultPlayers: LocalPlayer[] = [
    { id: 'p1', name: 'Player 1', score: 0, avatarId: 'avatar_05', avatarPose: AvatarPose.ORIGINAL },
    { id: 'p2', name: 'Player 2', score: 0, avatarId: 'avatar_08', avatarPose: AvatarPose.ORIGINAL },
    { id: 'p3', name: 'Player 3', score: 0, avatarId: 'avatar_01', avatarPose: AvatarPose.ORIGINAL },
    { id: 'p4', name: 'Player 4', score: 0, avatarId: 'avatar_09', avatarPose: AvatarPose.ORIGINAL },
  ];
  const generated = CategoryWordGenerator.generate({
    category: CATEGORY_ALL,
    hintsEnabled: true,
  }).entry;
  const used = new Set<string>([generated.word]);

  return {
    phase: GamePhase.LOBBY,
    players: defaultPlayers,
    impostorCount: 1,
    selectedCategory: CATEGORY_ALL,
    hintsEnabled: true,
    sessionSeedNumber: 1,
    playerRoles: buildRoleAssignments(defaultPlayers, 1),
    revealedRolePlayerIds: new Set<string>(),
    locationWord: generated,
    usedWords: used,
    turnOrderIds: [],
    currentTurnIndex: 0,
    turnStatus: TurnStatus.IDLE,
    currentTimerCount: 45,
    turnDurationSeconds: 45,
    votesByPlayerId: {},
    totalVotesPerPlayer: computeTotalVotesPerPlayer(defaultPlayers, {}),
    votedOutPlayerIds: [],
    roundOutcome: RoundOutcome.PENDING,
    outcomeReason: '',
    emergencyAlertSoundEnabled: SoundCuePlayer.getEmergencyAlertSoundEnabled(),
    soundEffectsEnabled: SoundCuePlayer.getSoundEnabled(),
    emergencyMeeting: {
      isActive: false,
      reporterName: '',
      reason: '',
      countdownSeconds: 0,
    },
  };
}

export function getLobbyImpostorIds(state: GameState): Set<string> {
  const ids = new Set<string>();
  Object.entries(state.playerRoles).forEach(([pid, role]) => {
    if (role === PlayerRole.IMPOSTOR) ids.add(pid);
  });
  return ids;
}

export function getActiveTurnPlayer(state: GameState): LocalPlayer | undefined {
  const activeId = state.turnOrderIds[state.currentTurnIndex];
  if (!activeId) return undefined;
  return state.players.find((p) => p.id === activeId);
}

export function getReferenceWordForPlayer(state: GameState, playerId: string): string | null {
  const role = state.playerRoles[playerId] ?? PlayerRole.NORMAL;
  if (role === PlayerRole.IMPOSTOR) return null;
  return state.locationWord?.word ?? null;
}

export function getReferenceHintForPlayer(state: GameState, playerId: string): string | null {
  const role = state.playerRoles[playerId] ?? PlayerRole.NORMAL;
  if (role === PlayerRole.IMPOSTOR || !state.hintsEnabled) return null;
  const hint = state.locationWord?.hint ?? '';
  return hint.trim() ? hint : null;
}

export function useRoleLobbyGame() {
  const [state, setState] = useState<GameState>(() => createInitialLobbyState());
  const emergencyTimer1Ref = useRef<number | null>(null);
  const emergencyTimer2Ref = useRef<number | null>(null);
  const autoAdvanceTimerRef = useRef<number | null>(null);

  const clearTimers = useCallback(() => {
    if (emergencyTimer1Ref.current !== null) {
      window.clearTimeout(emergencyTimer1Ref.current);
      emergencyTimer1Ref.current = null;
    }
    if (emergencyTimer2Ref.current !== null) {
      window.clearTimeout(emergencyTimer2Ref.current);
      emergencyTimer2Ref.current = null;
    }
    if (autoAdvanceTimerRef.current !== null) {
      window.clearTimeout(autoAdvanceTimerRef.current);
      autoAdvanceTimerRef.current = null;
    }
  }, []);

  useEffect(() => {
    return () => clearTimers();
  }, [clearTimers]);

  // Keep sound settings synced with global SoundCuePlayer
  useEffect(() => {
    return SoundCuePlayer.subscribe(() => {
      setState((curr) => ({
        ...curr,
        emergencyAlertSoundEnabled: SoundCuePlayer.getEmergencyAlertSoundEnabled(),
        soundEffectsEnabled: SoundCuePlayer.getSoundEnabled(),
      }));
    });
  }, []);

  // Discussion timer loop + 1.2s auto-advance after expiry
  useEffect(() => {
    if (
      state.phase !== GamePhase.DISCUSSION ||
      state.emergencyMeeting.isActive ||
      state.turnStatus !== TurnStatus.ACTIVE ||
      state.currentTimerCount <= 0
    ) {
      return;
    }

    const intervalId = window.setInterval(() => {
      let expiredNow = false;
      setState((curr) => {
        if (
          curr.phase !== GamePhase.DISCUSSION ||
          curr.emergencyMeeting.isActive ||
          curr.turnStatus !== TurnStatus.ACTIVE ||
          curr.currentTimerCount <= 0
        ) {
          return curr;
        }
        const nextSec = Math.max(0, curr.currentTimerCount - 1);
        if (nextSec === 0) expiredNow = true;
        return {
          ...curr,
          currentTimerCount: nextSec,
          turnStatus: nextSec === 0 ? TurnStatus.EXPIRED : TurnStatus.ACTIVE,
        };
      });

      if (expiredNow) {
        SoundCuePlayer.playTimerExpiredCue();
        if (autoAdvanceTimerRef.current !== null) {
          window.clearTimeout(autoAdvanceTimerRef.current);
        }
        autoAdvanceTimerRef.current = window.setTimeout(() => {
          setState((afterWait) => {
            if (
              afterWait.phase !== GamePhase.DISCUSSION ||
              afterWait.turnStatus !== TurnStatus.EXPIRED ||
              afterWait.emergencyMeeting.isActive
            ) {
              return afterWait;
            }
            const nextIndex = afterWait.currentTurnIndex + 1;
            if (nextIndex >= afterWait.turnOrderIds.length) {
              return {
                ...afterWait,
                phase: GamePhase.VOTING,
                turnStatus: TurnStatus.IDLE,
                votesByPlayerId: {},
                totalVotesPerPlayer: computeTotalVotesPerPlayer(afterWait.players, {}),
                votedOutPlayerIds: [],
                roundOutcome: RoundOutcome.PENDING,
                outcomeReason: '',
                emergencyMeeting: { isActive: false, reporterName: '', reason: '', countdownSeconds: 0 },
              };
            }
            return {
              ...afterWait,
              currentTurnIndex: nextIndex,
              turnStatus: TurnStatus.ACTIVE,
              currentTimerCount: afterWait.turnDurationSeconds,
            };
          });
        }, 1200);
      }
    }, 1000);

    return () => window.clearInterval(intervalId);
  }, [
    state.phase,
    state.emergencyMeeting.isActive,
    state.turnStatus,
    state.currentTimerCount,
  ]);

  const setPlayerCount = useCallback((targetCount: number) => {
    const clampedCount = Math.max(2, Math.min(16, targetCount));
    setState((curr) => {
      let updatedPlayers: LocalPlayer[];
      if (clampedCount === curr.players.length) {
        updatedPlayers = curr.players;
      } else if (clampedCount < curr.players.length) {
        updatedPlayers = curr.players.slice(0, clampedCount);
      } else {
        const used = new Set(curr.players.map((p) => p.avatarId));
        const extras: LocalPlayer[] = [];
        for (let idx = curr.players.length + 1; idx <= clampedCount; idx++) {
          const nextAvatar = AvatarCatalog.nextAvailableAvatar(used, idx - 1);
          used.add(nextAvatar.id);
          extras.push({
            id: makePlayerId(),
            name: `Player ${idx}`,
            score: 0,
            avatarId: nextAvatar.id,
            avatarPose: AvatarPose.ORIGINAL,
          });
        }
        updatedPlayers = [...curr.players, ...extras];
      }
      const validIds = new Set(updatedPlayers.map((p) => p.id));
      const maxImp = Math.max(1, updatedPlayers.length - 1);
      const clampedImp = Math.max(1, Math.min(curr.impostorCount, maxImp));
      const nextRevealed = new Set<string>();
      curr.revealedRolePlayerIds.forEach((id) => {
        if (validIds.has(id)) nextRevealed.add(id);
      });
      return {
        ...curr,
        players: updatedPlayers,
        impostorCount: clampedImp,
        playerRoles: buildRoleAssignments(updatedPlayers, clampedImp),
        revealedRolePlayerIds: nextRevealed,
        totalVotesPerPlayer: computeTotalVotesPerPlayer(updatedPlayers, {}),
      };
    });
  }, []);

  const setImpostorCount = useCallback((count: number) => {
    setState((curr) => {
      const maxImp = Math.max(1, curr.players.length - 1);
      const clamped = Math.max(1, Math.min(count, maxImp));
      return {
        ...curr,
        impostorCount: clamped,
        playerRoles: buildRoleAssignments(curr.players, clamped),
      };
    });
  }, []);

  const assignRoles = useCallback(() => {
    setState((curr) => {
      const maxImp = Math.max(1, curr.players.length - 1);
      const clamped = Math.max(1, Math.min(curr.impostorCount, maxImp));
      return {
        ...curr,
        impostorCount: clamped,
        sessionSeedNumber: curr.sessionSeedNumber + 1,
        playerRoles: buildRoleAssignments(curr.players, clamped),
        revealedRolePlayerIds: new Set<string>(),
      };
    });
  }, []);

  const setCategory = useCallback((category: string) => {
    setState((curr) => {
      const exclude = new Set(curr.usedWords);
      if (curr.locationWord?.word) exclude.add(curr.locationWord.word);
      const generated = CategoryWordGenerator.generate({
        category,
        usedWords: exclude,
        hintsEnabled: curr.hintsEnabled,
      }).entry;
      const nextUsed = new Set(curr.usedWords);
      nextUsed.add(generated.word);
      return {
        ...curr,
        selectedCategory: category,
        locationWord: generated,
        usedWords: nextUsed,
      };
    });
  }, []);

  const setHintsEnabled = useCallback((enabled: boolean) => {
    setState((curr) => ({ ...curr, hintsEnabled: enabled }));
  }, []);

  const selectNewLocationWord = useCallback(() => {
    setState((curr) => {
      const exclude = new Set<string>(curr.locationWord?.word ? [curr.locationWord.word] : []);
      const generated =
        curr.selectedCategory === 'Locations'
          ? LocationWordRepository.pickRandomLocationWord(exclude)
          : CategoryWordGenerator.generate({
              category: curr.selectedCategory,
              usedWords: new Set([...curr.usedWords, ...exclude]),
              hintsEnabled: curr.hintsEnabled,
            }).entry;
      const nextUsed = new Set(curr.usedWords);
      nextUsed.add(generated.word);
      return {
        ...curr,
        locationWord: generated,
        usedWords: nextUsed,
      };
    });
  }, []);

  const toggleRoleReveal = useCallback((playerId: string) => {
    setState((curr) => {
      const next = new Set(curr.revealedRolePlayerIds);
      if (next.has(playerId)) next.delete(playerId);
      else next.add(playerId);
      return { ...curr, revealedRolePlayerIds: next };
    });
  }, []);

  const revealAllRoles = useCallback(() => {
    setState((curr) => ({
      ...curr,
      revealedRolePlayerIds: new Set(curr.players.map((p) => p.id)),
    }));
  }, []);

  const hideAllRoles = useCallback(() => {
    setState((curr) => ({ ...curr, revealedRolePlayerIds: new Set<string>() }));
  }, []);

  const addPlayer = useCallback((rawName: string) => {
    const trimmed = rawName.trim().slice(0, 24);
    setState((curr) => {
      const finalName = trimmed || `Player ${curr.players.length + 1}`;
      const used = new Set(curr.players.map((p) => p.avatarId));
      const nextAvatar = AvatarCatalog.nextAvailableAvatar(used, curr.players.length);
      const newPlayer: LocalPlayer = {
        id: makePlayerId(),
        name: finalName,
        score: 0,
        avatarId: nextAvatar.id,
        avatarPose: AvatarPose.ORIGINAL,
      };
      const updatedPlayers = [...curr.players, newPlayer];
      const maxImp = Math.max(1, updatedPlayers.length - 1);
      const clampedImp = Math.max(1, Math.min(curr.impostorCount, maxImp));
      return {
        ...curr,
        players: updatedPlayers,
        impostorCount: clampedImp,
        playerRoles: buildRoleAssignments(updatedPlayers, clampedImp),
        totalVotesPerPlayer: computeTotalVotesPerPlayer(updatedPlayers, {}),
      };
    });
  }, []);

  const setPlayerAvatar = useCallback(
    (playerId: string, avatarId: string, pose?: AvatarPose) => {
      const validId = AvatarCatalog.findById(avatarId).id;
      setState((curr) => ({
        ...curr,
        players: curr.players.map((p) =>
          p.id === playerId ? { ...p, avatarId: validId, avatarPose: pose ?? p.avatarPose } : p
        ),
      }));
    },
    []
  );

  const removePlayer = useCallback((playerId: string) => {
    setState((curr) => {
      if (curr.players.length <= 2) return curr;
      const updatedPlayers = curr.players.filter((p) => p.id !== playerId);
      const maxImp = Math.max(1, updatedPlayers.length - 1);
      const clampedImp = Math.max(1, Math.min(curr.impostorCount, maxImp));
      const nextRevealed = new Set(curr.revealedRolePlayerIds);
      nextRevealed.delete(playerId);
      return {
        ...curr,
        players: updatedPlayers,
        impostorCount: clampedImp,
        playerRoles: buildRoleAssignments(updatedPlayers, clampedImp),
        revealedRolePlayerIds: nextRevealed,
        totalVotesPerPlayer: computeTotalVotesPerPlayer(updatedPlayers, {}),
      };
    });
  }, []);

  const setTurnDuration = useCallback((seconds: number) => {
    const clamped = Math.max(5, Math.min(300, seconds));
    setState((curr) => ({
      ...curr,
      turnDurationSeconds: clamped,
      currentTimerCount:
        curr.phase === GamePhase.LOBBY ? clamped : Math.min(curr.currentTimerCount, clamped),
    }));
  }, []);

  const setEmergencyAlertSoundEnabled = useCallback((enabled: boolean) => {
    SoundCuePlayer.setEmergencyAlertSoundEnabled(enabled);
    setState((curr) => ({ ...curr, emergencyAlertSoundEnabled: enabled }));
  }, []);

  const setSoundEffectsEnabled = useCallback((enabled: boolean) => {
    SoundCuePlayer.setSoundEnabled(enabled);
    setState((curr) => ({ ...curr, soundEffectsEnabled: enabled }));
  }, []);

  const startDiscussion = useCallback(() => {
    clearTimers();
    setState((curr) => {
      const maxImp = Math.max(1, curr.players.length - 1);
      const clampedImp = Math.max(1, Math.min(curr.impostorCount, maxImp));
      const playerIdSet = new Set(curr.players.map((p) => p.id));
      const rolesMatch =
        Object.keys(curr.playerRoles).length === playerIdSet.size &&
        Array.from(playerIdSet).every((id) => id in curr.playerRoles);
      const roles = rolesMatch ? curr.playerRoles : buildRoleAssignments(curr.players, clampedImp);
      const selectedLocation =
        curr.locationWord ??
        CategoryWordGenerator.generate({
          category: curr.selectedCategory,
          usedWords: curr.usedWords,
          hintsEnabled: curr.hintsEnabled,
        }).entry;
      const turnOrder = CryptoRandom.shuffled(curr.players.map((p) => p.id));
      return {
        ...curr,
        phase: GamePhase.DISCUSSION,
        impostorCount: clampedImp,
        playerRoles: roles,
        locationWord: selectedLocation,
        turnOrderIds: turnOrder,
        currentTurnIndex: 0,
        turnStatus: TurnStatus.ACTIVE,
        currentTimerCount: curr.turnDurationSeconds,
        votesByPlayerId: {},
        totalVotesPerPlayer: computeTotalVotesPerPlayer(curr.players, {}),
        votedOutPlayerIds: [],
        roundOutcome: RoundOutcome.PENDING,
        outcomeReason: '',
        emergencyMeeting: { isActive: false, reporterName: '', reason: '', countdownSeconds: 0 },
      };
    });
  }, [clearTimers]);

  const toggleTimerPause = useCallback(() => {
    setState((curr) => {
      if (curr.phase !== GamePhase.DISCUSSION || curr.emergencyMeeting.isActive) return curr;
      let nextStatus = curr.turnStatus;
      if (curr.turnStatus === TurnStatus.ACTIVE) nextStatus = TurnStatus.PAUSED;
      else if (curr.turnStatus === TurnStatus.PAUSED) {
        nextStatus = curr.currentTimerCount > 0 ? TurnStatus.ACTIVE : TurnStatus.EXPIRED;
      }
      return { ...curr, turnStatus: nextStatus };
    });
  }, []);

  const startVoting = useCallback(() => {
    clearTimers();
    setState((curr) => ({
      ...curr,
      phase: GamePhase.VOTING,
      turnStatus: TurnStatus.IDLE,
      votesByPlayerId: {},
      totalVotesPerPlayer: computeTotalVotesPerPlayer(curr.players, {}),
      votedOutPlayerIds: [],
      roundOutcome: RoundOutcome.PENDING,
      outcomeReason: '',
      emergencyMeeting: { isActive: false, reporterName: '', reason: '', countdownSeconds: 0 },
    }));
  }, [clearTimers]);

  const nextTurn = useCallback(() => {
    if (autoAdvanceTimerRef.current !== null) {
      window.clearTimeout(autoAdvanceTimerRef.current);
      autoAdvanceTimerRef.current = null;
    }
    setState((curr) => {
      if (curr.phase !== GamePhase.DISCUSSION) return curr;
      const nextIndex = curr.currentTurnIndex + 1;
      if (nextIndex >= curr.turnOrderIds.length) {
        return {
          ...curr,
          phase: GamePhase.VOTING,
          turnStatus: TurnStatus.IDLE,
          votesByPlayerId: {},
          totalVotesPerPlayer: computeTotalVotesPerPlayer(curr.players, {}),
          votedOutPlayerIds: [],
          roundOutcome: RoundOutcome.PENDING,
          outcomeReason: '',
          emergencyMeeting: { isActive: false, reporterName: '', reason: '', countdownSeconds: 0 },
        };
      }
      return {
        ...curr,
        currentTurnIndex: nextIndex,
        turnStatus: TurnStatus.ACTIVE,
        currentTimerCount: curr.turnDurationSeconds,
      };
    });
  }, []);

  const triggerEmergencyMeeting = useCallback(
    (reporterId?: string, reason = 'Suspicious activity reported!') => {
      clearTimers();
      setState((curr) => {
        if (curr.phase !== GamePhase.DISCUSSION || curr.emergencyMeeting.isActive) return curr;
        const activePlayer = getActiveTurnPlayer(curr);
        const reporterName =
          curr.players.find((p) => p.id === reporterId)?.name ??
          activePlayer?.name ??
          'Agent';
        SoundCuePlayer.playEmergencyMeetingCue();
        return {
          ...curr,
          turnStatus: TurnStatus.PAUSED,
          emergencyMeeting: {
            isActive: true,
            reporterName,
            reason,
            countdownSeconds: 2,
          },
        };
      });

      emergencyTimer1Ref.current = window.setTimeout(() => {
        setState((curr) =>
          curr.emergencyMeeting.isActive
            ? {
                ...curr,
                emergencyMeeting: { ...curr.emergencyMeeting, countdownSeconds: 1 },
              }
            : curr
        );
      }, 1000);

      emergencyTimer2Ref.current = window.setTimeout(() => {
        setState((curr) => {
          if (!curr.emergencyMeeting.isActive) return curr;
          return {
            ...curr,
            phase: GamePhase.VOTING,
            turnStatus: TurnStatus.IDLE,
            votesByPlayerId: {},
            totalVotesPerPlayer: computeTotalVotesPerPlayer(curr.players, {}),
            votedOutPlayerIds: [],
            roundOutcome: RoundOutcome.PENDING,
            outcomeReason: '',
            emergencyMeeting: { isActive: false, reporterName: '', reason: '', countdownSeconds: 0 },
          };
        });
      }, 2000);
    },
    [clearTimers]
  );

  const dismissEmergencyMeetingAndVote = useCallback(() => {
    startVoting();
  }, [startVoting]);

  const castVote = useCallback((voterId: string, suspectId: string) => {
    if (voterId === suspectId) return;
    setState((curr) => {
      if (curr.phase !== GamePhase.VOTING) return curr;
      const updatedVotes = { ...curr.votesByPlayerId, [voterId]: suspectId };
      return {
        ...curr,
        votesByPlayerId: updatedVotes,
        totalVotesPerPlayer: computeTotalVotesPerPlayer(curr.players, updatedVotes),
      };
    });
  }, []);

  const finalizeVoting = useCallback(() => {
    clearTimers();
    let impostorCaughtForCue = false;
    setState((curr) => {
      const tally = computeTotalVotesPerPlayer(curr.players, curr.votesByPlayerId);
      const values = Object.values(tally);
      const maxVotes = values.length > 0 ? Math.max(...values) : 0;
      const topIds =
        maxVotes > 0
          ? Object.entries(tally)
              .filter(([, count]) => count === maxVotes)
              .map(([id]) => id)
          : [];
      const impostorIds = getLobbyImpostorIds(curr);
      const impostorVotedOut = topIds.some((id) => impostorIds.has(id));
      impostorCaughtForCue = impostorVotedOut;
      const outcome = impostorVotedOut ? RoundOutcome.VICTORY : RoundOutcome.DEFEAT;
      const votedNames = curr.players
        .filter((p) => topIds.includes(p.id))
        .map((p) => p.name)
        .join(', ');
      let reason: string;
      if (topIds.length === 0) {
        reason = 'No votes were cast — the Impostors achieved their goal!';
      } else if (impostorVotedOut) {
        reason = `${votedNames} was voted out — the Impostors have been eliminated!`;
      } else if (topIds.length > 1) {
        reason = `Tie vote (${votedNames}) — the Impostors achieved their goal!`;
      } else {
        reason = `${votedNames} was ejected, but was innocent — the Impostors achieved their goal!`;
      }
      return {
        ...curr,
        phase: GamePhase.RESULTS,
        turnStatus: TurnStatus.IDLE,
        totalVotesPerPlayer: tally,
        votedOutPlayerIds: topIds,
        roundOutcome: outcome,
        outcomeReason: reason,
      };
    });
    SoundCuePlayer.playVotingResultsCue(impostorCaughtForCue);
  }, [clearTimers]);

  const playNextRound = useCallback(() => {
    clearTimers();
    setState((curr) => {
      const maxImp = Math.max(1, curr.players.length - 1);
      const clampedImp = Math.max(1, Math.min(curr.impostorCount, maxImp));
      const newRoles = buildRoleAssignments(curr.players, clampedImp);
      const exclude = new Set<string>(curr.locationWord?.word ? [curr.locationWord.word] : []);
      const generated =
        curr.selectedCategory === 'Locations'
          ? LocationWordRepository.pickRandomLocationWord(exclude)
          : CategoryWordGenerator.generate({
              category: curr.selectedCategory,
              usedWords: new Set([...curr.usedWords, ...exclude]),
              hintsEnabled: curr.hintsEnabled,
            }).entry;
      const nextUsed = new Set(curr.usedWords);
      nextUsed.add(generated.word);
      const turnOrder = CryptoRandom.shuffled(curr.players.map((p) => p.id));

      return {
        ...curr,
        phase: GamePhase.DISCUSSION,
        impostorCount: clampedImp,
        sessionSeedNumber: curr.sessionSeedNumber + 1,
        playerRoles: newRoles,
        revealedRolePlayerIds: new Set<string>(),
        locationWord: generated,
        usedWords: nextUsed,
        turnOrderIds: turnOrder,
        currentTurnIndex: 0,
        turnStatus: TurnStatus.ACTIVE,
        currentTimerCount: curr.turnDurationSeconds,
        votesByPlayerId: {},
        totalVotesPerPlayer: computeTotalVotesPerPlayer(curr.players, {}),
        votedOutPlayerIds: [],
        roundOutcome: RoundOutcome.PENDING,
        outcomeReason: '',
        emergencyMeeting: { isActive: false, reporterName: '', reason: '', countdownSeconds: 0 },
      };
    });
  }, [clearTimers]);

  const returnToLobby = useCallback(() => {
    clearTimers();
    setState((curr) => ({
      ...curr,
      phase: GamePhase.LOBBY,
      turnStatus: TurnStatus.IDLE,
      currentTurnIndex: 0,
      currentTimerCount: curr.turnDurationSeconds,
      votesByPlayerId: {},
      totalVotesPerPlayer: computeTotalVotesPerPlayer(curr.players, {}),
      votedOutPlayerIds: [],
      roundOutcome: RoundOutcome.PENDING,
      outcomeReason: '',
      emergencyMeeting: { isActive: false, reporterName: '', reason: '', countdownSeconds: 0 },
    }));
  }, [clearTimers]);

  return {
    state,
    setPlayerCount,
    setImpostorCount,
    assignRoles,
    setCategory,
    setHintsEnabled,
    selectNewLocationWord,
    toggleRoleReveal,
    revealAllRoles,
    hideAllRoles,
    addPlayer,
    setPlayerAvatar,
    removePlayer,
    setTurnDuration,
    setEmergencyAlertSoundEnabled,
    setSoundEffectsEnabled,
    startDiscussion,
    toggleTimerPause,
    nextTurn,
    triggerEmergencyMeeting,
    dismissEmergencyMeetingAndVote,
    startVoting,
    castVote,
    finalizeVoting,
    playNextRound,
    returnToLobby,
  };
}
