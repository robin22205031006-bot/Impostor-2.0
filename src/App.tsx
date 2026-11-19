import React, { useCallback, useMemo, useState } from 'react';
import {
  AlertCircle,
  ArrowLeft,
  LogOut,
  Settings,
  Volume2,
  VolumeX,
  X,
} from 'lucide-react';
import { SoundCuePlayer, useSoundSettings } from './audio/soundCuePlayer';
import { EliminationRevealOverlay } from './components/EliminationRevealOverlay';
import { EmergencyMeetingOverlay } from './components/EmergencyMeetingOverlay';
import {
  ImpostorAnimatedBackdrop,
  PulsingStatusDot,
} from './components/ImpostorAtmosphere';
import { SettingsMenuSheet } from './components/SettingsMenuSheet';
import { RoundScoring } from './data/roundScoring';
import { getRoleForPlayer, useLocalGame } from './hooks/useLocalGame';
import { useRoleLobbyGame } from './hooks/useRoleLobbyGame';
import { GameLobbyScreen } from './screens/GameLobbyScreen';
import {
  LocalPlayingContent,
  LocalResultsContent,
  LocalVotingContent,
} from './screens/LocalArenaStages';
import { GameSetupScreen } from './screens/GameSetupScreen';
import { RoleRevealScreen } from './screens/RoleRevealScreen';
import {
  GAME_MODE_META,
  GAME_PHASE_TITLES,
  PALETTE,
  resolveRoleIdentity,
} from './theme/palette';
import {
  EliminatedPlayerReveal,
  GamePhase,
  LocalPhase,
  PlayerRole,
  SetupWizardStep,
} from './types/game';

