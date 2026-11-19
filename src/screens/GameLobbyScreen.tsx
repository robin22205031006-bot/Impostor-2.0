import React, { useState } from 'react';
import {
  AlertTriangle,
  Eye,
  EyeOff,
  Layers,
  Pause,
  Play,
  Plus,
  RefreshCw,
  Shuffle,
  Siren,
  SkipForward,
  Sparkles,
  Trash2,
  Users,
  Vote,
  X,
} from 'lucide-react';
import { SoundCuePlayer } from '../audio/soundCuePlayer';
import { CharacterAvatarThumbnail } from '../components/AvatarCardGraphics';
import { AvatarPickerSheet } from '../components/AvatarPickerSheet';
import {
  AddOrRenamePlayerDialog,
  GoldCtaButton,
  PlumStepperControl,
  PlumTogglePill,
} from '../components/ImpostorWizardComponents';
import { RoleEmblemChip } from '../components/RoleEmblem';
import { RoundOutcomeScreen } from '../components/RoundOutcomeScreen';
import {
  CircularCountdownTimer,
  TurnTimerBar,
} from '../components/TurnTimerBar';
import { VotingScreen } from '../components/VotingScreen';
import { AvatarCatalog } from '../data/avatarCatalog';
import { CATEGORY_ALL, useWordBank } from '../data/wordBank';
import {
  getActiveTurnPlayer,
  getReferenceHintForPlayer,
  getReferenceWordForPlayer,
} from '../hooks/useRoleLobbyGame';
import { getRoleVisualStyle, PALETTE } from '../theme/palette';
import {
  AvatarPose,
  GamePhase,
  GameRoleIdentity,
  GameState,
  PlayerRole,
  TurnStatus,
} from '../types/game';

interface GameLobbyScreenProps {
  state: GameState;
  onSetPlayerCount: (count: number) => void;
  onSetImpostorCount: (count: number) => void;
  onAssignRoles: () => void;
  onRevealAllRoles: () => void;
  onHideAllRoles: () => void;
  onToggleRoleReveal: (playerId: string) => void;
  onAddPlayer: (name: string) => void;
  onRemovePlayer: (playerId: string) => void;
  onSetPlayerAvatar: (playerId: string, avatarId: string, pose?: AvatarPose) => void;
  onSetCategory: (category: string) => void;
  onSetHintsEnabled: (enabled: boolean) => void;
  onSelectNewLocationWord: () => void;
  onStartDiscussion: () => void;
  onToggleTimerPause: () => void;
  onNextTurn: () => void;
  onTriggerEmergencyMeeting: (reporterId?: string, reason?: string) => void;
  onStartVoting: () => void;
  onCastVote: (voterId: string, suspectId: string) => void;
  onFinalizeVoting: () => void;
  onPlayNextRound: () => void;
  onReturnToLobby: () => void;
}

const PHASE_STEPS: Array<{ phase: GamePhase; label: string }> = [
  { phase: GamePhase.LOBBY, label: 'Lobby' },
  { phase: GamePhase.DISCUSSION, label: 'Discuss' },
  { phase: GamePhase.VOTING, label: 'Vote' },
  { phase: GamePhase.RESULTS, label: 'Results' },
];

const TURN_STATUS_LABELS: Record<TurnStatus, string> = {
  [TurnStatus.IDLE]: 'IDLE',
  [TurnStatus.ACTIVE]: 'ACTIVE',
  [TurnStatus.PAUSED]: 'PAUSED',
  [TurnStatus.EXPIRED]: 'EXPIRED',
};

