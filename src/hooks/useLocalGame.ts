import { useCallback, useEffect, useRef, useState } from 'react';
import { SoundCuePlayer } from '../audio/soundCuePlayer';
import { AvatarCatalog, nextAvatarPose } from '../data/avatarCatalog';
import { CategoryWordGenerator } from '../data/categoryWordGenerator';
import { CryptoRandom } from '../data/cryptoRandom';
import { RoundScoring } from '../data/roundScoring';
import { CATEGORY_ALL, WordBank } from '../data/wordBank';
import { GAME_MODE_META } from '../theme/palette';
import {
  AvatarPose,
  GameMode,
  LocalGameUiState,
  LocalPhase,
  LocalPlayer,
  PlayerRole,
  PlayerWordBriefing,
  SetupWizardStep,
} from '../types/game';

function createInitialState(): LocalGameUiState {
  return {
    phase: LocalPhase.SETUP,
    setupStep: SetupWizardStep.WELCOME,
    players: [
      { id: 'p1', name: 'Player 1', score: 0, avatarId: 'avatar_05', avatarPose: AvatarPose.ORIGINAL },
      { id: 'p2', name: 'Player 2', score: 0, avatarId: 'avatar_08', avatarPose: AvatarPose.ORIGINAL },
      { id: 'p3', name: 'Player 3', score: 0, avatarId: 'avatar_01', avatarPose: AvatarPose.ORIGINAL },
      { id: 'p4', name: 'Player 4', score: 0, avatarId: 'avatar_09', avatarPose: AvatarPose.ORIGINAL },
    ],
    gameMode: GameMode.CLASSIC,
    impostorCount: 1,
    turnDurationSeconds: 45,
    selectedCategory: CATEGORY_ALL,
    favoritePacks: new Set<string>(),
    hintsEnabled: true,
    categoryCluesEnabled: true,
    timerEnabled: true,
    impostorAbilityEnabled: false,
    saviorAgentEnabled: false,
    agentDirectorEnabled: false,
    agentPredictionEnabled: false,
    roundNumber: 0,
    activeWord: null,
    activeHint: '',
    impostorIds: new Set<string>(),
    ultimateKillerId: null,
    rawKillerTargetId: null,
    nullifiedVoterId: null,
    saviorAgentId: null,
    protectedPlayerId: null,
    saviorSavedTarget: false,
    agentDirectorId: null,
    agentPredictions: {},
    revealPlayerIndex: 0,
    isRoleCardVisible: false,
    turnOrderIds: [],
    currentTurnIndex: 0,
    remainingTurnSeconds: 45,
    isTimerPaused: false,
    votesByPlayerId: {},
    votingPlayerIndex: 0,
    isVoteCardRevealed: false,
    impostorWordGuess: '',
    impostorGuessCorrect: false,
    crewWon: false,
    roundDeltas: {},
    roundVoteCounts: {},
    usedWords: new Set<string>(),
    emergencyMeeting: {
      isActive: false,
      reporterName: '',
      reason: '',
      countdownSeconds: 0,
    },
    errorBanner: null,
  };
}

function makePlayerId(): string {
  if (typeof crypto !== 'undefined' && typeof crypto.randomUUID === 'function') {
    return crypto.randomUUID().slice(0, 8);
  }
  return Math.random().toString(36).slice(2, 10);
}

export function getRoleForPlayer(state: LocalGameUiState, playerId: String | string): PlayerRole {
  return state.impostorIds.has(String(playerId)) ? PlayerRole.IMPOSTOR : PlayerRole.NORMAL;
}

export function getWordBriefingForPlayer(
  state: LocalGameUiState,
  playerId: string
): PlayerWordBriefing | null {
  const word = state.activeWord;
  if (!word) return null;
  const role = getRoleForPlayer(state, playerId);
  const isImp = role === PlayerRole.IMPOSTOR;
  return {
    role,
    secretWord: isImp ? null : word.word,
    agentHint: isImp || !state.hintsEnabled ? null : state.activeHint.trim() || null,
    category: state.categoryCluesEnabled ? word.category : null,
    genre: state.categoryCluesEnabled ? word.genre : null,
    isSecretWordHidden: isImp,
  };
}