export const App: React.FC = () => {
  const localGame = useLocalGame();
  const lobbyGame = useRoleLobbyGame();
  const soundSettings = useSoundSettings();

  const [isRoleLobbyMode, setIsRoleLobbyMode] = useState(false);
  const [settingsOpen, setSettingsOpen] = useState(false);
  const [quitConfirmOpen, setQuitConfirmOpen] = useState(false);
  const [showEliminationOverlay, setShowEliminationOverlay] = useState(false);

  const localState = localGame.state;
  const lobbyState = lobbyGame.state;

  const isTopBarVisible =
    isRoleLobbyMode || localState.phase !== LocalPhase.SETUP;

  // Dynamic backdrop orb colors based on current stage
  const orbColors = useMemo(() => {
    if (isRoleLobbyMode) {
      if (lobbyState.phase === GamePhase.VOTING) {
        return {
          primary: PALETTE.impostorAccent,
          secondary: PALETTE.goldAccent,
          tertiary: '#46335C',
        };
      }
      if (lobbyState.phase === GamePhase.RESULTS) {
        return {
          primary: PALETTE.normalAccent,
          secondary: PALETTE.goldAccent,
          tertiary: '#46335C',
        };
      }
      return {
        primary: PALETTE.saviorAccent,
        secondary: '#5E467A',
        tertiary: '#46335C',
      };
    }

    if (localState.phase === LocalPhase.VOTING) {
      return {
        primary: PALETTE.impostorAccent,
        secondary: '#5E467A',
        tertiary: '#46335C',
      };
    }
    if (localState.phase === LocalPhase.RESULTS) {
      return {
        primary: localState.crewWon
          ? PALETTE.normalAccent
          : PALETTE.impostorAccent,
        secondary: PALETTE.goldAccent,
        tertiary: '#46335C',
      };
    }
    return {
      primary: PALETTE.goldAccent,
      secondary: '#5E467A',
      tertiary: '#46335C',
    };
  }, [isRoleLobbyMode, lobbyState.phase, localState.phase, localState.crewWon]);

  const handleBackNavigation = useCallback(() => {
    if (isRoleLobbyMode) {
      if (lobbyState.phase === GamePhase.LOBBY) {
        setIsRoleLobbyMode(false);
        localGame.setSetupStep(SetupWizardStep.MODE_PICK);
      } else {
        lobbyGame.returnToLobby();
      }
      return;
    }

    if (localState.phase !== LocalPhase.SETUP) {
      setQuitConfirmOpen(true);
    }
  }, [isRoleLobbyMode, lobbyState.phase, lobbyGame, localState.phase, localGame]);

  const handleSubmitVotingWithOverlay = useCallback(() => {
    localGame.submitResults();
    setShowEliminationOverlay(true);
  }, [localGame]);

  // Compute elimination preview data for EliminationRevealOverlay
  const eliminationPreview = useMemo(() => {
    if (localState.phase !== LocalPhase.RESULTS) {
      return {
        eliminatedPlayers: [] as EliminatedPlayerReveal[],
        verdictTitle: '',
        verdictSubtitle: '',
      };
    }

    const evaluation = RoundScoring.evaluate({
      playerIds: localState.players.map((p) => p.id),
      impostorIds: localState.impostorIds,
      votes: localState.votesByPlayerId,
      agentPredictions: localState.agentPredictions,
      nullifiedVoterId: localState.nullifiedVoterId,
      protectedPlayerId: localState.protectedPlayerId,
      saviorAgentId: localState.saviorAgentId,
      agentDirectorId: localState.agentDirectorId,
      impostorAbilityEnabled: localState.impostorAbilityEnabled,
      saviorAgentEnabled: localState.saviorAgentEnabled,
      agentDirectorEnabled: localState.agentDirectorEnabled,
      agentPredictionEnabled: localState.agentPredictionEnabled,
      impostorGuessCorrect: localState.impostorGuessCorrect,
    });

    const eliminatedPlayers: EliminatedPlayerReveal[] = localState.players
      .filter((p) => evaluation.topSuspectIds.has(p.id))
      .map((p) => {
        const role = getRoleForPlayer(localState, p.id);
        return {
          id: p.id,
          name: p.name,
          isImpostor: role === PlayerRole.IMPOSTOR,
          voteCount: localState.roundVoteCounts[p.id] ?? 0,
          roleIdentity: resolveRoleIdentity(
            role,
            p.id === localState.ultimateKillerId,
            p.id === localState.saviorAgentId,
            p.id === localState.agentDirectorId
          ),
          avatarId: p.avatarId,
          avatarPose: p.avatarPose,
        };
      });

    const verdictTitle = localState.crewWon
      ? 'CREW WINS!'
      : 'IMPOSTOR SURVIVES!';
    const verdictSubtitle = localState.impostorGuessCorrect
      ? 'The impostor guessed the exact secret word and stole the round.'
      : localState.crewWon
      ? 'The suspected impostor was voted out by the table.'
      : evaluation.topSuspectIds.size === 0
      ? 'A tied or empty ballot spared the impostor.'
      : 'An innocent agent was ejected while the impostor escaped.';

    return { eliminatedPlayers, verdictTitle, verdictSubtitle };
  }, [localState]);

  const handleRevealVerdictCue = useCallback(() => {
    SoundCuePlayer.playVotingResultsCue(localState.crewWon);
  }, [localState.crewWon]);

  const topTitle = isRoleLobbyMode
    ? `ROLE LOBBY • ${GAME_PHASE_TITLES[lobbyState.phase].toUpperCase()}`
    : `ROUND ${localState.roundNumber}`;
  const topSubtitle = isRoleLobbyMode
    ? `${lobbyState.players.length} Players • ${lobbyState.impostorCount} Impostor${
        lobbyState.impostorCount > 1 ? 's' : ''
      }`
    : `${GAME_MODE_META[localState.gameMode].shortTitle} • ${
        localState.players.length
      } Players`;

  return (
    <ImpostorAnimatedBackdrop
      primaryOrbColor={orbColors.primary}
      secondaryOrbColor={orbColors.secondary}
      tertiaryOrbColor={orbColors.tertiary}
    >
      {/* Shared Top Bar for Active Game & Role Lobby */}
      {isTopBarVisible && (
        <header className="sticky top-0 z-40 border-b border-[#423554]/70 bg-[#1D1626]/90 backdrop-blur-md">
          <div className="mx-auto flex max-w-2xl items-center justify-between gap-3 px-4 py-3">
            <div className="flex min-w-0 items-center gap-3">
              <button
                type="button"
                onClick={handleBackNavigation}
                aria-label="Back"
                className="flex h-10 w-10 shrink-0 items-center justify-center rounded-2xl border border-[#423554] bg-[#241C2F] text-[#F7F2EC] hover:border-[#F4CD6C]/60"
              >
                <ArrowLeft className="h-4 w-4" />
              </button>
              <div className="min-w-0">
                <div className="flex items-center gap-2">
                  <PulsingStatusDot
                    color={
                      isRoleLobbyMode
                        ? PALETTE.saviorAccent
                        : PALETTE.goldAccent
                    }
                    size={8}
                  />
                  <span className="truncate font-mono-label text-[10px] font-extrabold uppercase tracking-[0.2em] text-[#F4CD6C]">
                    {topTitle}
                  </span>
                </div>
                <div className="truncate text-xs font-bold text-[#B7ABC8]">
                  {topSubtitle}
                </div>
              </div>
            </div>

            <div className="flex shrink-0 items-center gap-2">
              <button
                type="button"
                onClick={() =>
                  SoundCuePlayer.setSoundEnabled(!soundSettings.soundEnabled)
                }
                aria-label={
                  soundSettings.soundEnabled ? 'Mute sound' : 'Unmute sound'
                }
                className="flex h-10 w-10 items-center justify-center rounded-2xl border border-[#423554] bg-[#241C2F] text-[#F7F2EC] hover:border-[#F4CD6C]/60"
              >
                {soundSettings.soundEnabled ? (
                  <Volume2 className="h-4 w-4 text-[#F4CD6C]" />
                ) : (
                  <VolumeX className="h-4 w-4 text-[#887B9B]" />
                )}
              </button>

              <button
                type="button"
                onClick={() => setSettingsOpen(true)}
                aria-label="Open game settings"
                className="flex h-10 w-10 items-center justify-center rounded-2xl border border-[#423554] bg-[#241C2F] text-[#F7F2EC] hover:border-[#F4CD6C]/60"
              >
                <Settings className="h-4 w-4" />
              </button>

              {!isRoleLobbyMode && (
                <button
                  type="button"
                  onClick={() => setQuitConfirmOpen(true)}
                  aria-label="Quit round"
                  className="flex h-10 w-10 items-center justify-center rounded-2xl border border-[#FF3E5B]/45 bg-[#341624] text-[#FF7A8F] hover:bg-[#491D31]"
                >
                  <LogOut className="h-4 w-4" />
                </button>
              )}
            </div>
          </div>
        </header>
      )}

      {/* Floating Error Toast Banner */}
      {localState.errorBanner && (
        <div className="fixed left-1/2 top-3 z-50 w-[calc(100%-2rem)] max-w-lg -translate-x-1/2">
          <div className="flex items-center justify-between gap-3 rounded-2xl border border-[#FF3E5B]/80 bg-[#341624]/95 px-4 py-3 text-xs font-bold text-[#FF7A8F] shadow-2xl backdrop-blur-md">
            <div className="flex items-center gap-2">
              <AlertCircle className="h-4 w-4 shrink-0" />
              <span>{localState.errorBanner}</span>
            </div>
            <button
              type="button"
              onClick={localGame.clearError}
              aria-label="Dismiss error"
              className="rounded-lg p-1 hover:bg-[#16101E]"
            >
              <X className="h-4 w-4" />
            </button>
          </div>
        </div>
      )}

      {/* Main Route Content */}
      <main className={isTopBarVisible ? 'pt-4' : ''}>
        {isRoleLobbyMode ? (
          <GameLobbyScreen
            state={lobbyState}
            onSetPlayerCount={lobbyGame.setPlayerCount}
            onSetImpostorCount={lobbyGame.setImpostorCount}
            onAssignRoles={lobbyGame.assignRoles}
            onRevealAllRoles={lobbyGame.revealAllRoles}
            onHideAllRoles={lobbyGame.hideAllRoles}
            onToggleRoleReveal={lobbyGame.toggleRoleReveal}
            onAddPlayer={lobbyGame.addPlayer}
            onRemovePlayer={lobbyGame.removePlayer}
            onSetPlayerAvatar={lobbyGame.setPlayerAvatar}
            onSetCategory={lobbyGame.setCategory}
            onSetHintsEnabled={lobbyGame.setHintsEnabled}
            onSelectNewLocationWord={lobbyGame.selectNewLocationWord}
            onStartDiscussion={lobbyGame.startDiscussion}
            onToggleTimerPause={lobbyGame.toggleTimerPause}
            onNextTurn={lobbyGame.nextTurn}
            onTriggerEmergencyMeeting={lobbyGame.triggerEmergencyMeeting}
            onStartVoting={lobbyGame.startVoting}
            onCastVote={lobbyGame.castVote}
            onFinalizeVoting={lobbyGame.finalizeVoting}
            onPlayNextRound={lobbyGame.playNextRound}
            onReturnToLobby={lobbyGame.returnToLobby}
          />
        ) : localState.phase === LocalPhase.SETUP ? (
          <GameSetupScreen
            state={localState}
            onSetSetupStep={localGame.setSetupStep}
            onEnterRoleLobbyMode={() => setIsRoleLobbyMode(true)}
            onAddPlayer={localGame.addPlayer}
            onRenamePlayer={localGame.renamePlayer}
            onRemovePlayer={localGame.removePlayer}
            onSetPlayerCount={localGame.setPlayerCount}
            onSetPlayerAvatar={localGame.setPlayerAvatar}
            onCyclePlayerPose={localGame.cyclePlayerPose}
            onSetGameMode={localGame.setGameMode}
            onSetImpostorCount={localGame.setImpostorCount}
            onSetTurnDuration={localGame.setTurnDuration}
            onSetTimerEnabled={localGame.setTimerEnabled}
            onSetHintsEnabled={localGame.setHintsEnabled}
            onSetCategoryCluesEnabled={localGame.setCategoryCluesEnabled}
            onSetImpostorAbilityEnabled={localGame.setImpostorAbilityEnabled}
            onSetSaviorAgentEnabled={localGame.setSaviorAgentEnabled}
            onSetAgentDirectorEnabled={localGame.setAgentDirectorEnabled}
            onSetAgentPredictionEnabled={localGame.setAgentPredictionEnabled}
            onSetCategory={localGame.setCategory}
            onToggleFavoritePack={localGame.toggleFavoritePack}
            onStartRound={localGame.startRound}
          />
        ) : localState.phase === LocalPhase.REVEAL ? (
          <RoleRevealScreen
            state={localState}
            onShowCard={localGame.showCurrentPlayerCard}
            onReshuffleRoles={localGame.reshuffleSessionRoles}
            onHideAndAdvance={localGame.hideAndAdvanceReveal}
            onSelectKillerTarget={localGame.setNullifiedVoter}
            onSelectSaviorProtected={localGame.setProtectedPlayer}
            onSelectAgentPrediction={localGame.setAgentPrediction}
          />
        ) : localState.phase === LocalPhase.PLAYING ? (
          <LocalPlayingContent
            state={localState}
            onToggleTimerPause={localGame.toggleTimerPause}
            onAddExtraSeconds={localGame.addExtraSeconds}
            onNextTurn={localGame.nextTurn}
            onGoToVoting={localGame.goToVoting}
            onTriggerEmergencyMeeting={localGame.triggerEmergencyMeeting}
            onUpdateImpostorWordGuess={localGame.updateImpostorWordGuess}
            onSelectKillerTarget={localGame.setNullifiedVoter}
            onSelectSaviorProtected={localGame.setProtectedPlayer}
          />
        ) : localState.phase === LocalPhase.VOTING ? (
          <LocalVotingContent
            state={localState}
            onRevealVoterBallot={localGame.revealVoterBallot}
            onCastLocalVote={localGame.castLocalVote}
            onConfirmVoteAndAdvance={localGame.confirmVoteAndAdvance}
            onUpdateImpostorWordGuess={localGame.updateImpostorWordGuess}
            onSubmitResults={handleSubmitVotingWithOverlay}
          />
        ) : (
          <LocalResultsContent
            state={localState}
            onEditSetup={(step) => localGame.returnToSetup(step)}
            onPlayAnotherRound={localGame.startRound}
          />
        )}
      </main>

      {/* Emergency Meeting Overlays */}
      <EmergencyMeetingOverlay
        alert={
          isRoleLobbyMode
            ? lobbyState.emergencyMeeting
            : localState.emergencyMeeting
        }
        onDismissToVoting={() => {
          if (isRoleLobbyMode) {
            lobbyGame.dismissEmergencyMeetingAndVote();
          } else {
            localGame.dismissEmergencyMeetingAndVote();
          }
        }}
      />

      {/* Dramatic Elimination Reveal Overlay (Pass-and-Play Results) */}
      <EliminationRevealOverlay
        visible={!isRoleLobbyMode && showEliminationOverlay}
        eliminatedPlayers={eliminationPreview.eliminatedPlayers}
        crewWon={localState.crewWon}
        verdictTitle={eliminationPreview.verdictTitle}
        verdictSubtitle={eliminationPreview.verdictSubtitle}
        onRevealVerdict={handleRevealVerdictCue}
        onContinueToResults={() => setShowEliminationOverlay(false)}
      />

      {/* Settings Sheet */}
      <SettingsMenuSheet
        isOpen={settingsOpen}
        emergencyAlertSoundEnabled={soundSettings.emergencyAlertSoundEnabled}
        soundEffectsEnabled={soundSettings.soundEnabled}
        turnDurationSeconds={
          isRoleLobbyMode
            ? lobbyState.turnDurationSeconds
            : localState.turnDurationSeconds
        }
        onToggleEmergencyAlertSound={(enabled) => {
          SoundCuePlayer.setEmergencyAlertSoundEnabled(enabled);
          lobbyGame.setEmergencyAlertSoundEnabled(enabled);
        }}
        onToggleSoundEffects={(enabled) => {
          SoundCuePlayer.setSoundEnabled(enabled);
          lobbyGame.setSoundEffectsEnabled(enabled);
        }}
        onTestEmergencySound={() => SoundCuePlayer.playEmergencyMeetingCue()}
        onSetTurnDuration={(sec) => {
          if (isRoleLobbyMode) {
            lobbyGame.setTurnDuration(sec);
          } else {
            localGame.setTurnDuration(sec);
          }
        }}
        onClose={() => setSettingsOpen(false)}
      />

      {/* Quit Round Confirmation Modal */}
      {quitConfirmOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/75 p-4 backdrop-blur-sm">
          <div className="w-full max-w-sm rounded-3xl border border-[#423554] bg-[#241C2F] p-6 shadow-2xl">
            <h3 className="font-display text-xl font-extrabold text-[#F7F2EC]">
              Leave current round?
            </h3>
            <p className="mt-2 text-sm font-medium text-[#B7ABC8]">
              Your current round progress will end and return you to the setup
              wizard. Players and scores are preserved.
            </p>
            <div className="mt-5 flex justify-end gap-2.5">
              <button
                type="button"
                onClick={() => setQuitConfirmOpen(false)}
                className="rounded-2xl border border-[#423554] bg-[#2C2339] px-4 py-2.5 text-sm font-bold text-[#F7F2EC]"
              >
                Keep Playing
              </button>
              <button
                type="button"
                onClick={() => {
                  setQuitConfirmOpen(false);
                  setShowEliminationOverlay(false);
                  localGame.returnToSetup(SetupWizardStep.PLAYERS);
                }}
                className="rounded-2xl bg-[#FF3E5B] px-5 py-2.5 text-sm font-extrabold text-white"
              >
                Leave Round
              </button>
            </div>
          </div>
        </div>
      )}
    </ImpostorAnimatedBackdrop>
  );
};

export default App;