export const GameLobbyScreen: React.FC<GameLobbyScreenProps> = ({
  state,
  onSetPlayerCount,
  onSetImpostorCount,
  onAssignRoles,
  onRevealAllRoles,
  onHideAllRoles,
  onToggleRoleReveal,
  onAddPlayer,
  onRemovePlayer,
  onSetPlayerAvatar,
  onSetCategory,
  onSetHintsEnabled,
  onSelectNewLocationWord,
  onStartDiscussion,
  onToggleTimerPause,
  onNextTurn,
  onTriggerEmergencyMeeting,
  onStartVoting,
  onCastVote,
  onFinalizeVoting,
  onPlayNextRound,
  onReturnToLobby,
}) => {
  const wordBankSnapshot = useWordBank();
  const [addPlayerOpen, setAddPlayerOpen] = useState(false);
  const [avatarDeckPlayerId, setAvatarDeckPlayerId] = useState<string | null>(
    null
  );
  const [peekWordOpen, setPeekWordOpen] = useState(false);
  const [referencePlayerId, setReferencePlayerId] = useState<string | null>(
    null
  );
  const [emergencyPickerOpen, setEmergencyPickerOpen] = useState(false);

  const maxImpostors = Math.max(1, state.players.length - 1);

  return (
    <div className="mx-auto flex w-full max-w-2xl flex-col gap-5 px-5 pb-12 pt-2">
      {/* 4-Phase Stepper */}
      <div className="grid grid-cols-4 gap-2 rounded-2xl border border-[#423554] bg-[#241C2F] p-1.5">
        {PHASE_STEPS.map((step) => {
          const active = state.phase === step.phase;
          return (
            <div
              key={step.phase}
              className={`rounded-xl py-2 text-center font-mono-label text-[11px] font-extrabold uppercase tracking-wider transition ${
                active
                  ? 'bg-[#F4CD6C] text-[#1D1626]'
                  : 'text-[#887B9B]'
              }`}
            >
              {step.label}
            </div>
          );
        })}
      </div>

      {/* PHASE 1: LOBBY */}
      {state.phase === GamePhase.LOBBY && (
        <div className="space-y-4">
          {/* Deal Control Card */}
          <div className="rounded-3xl border border-[#423554] bg-[#241C2F] p-5">
            <div className="flex flex-wrap items-center justify-between gap-3">
              <div>
                <div className="font-mono-label text-[10px] font-bold uppercase tracking-[0.22em] text-[#F4CD6C]">
                  DEAL #{state.sessionSeedNumber}
                </div>
                <h2 className="mt-0.5 font-display text-2xl font-extrabold text-[#F7F2EC]">
                  Role Assignment Table
                </h2>
              </div>
              <div className="flex flex-wrap gap-2">
                <button
                  type="button"
                  onClick={onAssignRoles}
                  className="inline-flex items-center gap-1.5 rounded-xl bg-[#F4CD6C] px-3.5 py-2 text-xs font-extrabold text-[#1D1626] hover:bg-[#FFDE8A]"
                >
                  <Shuffle className="h-3.5 w-3.5" />
                  <span>Deal</span>
                </button>
                <button
                  type="button"
                  onClick={onRevealAllRoles}
                  className="inline-flex items-center gap-1 rounded-xl border border-[#423554] bg-[#16101E] px-3 py-2 text-xs font-bold text-[#F7F2EC] hover:border-[#F4CD6C]/50"
                >
                  <Eye className="h-3.5 w-3.5 text-[#1AE682]" />
                  <span>Reveal All</span>
                </button>
                <button
                  type="button"
                  onClick={onHideAllRoles}
                  className="inline-flex items-center gap-1 rounded-xl border border-[#423554] bg-[#16101E] px-3 py-2 text-xs font-bold text-[#B7ABC8] hover:border-[#F4CD6C]/50"
                >
                  <EyeOff className="h-3.5 w-3.5" />
                  <span>Hide All</span>
                </button>
              </div>
            </div>

            {/* Player & Impostor Steppers */}
            <div className="mt-4 grid grid-cols-1 gap-3 sm:grid-cols-2">
              <div className="flex items-center justify-between rounded-2xl border border-[#423554] bg-[#16101E] px-4 py-3">
                <div>
                  <div className="font-display text-sm font-extrabold text-[#F7F2EC]">
                    Players
                  </div>
                  <div className="text-[11px] text-[#887B9B]">2–16 seats</div>
                </div>
                <PlumStepperControl
                  value={state.players.length}
                  min={2}
                  max={16}
                  label="players"
                  onDecrement={() => onSetPlayerCount(state.players.length - 1)}
                  onIncrement={() => onSetPlayerCount(state.players.length + 1)}
                />
              </div>

              <div className="flex items-center justify-between rounded-2xl border border-[#423554] bg-[#16101E] px-4 py-3">
                <div>
                  <div className="font-display text-sm font-extrabold text-[#F7F2EC]">
                    Impostors
                  </div>
                  <div className="text-[11px] text-[#887B9B]">
                    Max {maxImpostors}
                  </div>
                </div>
                <PlumStepperControl
                  value={state.impostorCount}
                  min={1}
                  max={maxImpostors}
                  label="impostors"
                  accentColor={PALETTE.impostorAccent}
                  onDecrement={() => onSetImpostorCount(state.impostorCount - 1)}
                  onIncrement={() => onSetImpostorCount(state.impostorCount + 1)}
                />
              </div>
            </div>
          </div>

          {/* Glowing Lobby Role Cards */}
          <div className="grid grid-cols-1 gap-3 sm:grid-cols-2">
            {state.players.map((player) => {
              const role = state.playerRoles[player.id] ?? PlayerRole.NORMAL;
              const isImpostor = role === PlayerRole.IMPOSTOR;
              const revealed = state.revealedRolePlayerIds.has(player.id);
              const identity = isImpostor
                ? GameRoleIdentity.IMPOSTOR
                : GameRoleIdentity.AGENT;
              const roleStyle = getRoleVisualStyle(identity);
              const char = AvatarCatalog.findById(player.avatarId);

              return (
                <div
                  key={player.id}
                  onClick={() => {
                    if (!revealed) {
                      SoundCuePlayer.playRoleRevealCue(isImpostor);
                    }
                    onToggleRoleReveal(player.id);
                  }}
                  role="button"
                  tabIndex={0}
                  onKeyDown={(e) => {
                    if (e.key === 'Enter' || e.key === ' ') {
                      e.preventDefault();
                      if (!revealed) {
                        SoundCuePlayer.playRoleRevealCue(isImpostor);
                      }
                      onToggleRoleReveal(player.id);
                    }
                  }}
                  className="cursor-pointer rounded-3xl border-2 p-4 transition active:scale-[0.99]"
                  style={{
                    borderColor: revealed
                      ? roleStyle.accent
                      : PALETTE.obsidianOutline,
                    background: revealed
                      ? `linear-gradient(155deg, ${roleStyle.deepSurface} 0%, #1D1626 100%)`
                      : '#241C2F',
                    boxShadow: revealed
                      ? `0 0 22px ${roleStyle.accent}2B`
                      : undefined,
                  }}
                >
                  <div className="flex items-center justify-between gap-2">
                    <div className="flex min-w-0 items-center gap-3">
                      <button
                        type="button"
                        onClick={(e) => {
                          e.stopPropagation();
                          setAvatarDeckPlayerId(player.id);
                        }}
                        aria-label={`Change avatar for ${player.name}`}
                        className="shrink-0"
                      >
                        <CharacterAvatarThumbnail
                          character={char}
                          pose={player.avatarPose}
                          size={44}
                          borderColor={
                            revealed ? roleStyle.accent : char.accentColor
                          }
                          showBadgeDot
                        />
                      </button>
                      <div className="min-w-0">
                        <div className="truncate font-display text-base font-extrabold text-[#F7F2EC]">
                          {player.name}
                        </div>
                        <div className="font-mono-label text-[10px] font-bold uppercase text-[#887B9B]">
                          {revealed ? 'Tap to hide' : 'Tap to reveal'}
                        </div>
                      </div>
                    </div>

                    {state.players.length > 2 && (
                      <button
                        type="button"
                        onClick={(e) => {
                          e.stopPropagation();
                          onRemovePlayer(player.id);
                        }}
                        aria-label={`Remove ${player.name}`}
                        className="rounded-xl p-1.5 text-[#887B9B] hover:bg-[#341624] hover:text-[#FF3E5B]"
                      >
                        <Trash2 className="h-4 w-4" />
                      </button>
                    )}
                  </div>

                  {revealed && (
                    <div className="mt-3 flex flex-wrap items-center justify-between gap-2 rounded-2xl border border-[#423554]/70 bg-[#16101E]/90 px-3 py-2.5">
                      <RoleEmblemChip identity={identity} useShortLabel />
                      <span className="font-display text-sm font-black text-[#F7F2EC]">
                        {isImpostor
                          ? 'Hidden Word'
                          : state.locationWord?.word ?? '—'}
                      </span>
                    </div>
                  )}
                </div>
              );
            })}
          </div>

          {/* Add Player Card */}
          {state.players.length < 16 && (
            <button
              type="button"
              onClick={() => setAddPlayerOpen(true)}
              className="flex h-12 w-full items-center justify-center gap-2 rounded-2xl border border-dashed border-[#F4CD6C]/50 bg-[#241C2F]/60 text-sm font-bold text-[#F4CD6C] hover:bg-[#3A2F17]/35"
            >
              <Plus className="h-4 w-4" />
              <span>Add Player</span>
            </button>
          )}

          {/* Category Word Generator Card */}
          <div className="rounded-3xl border border-[#423554] bg-[#241C2F] p-5">
            <div className="flex flex-wrap items-center justify-between gap-2">
              <div className="flex items-center gap-2">
                <Layers className="h-4 w-4 text-[#F4CD6C]" />
                <span className="font-mono-label text-[10px] font-bold uppercase tracking-[0.2em] text-[#F4CD6C]">
                  CATEGORY WORD GENERATOR
                </span>
              </div>
              <div className="flex items-center gap-2">
                <button
                  type="button"
                  onClick={() => setPeekWordOpen((v) => !v)}
                  className="inline-flex items-center gap-1 rounded-xl border border-[#423554] bg-[#16101E] px-2.5 py-1 text-xs font-bold text-[#B7ABC8] hover:text-[#F7F2EC]"
                >
                  {peekWordOpen ? (
                    <EyeOff className="h-3.5 w-3.5" />
                  ) : (
                    <Eye className="h-3.5 w-3.5" />
                  )}
                  <span>{peekWordOpen ? 'Hide' : 'Peek'}</span>
                </button>
                <button
                  type="button"
                  onClick={onSelectNewLocationWord}
                  className="inline-flex items-center gap-1 rounded-xl border border-[#F4CD6C]/50 bg-[#3A2F17] px-2.5 py-1 text-xs font-extrabold text-[#FFDE8A]"
                >
                  <RefreshCw className="h-3.5 w-3.5" />
                  <span>New Word</span>
                </button>
              </div>
            </div>

            {peekWordOpen && state.locationWord && (
              <div className="mt-3 rounded-2xl border border-[#F4CD6C]/40 bg-[#16101E] p-3.5">
                <div className="font-display text-xl font-black text-[#F4CD6C]">
                  {state.locationWord.word}
                </div>
                <div className="mt-0.5 text-xs text-[#B7ABC8]">
                  {state.locationWord.category} &bull;{' '}
                  {state.locationWord.genre}
                  {state.hintsEnabled && state.locationWord.hint
                    ? ` • Hint: ${state.locationWord.hint}`
                    : ''}
                </div>
              </div>
            )}

            <div className="mt-3 flex flex-wrap gap-1.5">
              {wordBankSnapshot.categories.map((cat) => {
                const selected = state.selectedCategory === cat;
                return (
                  <button
                    key={cat}
                    type="button"
                    onClick={() => onSetCategory(cat)}
                    className={`rounded-xl border px-3 py-1.5 text-xs font-bold transition ${
                      selected
                        ? 'border-[#F4CD6C] bg-[#F4CD6C] text-[#1D1626]'
                        : 'border-[#423554] bg-[#16101E] text-[#B7ABC8] hover:text-[#F7F2EC]'
                    }`}
                  >
                    {cat === CATEGORY_ALL ? 'All Mixed' : cat}
                  </button>
                );
              })}
            </div>

            <div className="mt-4 flex items-center justify-between border-t border-[#423554]/60 pt-3">
              <div>
                <div className="font-display text-sm font-bold text-[#F7F2EC]">
                  Agent Hints
                </div>
                <div className="text-[11px] text-[#887B9B]">
                  Include 1-word clue for non-impostor players
                </div>
              </div>
              <PlumTogglePill
                checked={state.hintsEnabled}
                onToggle={onSetHintsEnabled}
                ariaLabel="Toggle lobby agent hints"
              />
            </div>
          </div>

          <GoldCtaButton
            text="Start Discussion Phase"
            onClick={onStartDiscussion}
            icon={<Sparkles className="h-5 w-5" />}
          />
        </div>
      )}

      {/* PHASE 2: DISCUSSION */}
      {state.phase === GamePhase.DISCUSSION && (
        <div className="space-y-4">
          {(() => {
            const speaker = getActiveTurnPlayer(state);
            const speakerChar = speaker
              ? AvatarCatalog.findById(speaker.avatarId)
              : AvatarCatalog.defaultForIndex(0);
            const isExpired = state.turnStatus === TurnStatus.EXPIRED;
            const isPaused = state.turnStatus === TurnStatus.PAUSED;

            return (
              <div className="rounded-3xl border border-[#423554] bg-[#241C2F] p-5 text-center shadow-xl">
                <div className="flex items-center justify-between">
                  <span className="font-mono-label text-[10px] font-bold uppercase tracking-[0.2em] text-[#F4CD6C]">
                    TURN {state.currentTurnIndex + 1} /{' '}
                    {Math.max(1, state.turnOrderIds.length)}
                  </span>
                  <span
                    className={`rounded-full px-2.5 py-0.5 font-mono-label text-[10px] font-extrabold ${
                      isExpired
                        ? 'bg-[#341624] text-[#FF7A8F]'
                        : isPaused
                        ? 'bg-[#3A2F17] text-[#FFDE8A]'
                        : 'bg-[#122C24] text-[#5CFFB0]'
                    }`}
                  >
                    {TURN_STATUS_LABELS[state.turnStatus]}
                  </span>
                </div>

                {speaker && (
                  <div className="mt-4 flex flex-col items-center">
                    <CharacterAvatarThumbnail
                      character={speakerChar}
                      pose={speaker.avatarPose}
                      size={64}
                      borderColor={speakerChar.accentColor}
                    />
                    <h3 className="mt-2 font-display text-2xl font-black text-[#F7F2EC]">
                      {speaker.name}&apos;s Turn
                    </h3>
                  </div>
                )}

                <div className="mt-4 flex justify-center">
                  <CircularCountdownTimer
                    remainingSeconds={state.currentTimerCount}
                    totalSeconds={state.turnDurationSeconds}
                    isPaused={isPaused}
                    isActive={state.turnStatus === TurnStatus.ACTIVE}
                    size={156}
                  />
                </div>

                <TurnTimerBar
                  remainingSeconds={state.currentTimerCount}
                  totalSeconds={state.turnDurationSeconds}
                  turnStatus={state.turnStatus}
                  className="mt-4"
                />

                {isExpired && (
                  <div className="mt-3 flex items-center justify-center gap-2 rounded-2xl border border-[#FF3E5B]/60 bg-[#341624] px-3 py-2 text-xs font-extrabold text-[#FF7A8F]">
                    <AlertTriangle className="h-4 w-4" />
                    <span>Turn expired — advancing automatically...</span>
                  </div>
                )}

                <div className="mt-5 flex flex-wrap items-center justify-center gap-2.5">
                  <button
                    type="button"
                    onClick={onToggleTimerPause}
                    className="inline-flex items-center gap-1.5 rounded-2xl border border-[#423554] bg-[#16101E] px-4 py-2.5 text-xs font-extrabold text-[#F7F2EC]"
                  >
                    {isPaused ? (
                      <>
                        <Play className="h-3.5 w-3.5 text-[#1AE682]" />
                        <span>Resume</span>
                      </>
                    ) : (
                      <>
                        <Pause className="h-3.5 w-3.5 text-[#F4CD6C]" />
                        <span>Pause</span>
                      </>
                    )}
                  </button>
                  <button
                    type="button"
                    onClick={onNextTurn}
                    className="inline-flex items-center gap-1.5 rounded-2xl border border-[#F4CD6C]/60 bg-[#3A2F17] px-4 py-2.5 text-xs font-extrabold text-[#FFDE8A]"
                  >
                    <SkipForward className="h-3.5 w-3.5" />
                    <span>Next Turn</span>
                  </button>
                  <button
                    type="button"
                    onClick={() => setEmergencyPickerOpen(true)}
                    className="inline-flex items-center gap-1.5 rounded-2xl border border-[#FF3E5B]/60 bg-[#341624] px-4 py-2.5 text-xs font-extrabold text-[#FF7A8F]"
                  >
                    <Siren className="h-3.5 w-3.5" />
                    <span>Emergency</span>
                  </button>
                </div>
              </div>
            );
          })()}

          {/* Private Word Reference Chips */}
          <div className="rounded-3xl border border-[#423554] bg-[#241C2F] p-4">
            <div className="font-mono-label text-[10px] font-bold uppercase tracking-[0.2em] text-[#B7ABC8]">
              PRIVATE WORD REFERENCE (TAP YOUR NAME)
            </div>
            <div className="mt-3 flex flex-wrap gap-2">
              {state.players.map((p) => (
                <button
                  key={p.id}
                  type="button"
                  onClick={() => setReferencePlayerId(p.id)}
                  className="inline-flex items-center gap-2 rounded-2xl border border-[#423554] bg-[#16101E] px-3 py-2 text-xs font-bold text-[#F7F2EC] hover:border-[#F4CD6C]/60"
                >
                  <CharacterAvatarThumbnail
                    character={AvatarCatalog.findById(p.avatarId)}
                    pose={p.avatarPose}
                    size={22}
                  />
                  <span>{p.name}</span>
                </button>
              ))}
            </div>
          </div>

          <div className="grid grid-cols-1 gap-3 sm:grid-cols-2">
            <button
              type="button"
              onClick={onReturnToLobby}
              className="inline-flex h-[54px] items-center justify-center gap-2 rounded-2xl border border-[#423554] bg-[#241C2F] font-display text-sm font-extrabold text-[#B7ABC8] hover:text-[#F7F2EC]"
            >
              <Users className="h-4 w-4" />
              <span>Lobby</span>
            </button>
            <GoldCtaButton
              text="Voting Phase"
              onClick={onStartVoting}
              icon={<Vote className="h-5 w-5" />}
            />
          </div>
        </div>
      )}

      {/* PHASE 3: VOTING */}
      {state.phase === GamePhase.VOTING && (
        <VotingScreen
          gameState={state}
          onCastVote={onCastVote}
          onRevealVerdict={onFinalizeVoting}
          onBackToLobby={onReturnToLobby}
        />
      )}

      {/* PHASE 4: RESULTS */}
      {state.phase === GamePhase.RESULTS && (
        <RoundOutcomeScreen
          gameState={state}
          onPlayAgain={onPlayNextRound}
          onBackToLobby={onReturnToLobby}
        />
      )}

      {/* Dialogs & Sheets */}
      <AddOrRenamePlayerDialog
        isOpen={addPlayerOpen}
        title="Add Player to Lobby"
        confirmLabel="Add Player"
        placeholder={`Player ${state.players.length + 1}`}
        onConfirm={(name) => {
          onAddPlayer(name);
          setAddPlayerOpen(false);
        }}
        onClose={() => setAddPlayerOpen(false)}
      />

      <AvatarPickerSheet
        isOpen={avatarDeckPlayerId !== null}
        players={state.players}
        initialPlayerId={avatarDeckPlayerId}
        onSelectAvatar={onSetPlayerAvatar}
        onClose={() => setAvatarDeckPlayerId(null)}
      />

      {/* Private Word Reference Modal */}
      {referencePlayerId && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/80 p-4 backdrop-blur-sm">
          {(() => {
            const p = state.players.find((pl) => pl.id === referencePlayerId);
            if (!p) return null;
            const role = state.playerRoles[p.id] ?? PlayerRole.NORMAL;
            const isImp = role === PlayerRole.IMPOSTOR;
            const word = getReferenceWordForPlayer(state, p.id);
            const hint = getReferenceHintForPlayer(state, p.id);
            return (
              <div className="w-full max-w-sm rounded-3xl border-2 border-[#F4CD6C]/60 bg-[#241C2F] p-6 text-center shadow-2xl">
                <div className="flex items-center justify-between">
                  <span className="font-mono-label text-[10px] font-bold uppercase tracking-[0.2em] text-[#F4CD6C]">
                    PRIVATE BRIEFING
                  </span>
                  <button
                    type="button"
                    onClick={() => setReferencePlayerId(null)}
                    aria-label="Close reference"
                    className="rounded-xl p-1 text-[#887B9B] hover:text-[#F7F2EC]"
                  >
                    <X className="h-5 w-5" />
                  </button>
                </div>
                <h3 className="mt-2 font-display text-2xl font-black text-[#F7F2EC]">
                  {p.name}
                </h3>
                <div className="mt-3 flex justify-center">
                  <RoleEmblemChip
                    identity={
                      isImp
                        ? GameRoleIdentity.IMPOSTOR
                        : GameRoleIdentity.AGENT
                    }
                  />
                </div>
                <div className="mt-4 rounded-2xl border border-[#423554] bg-[#16101E] p-4">
                  {isImp ? (
                    <p className="text-sm font-semibold text-[#FF7A8F]">
                      You are the Impostor — blend in with the agents!
                    </p>
                  ) : (
                    <>
                      <div className="font-display text-2xl font-black text-[#F4CD6C]">
                        {word ?? '—'}
                      </div>
                      {hint && (
                        <div className="mt-1 text-xs text-[#B7ABC8]">
                          Hint: {hint}
                        </div>
                      )}
                    </>
                  )}
                </div>
                <div className="mt-5">
                  <GoldCtaButton
                    text="Hide Briefing"
                    onClick={() => setReferencePlayerId(null)}
                  />
                </div>
              </div>
            );
          })()}
        </div>
      )}

      {/* Emergency Meeting Reporter Picker Modal */}
      {emergencyPickerOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/80 p-4 backdrop-blur-sm">
          <div className="w-full max-w-md rounded-3xl border border-[#FF3E5B]/60 bg-[#241C2F] p-6 shadow-2xl">
            <div className="flex items-center justify-between">
              <div className="flex items-center gap-2 text-[#FF7A8F]">
                <Siren className="h-5 w-5" />
                <h3 className="font-display text-lg font-extrabold text-[#F7F2EC]">
                  Who Called the Meeting?
                </h3>
              </div>
              <button
                type="button"
                onClick={() => setEmergencyPickerOpen(false)}
                aria-label="Cancel emergency meeting"
                className="rounded-xl p-1 text-[#887B9B] hover:text-[#F7F2EC]"
              >
                <X className="h-5 w-5" />
              </button>
            </div>
            <div className="mt-4 grid grid-cols-2 gap-2.5">
              {state.players.map((p) => (
                <button
                  key={p.id}
                  type="button"
                  onClick={() => {
                    setEmergencyPickerOpen(false);
                    onTriggerEmergencyMeeting(
                      p.id,
                      `${p.name} requested an immediate vote!`
                    );
                  }}
                  className="flex items-center gap-2.5 rounded-2xl border border-[#423554] bg-[#16101E] px-3 py-2.5 text-left text-xs font-extrabold text-[#F7F2EC] hover:border-[#FF3E5B]"
                >
                  <CharacterAvatarThumbnail
                    character={AvatarCatalog.findById(p.avatarId)}
                    pose={p.avatarPose}
                    size={28}
                  />
                  <span className="truncate">{p.name}</span>
                </button>
              ))}
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