export function useLocalGame() {
  const [state, setState] = useState<LocalGameUiState>(() => createInitialState());
  const emergencyTimer1Ref = useRef<number | null>(null);
  const emergencyTimer2Ref = useRef<number | null>(null);

  const clearEmergencyTimers = useCallback(() => {
    if (emergencyTimer1Ref.current !== null) {
      window.clearTimeout(emergencyTimer1Ref.current);
      emergencyTimer1Ref.current = null;
    }
    if (emergencyTimer2Ref.current !== null) {
      window.clearTimeout(emergencyTimer2Ref.current);
      emergencyTimer2Ref.current = null;
    }
  }, []);

  useEffect(() => {
    return () => clearEmergencyTimers();
  }, [clearEmergencyTimers]);

  // 1-second countdown loop while in PLAYING phase with timer enabled & unpaused
  useEffect(() => {
    if (
      state.phase !== LocalPhase.PLAYING ||
      !state.timerEnabled ||
      state.isTimerPaused ||
      state.emergencyMeeting.isActive ||
      state.remainingTurnSeconds <= 0
    ) {
      return;
    }

    const intervalId = window.setInterval(() => {
      let didExpire = false;
      setState((curr) => {
        if (
          curr.phase !== LocalPhase.PLAYING ||
          curr.isTimerPaused ||
          curr.remainingTurnSeconds <= 0
        ) {
          return curr;
        }
        const nextSec = Math.max(0, curr.remainingTurnSeconds - 1);
        if (nextSec === 0) {
          didExpire = true;
        }
        return { ...curr, remainingTurnSeconds: nextSec };
      });
      if (didExpire) {
        SoundCuePlayer.playTimerExpiredCue();
      }
    }, 1000);

    return () => window.clearInterval(intervalId);
  }, [
    state.phase,
    state.timerEnabled,
    state.isTimerPaused,
    state.emergencyMeeting.isActive,
    state.remainingTurnSeconds,
  ]);

  const clearError = useCallback(() => {
    setState((curr) => ({ ...curr, errorBanner: null }));
  }, []);

  const setSetupStep = useCallback((step: SetupWizardStep) => {
    setState((curr) => ({ ...curr, setupStep: step, errorBanner: null }));
  }, []);

  const toggleFavoritePack = useCallback((category: string) => {
    setState((curr) => {
      const nextFavs = new Set(curr.favoritePacks);
      if (nextFavs.has(category)) {
        nextFavs.delete(category);
      } else {
        nextFavs.add(category);
      }
      return { ...curr, favoritePacks: nextFavs };
    });
  }, []);

  const addPlayer = useCallback((rawName: string) => {
    setState((curr) => {
      const trimmed = rawName.trim().slice(0, 24);
      const finalName = trimmed || `Player ${curr.players.length + 1}`;
      const usedIds = new Set(curr.players.map((p) => p.avatarId));
      const pickedAvatar = AvatarCatalog.nextAvailableAvatar(usedIds, curr.players.length);
      const newPlayer: LocalPlayer = {
        id: makePlayerId(),
        name: finalName,
        score: 0,
        avatarId: pickedAvatar.id,
        avatarPose: AvatarPose.ORIGINAL,
      };
      return {
        ...curr,
        players: [...curr.players, newPlayer],
        errorBanner: null,
      };
    });
  }, []);

  const renamePlayer = useCallback((playerId: string, rawName: string) => {
    const trimmed = rawName.trim().slice(0, 24);
    if (!trimmed) return;
    setState((curr) => ({
      ...curr,
      players: curr.players.map((p) => (p.id === playerId ? { ...p, name: trimmed } : p)),
    }));
  }, []);

  const setPlayerAvatar = useCallback(
    (playerId: string, avatarId: string, pose?: AvatarPose) => {
      const validAvatarId = AvatarCatalog.findById(avatarId).id;
      setState((curr) => ({
        ...curr,
        players: curr.players.map((p) =>
          p.id === playerId
            ? { ...p, avatarId: validAvatarId, avatarPose: pose ?? p.avatarPose }
            : p
        ),
      }));
    },
    []
  );

  const cyclePlayerPose = useCallback((playerId: string) => {
    setState((curr) => ({
      ...curr,
      players: curr.players.map((p) =>
        p.id === playerId ? { ...p, avatarPose: nextAvatarPose(p.avatarPose) } : p
      ),
    }));
  }, []);

  const removePlayer = useCallback((playerId: string) => {
    setState((curr) => {
      if (curr.players.length <= 2) {
        return { ...curr, errorBanner: 'At least 2 players are required.' };
      }
      const updatedPlayers = curr.players.filter((p) => p.id !== playerId);
      const maxImp = Math.max(1, updatedPlayers.length - 1);
      return {
        ...curr,
        players: updatedPlayers,
        impostorCount: Math.max(1, Math.min(curr.impostorCount, maxImp)),
        errorBanner: null,
      };
    });
  }, []);

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
      const maxImp = Math.max(1, updatedPlayers.length - 1);
      return {
        ...curr,
        players: updatedPlayers,
        impostorCount: Math.max(1, Math.min(curr.impostorCount, maxImp)),
        errorBanner: null,
      };
    });
  }, []);

  const setGameMode = useCallback((mode: GameMode) => {
    setState((curr) => ({
      ...curr,
      gameMode: mode,
      turnDurationSeconds: GAME_MODE_META[mode].defaultTurnSeconds,
    }));
  }, []);

  const setImpostorCount = useCallback((count: number) => {
    setState((curr) => {
      const maxImp = Math.max(1, curr.players.length - 1);
      return { ...curr, impostorCount: Math.max(1, Math.min(count, maxImp)) };
    });
  }, []);

  const setTurnDuration = useCallback((seconds: number) => {
    setState((curr) => ({
      ...curr,
      turnDurationSeconds: Math.max(5, Math.min(300, seconds)),
    }));
  }, []);

  const setCategory = useCallback((category: string) => {
    setState((curr) => ({ ...curr, selectedCategory: category }));
  }, []);

  const setHintsEnabled = useCallback((enabled: boolean) => {
    setState((curr) => ({ ...curr, hintsEnabled: enabled }));
  }, []);

  const setCategoryCluesEnabled = useCallback((enabled: boolean) => {
    setState((curr) => ({ ...curr, categoryCluesEnabled: enabled }));
  }, []);

  const setTimerEnabled = useCallback((enabled: boolean) => {
    setState((curr) => ({ ...curr, timerEnabled: enabled }));
  }, []);

  const setImpostorAbilityEnabled = useCallback((enabled: boolean) => {
    setState((curr) => ({ ...curr, impostorAbilityEnabled: enabled }));
  }, []);

  const setSaviorAgentEnabled = useCallback((enabled: boolean) => {
    setState((curr) => ({ ...curr, saviorAgentEnabled: enabled }));
  }, []);

  const setAgentDirectorEnabled = useCallback((enabled: boolean) => {
    setState((curr) => ({ ...curr, agentDirectorEnabled: enabled }));
  }, []);

  const setAgentPredictionEnabled = useCallback((enabled: boolean) => {
    setState((curr) => ({ ...curr, agentPredictionEnabled: enabled }));
  }, []);

  const startRound = useCallback(() => {
    clearEmergencyTimers();
    setState((curr) => {
      if (curr.players.length < 2) {
        return { ...curr, errorBanner: 'Add at least 2 players to start.' };
      }
      if (WordBank.isCustomCategory(curr.selectedCategory) && WordBank.customEntries.length === 0) {
        return {
          ...curr,
          errorBanner: 'Add at least 1 custom word first, or choose another category.',
        };
      }
      const maxImp = Math.max(1, curr.players.length - 1);
      const actualImpCount = Math.max(1, Math.min(curr.impostorCount, maxImp));
      const generatedRound = CategoryWordGenerator.generate({
        category: curr.selectedCategory,
        usedWords: curr.usedWords,
        mode: curr.gameMode,
        hintsEnabled: curr.hintsEnabled,
        categoryCluesEnabled: curr.categoryCluesEnabled,
      });
      const pickedWord = generatedRound.entry;
      const shuffledPlayers = CryptoRandom.shuffled(curr.players);
      const chosenImpostorList = shuffledPlayers.slice(0, actualImpCount).map((p) => p.id);
      const chosenImpostors = new Set(chosenImpostorList);
      const chosenKiller = curr.impostorAbilityEnabled
        ? (CryptoRandom.pickOne(chosenImpostorList) ?? null)
        : null;
      const regularAgents = CryptoRandom.shuffled(
        curr.players.filter((p) => !chosenImpostors.has(p.id)).map((p) => p.id)
      );
      const chosenSavior =
        curr.saviorAgentEnabled && regularAgents.length > 0 ? regularAgents[0] : null;
      let chosenDirector: string | null = null;
      if (curr.agentDirectorEnabled && regularAgents.length > 0) {
        if (regularAgents.length >= 2) {
          chosenDirector = regularAgents[1];
        } else if (chosenSavior === null) {
          chosenDirector = regularAgents[0];
        }
      }
      const turnOrder = CryptoRandom.shuffled(curr.players.map((p) => p.id));
      const nextUsed = new Set(curr.usedWords);
      nextUsed.add(pickedWord.word);

      return {
        ...curr,
        phase: LocalPhase.REVEAL,
        impostorCount: actualImpCount,
        roundNumber: curr.roundNumber + 1,
        activeWord: pickedWord,
        activeHint: generatedRound.agentHintText,
        impostorIds: chosenImpostors,
        ultimateKillerId: chosenKiller,
        rawKillerTargetId: null,
        nullifiedVoterId: null,
        saviorAgentId: chosenSavior,
        protectedPlayerId: null,
        saviorSavedTarget: false,
        agentDirectorId: chosenDirector,
        agentPredictions: {},
        revealPlayerIndex: 0,
        isRoleCardVisible: false,
        turnOrderIds: turnOrder,
        currentTurnIndex: 0,
        remainingTurnSeconds: curr.turnDurationSeconds,
        isTimerPaused: !curr.timerEnabled,
        votesByPlayerId: {},
        votingPlayerIndex: 0,
        isVoteCardRevealed: false,
        impostorWordGuess: '',
        impostorGuessCorrect: false,
        crewWon: false,
        roundDeltas: {},
        roundVoteCounts: {},
        usedWords: nextUsed,
        emergencyMeeting: { isActive: false, reporterName: '', reason: '', countdownSeconds: 0 },
        errorBanner: null,
      };
    });
  }, [clearEmergencyTimers]);

  const showCurrentPlayerCard = useCallback(() => {
    setState((curr) => ({ ...curr, isRoleCardVisible: true }));
  }, []);

  const reshuffleSessionRoles = useCallback(() => {
    setState((curr) => {
      if (curr.phase !== LocalPhase.REVEAL || curr.revealPlayerIndex > 0 || curr.isRoleCardVisible) {
        return curr;
      }
      const maxImp = Math.max(1, curr.players.length - 1);
      const actualImpCount = Math.max(1, Math.min(curr.impostorCount, maxImp));
      const shuffledPlayers = CryptoRandom.shuffled(curr.players);
      const chosenImpostorList = shuffledPlayers.slice(0, actualImpCount).map((p) => p.id);
      const chosenImpostors = new Set(chosenImpostorList);
      const chosenKiller = curr.impostorAbilityEnabled
        ? (CryptoRandom.pickOne(chosenImpostorList) ?? null)
        : null;
      const regularAgents = CryptoRandom.shuffled(
        curr.players.filter((p) => !chosenImpostors.has(p.id)).map((p) => p.id)
      );
      const chosenSavior =
        curr.saviorAgentEnabled && regularAgents.length > 0 ? regularAgents[0] : null;
      let chosenDirector: string | null = null;
      if (curr.agentDirectorEnabled && regularAgents.length > 0) {
        if (regularAgents.length >= 2) {
          chosenDirector = regularAgents[1];
        } else if (chosenSavior === null) {
          chosenDirector = regularAgents[0];
        }
      }
      return {
        ...curr,
        impostorIds: chosenImpostors,
        ultimateKillerId: chosenKiller,
        saviorAgentId: chosenSavior,
        agentDirectorId: chosenDirector,
        turnOrderIds: CryptoRandom.shuffled(curr.players.map((p) => p.id)),
      };
    });
  }, []);

  const hideAndAdvanceReveal = useCallback(() => {
    setState((curr) => {
      const nextIndex = curr.revealPlayerIndex + 1;
      if (nextIndex >= curr.players.length) {
        return {
          ...curr,
          phase: LocalPhase.PLAYING,
          isRoleCardVisible: false,
          currentTurnIndex: 0,
          remainingTurnSeconds: curr.turnDurationSeconds,
          isTimerPaused: !curr.timerEnabled,
        };
      }
      return {
        ...curr,
        revealPlayerIndex: nextIndex,
        isRoleCardVisible: false,
      };
    });
  }, []);

  const toggleTimerPause = useCallback(() => {
    setState((curr) => ({ ...curr, isTimerPaused: !curr.isTimerPaused }));
  }, []);

  const addExtraSeconds = useCallback((seconds = 10) => {
    setState((curr) => ({
      ...curr,
      remainingTurnSeconds: Math.min(300, curr.remainingTurnSeconds + seconds),
    }));
  }, []);

  const goToVoting = useCallback(() => {
    clearEmergencyTimers();
    setState((curr) => ({
      ...curr,
      phase: LocalPhase.VOTING,
      isTimerPaused: true,
      votesByPlayerId: {},
      votingPlayerIndex: 0,
      isVoteCardRevealed: false,
      emergencyMeeting: { isActive: false, reporterName: '', reason: '', countdownSeconds: 0 },
    }));
  }, [clearEmergencyTimers]);

  const nextTurn = useCallback(() => {
    setState((curr) => {
      const nextIndex = curr.currentTurnIndex + 1;
      if (nextIndex >= curr.turnOrderIds.length) {
        return {
          ...curr,
          phase: LocalPhase.VOTING,
          isTimerPaused: true,
          votesByPlayerId: {},
          votingPlayerIndex: 0,
          isVoteCardRevealed: false,
          emergencyMeeting: { isActive: false, reporterName: '', reason: '', countdownSeconds: 0 },
        };
      }
      return {
        ...curr,
        currentTurnIndex: nextIndex,
        remainingTurnSeconds: curr.turnDurationSeconds,
        isTimerPaused: !curr.timerEnabled,
      };
    });
  }, []);

  const setNullifiedVoter = useCallback((targetPlayerId: string | null) => {
    setState((curr) => ({
      ...curr,
      nullifiedVoterId:
        targetPlayerId === null || targetPlayerId === curr.nullifiedVoterId
          ? null
          : targetPlayerId,
    }));
  }, []);

  const setProtectedPlayer = useCallback((targetPlayerId: string | null) => {
    setState((curr) => ({
      ...curr,
      protectedPlayerId:
        targetPlayerId === null || targetPlayerId === curr.protectedPlayerId
          ? null
          : targetPlayerId,
    }));
  }, []);

  const setAgentPrediction = useCallback((agentId: string, predictedSuspectId: string | null) => {
    setState((curr) => {
      const nextMap = { ...curr.agentPredictions };
      if (predictedSuspectId === null || nextMap[agentId] === predictedSuspectId) {
        delete nextMap[agentId];
      } else {
        nextMap[agentId] = predictedSuspectId;
      }
      return { ...curr, agentPredictions: nextMap };
    });
  }, []);

  const triggerEmergencyMeeting = useCallback(
    (reporterId?: string, reason = 'Suspicious activity reported!') => {
      clearEmergencyTimers();
      setState((curr) => {
        if (curr.phase !== LocalPhase.PLAYING || curr.emergencyMeeting.isActive) return curr;
        const activeUid = curr.turnOrderIds[curr.currentTurnIndex];
        const reporterName =
          curr.players.find((p) => p.id === reporterId)?.name ??
          curr.players.find((p) => p.id === activeUid)?.name ??
          'Agent';
        SoundCuePlayer.playEmergencyMeetingCue();
        return {
          ...curr,
          isTimerPaused: true,
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
            phase: LocalPhase.VOTING,
            isTimerPaused: true,
            votesByPlayerId: {},
            votingPlayerIndex: 0,
            isVoteCardRevealed: false,
            emergencyMeeting: { isActive: false, reporterName: '', reason: '', countdownSeconds: 0 },
          };
        });
      }, 2000);
    },
    [clearEmergencyTimers]
  );

  const dismissEmergencyMeetingAndVote = useCallback(() => {
    goToVoting();
  }, [goToVoting]);

  const revealVoterBallot = useCallback(() => {
    setState((curr) => ({ ...curr, isVoteCardRevealed: true }));
  }, []);

  const castLocalVote = useCallback((voterId: string, suspectId: string) => {
    setState((curr) => ({
      ...curr,
      votesByPlayerId: { ...curr.votesByPlayerId, [voterId]: suspectId },
    }));
  }, []);

  const confirmVoteAndAdvance = useCallback(() => {
    setState((curr) => ({
      ...curr,
      votingPlayerIndex: Math.min(curr.players.length, curr.votingPlayerIndex + 1),
      isVoteCardRevealed: false,
    }));
  }, []);

  const updateImpostorWordGuess = useCallback((guess: string) => {
    setState((curr) => ({ ...curr, impostorWordGuess: guess.slice(0, 80) }));
  }, []);

  const submitResults = useCallback(() => {
    clearEmergencyTimers();
    setState((curr) => {
      const word = curr.activeWord?.word ?? '';
      const guessMatches = WordBank.isGuessMatch(curr.impostorWordGuess, word);

      const evaluated = RoundScoring.evaluate({
        playerIds: curr.players.map((p) => p.id),
        impostorIds: curr.impostorIds,
        votes: curr.votesByPlayerId,
        agentPredictions: curr.agentPredictions,
        nullifiedVoterId: curr.nullifiedVoterId,
        protectedPlayerId: curr.protectedPlayerId,
        saviorAgentId: curr.saviorAgentId,
        agentDirectorId: curr.agentDirectorId,
        impostorAbilityEnabled: curr.impostorAbilityEnabled,
        saviorAgentEnabled: curr.saviorAgentEnabled,
        agentDirectorEnabled: curr.agentDirectorEnabled,
        agentPredictionEnabled: curr.agentPredictionEnabled,
        impostorGuessCorrect: guessMatches,
      });

      const updatedPlayers = curr.players.map((player) => {
        const delta = evaluated.playerDeltas[player.id] ?? 0;
        return { ...player, score: player.score + delta };
      });

      return {
        ...curr,
        phase: LocalPhase.RESULTS,
        players: updatedPlayers,
        impostorGuessCorrect: guessMatches,
        crewWon: evaluated.crewWins,
        roundDeltas: evaluated.playerDeltas,
        roundVoteCounts: evaluated.voteCounts,
        rawKillerTargetId: evaluated.rawKillerTargetId,
        nullifiedVoterId: evaluated.activeNullifiedVoterId,
        protectedPlayerId: evaluated.activeProtectedId,
        saviorSavedTarget: evaluated.saviorSavedTarget,
        agentDirectorId: evaluated.activeDirectorId,
      };
    });
  }, [clearEmergencyTimers]);

  const returnToSetup = useCallback(
    (step: SetupWizardStep = SetupWizardStep.PLAYERS) => {
      clearEmergencyTimers();
      setState((curr) => ({
        ...curr,
        phase: LocalPhase.SETUP,
        setupStep: step,
        isRoleCardVisible: false,
        errorBanner: null,
      }));
    },
    [clearEmergencyTimers]
  );

  return {
    state,
    clearError,
    setSetupStep,
    toggleFavoritePack,
    addPlayer,
    renamePlayer,
    setPlayerAvatar,
    cyclePlayerPose,
    removePlayer,
    setPlayerCount,
    setGameMode,
    setImpostorCount,
    setTurnDuration,
    setCategory,
    setHintsEnabled,
    setCategoryCluesEnabled,
    setTimerEnabled,
    setImpostorAbilityEnabled,
    setSaviorAgentEnabled,
    setAgentDirectorEnabled,
    setAgentPredictionEnabled,
    startRound,
    showCurrentPlayerCard,
    reshuffleSessionRoles,
    hideAndAdvanceReveal,
    toggleTimerPause,
    addExtraSeconds,
    nextTurn,
    setNullifiedVoter,
    setProtectedPlayer,
    setAgentPrediction,
    triggerEmergencyMeeting,
    dismissEmergencyMeetingAndVote,
    goToVoting,
    revealVoterBallot,
    castLocalVote,
    confirmVoteAndAdvance,
    updateImpostorWordGuess,
    submitResults,
    returnToSetup,
  };
}
