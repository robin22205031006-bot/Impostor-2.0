import React, { useEffect, useMemo, useState } from 'react';
import {
  AlertTriangle,
  Award,
  Check,
  ChevronDown,
  ChevronUp,
  Crown,
  Edit3,
  Eye,
  Lock,
  Pause,
  Play,
  Plus,
  RotateCcw,
  Shield,
  Siren,
  SkipForward,
  Sparkles,
  Vote,
  X,
} from 'lucide-react';
import { CharacterAvatarThumbnail } from '../components/AvatarCardGraphics';
import {
  FloatingHeroSprite,
  GoldCtaButton,
} from '../components/ImpostorWizardComponents';
import { RoleEmblemBadge, RoleEmblemChip } from '../components/RoleEmblem';
import { RoleWordCard } from '../components/RoleWordCard';
import {
  AgentDirectorBadge,
  OtherImpostorAllyNote,
  SaviorAbilityCard,
  UltimateKillerAbilityCard,
} from '../components/RoundAbilitiesUi';
import { CircularCountdownTimer } from '../components/TurnTimerBar';
import { AvatarCatalog } from '../data/avatarCatalog';
import { TurnTimerEventBus } from '../data/turnTimerEventBus';
import { getRoleForPlayer, getWordBriefingForPlayer } from '../hooks/useLocalGame';
import {
  GAME_MODE_META,
  getRoleVisualStyle,
  PALETTE,
  resolveRoleIdentity,
} from '../theme/palette';
import {
  GameRoleIdentity,
  LocalGameUiState,
  LocalPlayer,
  PlayerRole,
  SetupWizardStep,
  TurnExpiredBroadcast,
} from '../types/game';

/* ─────────────────────────────────────────────────────────────
 * 1) LOCAL PLAYING CONTENT (Active Arena)
 * ───────────────────────────────────────────────────────────── */

interface LocalPlayingContentProps {
  state: LocalGameUiState;
  onToggleTimerPause: () => void;
  onAddExtraSeconds: (seconds: number) => void;
  onNextTurn: () => void;
  onGoToVoting: () => void;
  onTriggerEmergencyMeeting: (reporterId?: string, reason?: string) => void;
  onUpdateImpostorWordGuess: (guess: string) => void;
  onSelectKillerTarget: (targetId: string) => void;
  onSelectSaviorProtected: (targetId: string) => void;
}

export const LocalPlayingContent: React.FC<LocalPlayingContentProps> = ({
  state,
  onToggleTimerPause,
  onAddExtraSeconds,
  onNextTurn,
  onGoToVoting,
  onTriggerEmergencyMeeting,
  onUpdateImpostorWordGuess,
  onSelectKillerTarget,
  onSelectSaviorProtected,
}) => {
  const [peekPlayerId, setPeekPlayerId] = useState<string | null>(null);
  const [guessOpen, setGuessOpen] = useState(false);
  const [expiredBanner, setExpiredBanner] = useState<TurnExpiredBroadcast | null>(null);

  useEffect(() => {
    return TurnTimerEventBus.subscribe((evt) => {
      setExpiredBanner(evt);
    });
  }, []);

  useEffect(() => {
    if (state.remainingTurnSeconds > 0) {
      setExpiredBanner(null);
    }
  }, [state.currentTurnIndex, state.remainingTurnSeconds]);

  const activePlayerId = state.turnOrderIds[state.currentTurnIndex];
  const nextPlayerId = state.turnOrderIds[state.currentTurnIndex + 1];
  const activePlayer =
    state.players.find((p) => p.id === activePlayerId) ?? state.players[0];
  const nextPlayer = state.players.find((p) => p.id === nextPlayerId);

  const activeChar = activePlayer
    ? AvatarCatalog.findById(activePlayer.avatarId)
    : AvatarCatalog.defaultForIndex(0);
  const nextChar = nextPlayer ? AvatarCatalog.findById(nextPlayer.avatarId) : null;

  const isLastSpeaker = state.currentTurnIndex >= state.turnOrderIds.length - 1;
  const peekPlayer = state.players.find((p) => p.id === peekPlayerId) ?? null;
  const peekBriefing = peekPlayer
    ? getWordBriefingForPlayer(state, peekPlayer.id)
    : null;

  return (
    <div className="mx-auto flex w-full max-w-xl flex-col gap-5 px-5 pb-28 pt-2">
      {/* Speaker Stage + Timer */}
      <div className="rounded-3xl border border-[#423554] bg-[#241C2F] p-5 text-center shadow-xl">
        <div className="font-mono-label text-[10px] font-bold uppercase tracking-[0.22em] text-[#F4CD6C]">
          SPEAKER {state.currentTurnIndex + 1} OF{' '}
          {Math.max(1, state.turnOrderIds.length)}
        </div>

        <div className="mt-4 flex items-center justify-center gap-3 sm:gap-5">
          {/* Active Speaker */}
          {activePlayer && (
            <div className="flex flex-col items-center">
              <FloatingHeroSprite
                src={activeChar.baseImage}
                alt={activePlayer.name}
                accentColor={activeChar.accentColor}
                sizeClass="h-24 w-20 sm:h-28 sm:w-24"
              />
              <span className="mt-1 rounded-full bg-[#3A2F17] px-2.5 py-0.5 text-[11px] font-extrabold text-[#FFDE8A]">
                {activePlayer.name}
              </span>
            </div>
          )}

          {/* Circular Timer or FREE TALK */}
          {state.timerEnabled ? (
            <CircularCountdownTimer
              remainingSeconds={state.remainingTurnSeconds}
              totalSeconds={state.turnDurationSeconds}
              isPaused={state.isTimerPaused}
              isActive={!state.isTimerPaused && state.remainingTurnSeconds > 0}
              size={148}
              onTimeExpired={() => {
                TurnTimerEventBus.emitExpired(
                  activePlayer?.name ?? 'Speaker',
                  state.currentTurnIndex,
                  state.turnOrderIds.length
                );
              }}
            />
          ) : (
            <div className="flex h-36 w-36 flex-col items-center justify-center rounded-full border-2 border-dashed border-[#F4CD6C]/50 bg-[#16101E] p-3">
              <Sparkles className="h-6 w-6 text-[#F4CD6C]" />
              <div className="mt-1 font-display text-base font-black text-[#F7F2EC]">
                FREE TALK
              </div>
              <div className="font-mono-label text-[9px] font-bold uppercase text-[#887B9B]">
                UNTIMED
              </div>
            </div>
          )}

          {/* Next Speaker Preview */}
          {nextPlayer && nextChar && (
            <div className="flex flex-col items-center opacity-70">
              <FloatingHeroSprite
                src={nextChar.baseImage}
                alt={nextPlayer.name}
                accentColor={nextChar.accentColor}
                sizeClass="h-20 w-16 sm:h-24 sm:w-20"
                delayMs={200}
              />
              <span className="mt-1 max-w-[76px] truncate text-[10px] font-bold text-[#B7ABC8]">
                Next: {nextPlayer.name}
              </span>
            </div>
          )}
        </div>

        {expiredBanner && (
          <div className="mt-4 flex items-center justify-center gap-2 rounded-2xl border border-[#FF3E5B]/60 bg-[#341624] px-3.5 py-2 text-xs font-extrabold text-[#FF7A8F]">
            <AlertTriangle className="h-4 w-4 shrink-0" />
            <span>
              Time&apos;s up for {expiredBanner.playerName}! Pass the mic.
            </span>
          </div>
        )}

        {/* Controls */}
        <div className="mt-5 flex flex-wrap items-center justify-center gap-2.5">
          {state.timerEnabled && (
            <>
              <button
                type="button"
                onClick={onToggleTimerPause}
                className="inline-flex items-center gap-1.5 rounded-2xl border border-[#423554] bg-[#16101E] px-4 py-2.5 text-xs font-extrabold text-[#F7F2EC] hover:border-[#F4CD6C]/60"
              >
                {state.isTimerPaused ? (
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
                onClick={() => onAddExtraSeconds(10)}
                className="inline-flex items-center gap-1 rounded-2xl border border-[#423554] bg-[#16101E] px-3.5 py-2.5 text-xs font-extrabold text-[#F4CD6C] hover:border-[#F4CD6C]/60"
              >
                <Plus className="h-3.5 w-3.5" />
                <span>10s</span>
              </button>
            </>
          )}
          <button
            type="button"
            onClick={onNextTurn}
            className="inline-flex items-center gap-1.5 rounded-2xl border border-[#F4CD6C]/60 bg-[#3A2F17] px-4 py-2.5 text-xs font-extrabold text-[#FFDE8A] hover:bg-[#4B3D1E]"
          >
            <SkipForward className="h-3.5 w-3.5" />
            <span>{isLastSpeaker ? 'Finish Turns' : 'Next Turn'}</span>
          </button>
        </div>
      </div>

      {/* Table Briefing Summary */}
      <div className="flex flex-wrap items-center justify-between gap-2 rounded-2xl border border-[#423554] bg-[#241C2F] px-4 py-3">
        <div>
          <div className="font-mono-label text-[10px] font-bold uppercase tracking-wider text-[#887B9B]">
            TABLE BRIEFING
          </div>
          <div className="text-xs font-bold text-[#F7F2EC]">
            {GAME_MODE_META[state.gameMode].title} &bull;{' '}
            {state.categoryCluesEnabled && state.activeWord
              ? `${state.activeWord.category} (${state.activeWord.genre})`
              : 'Blind Category'}
          </div>
        </div>
        <span className="rounded-xl border border-[#FF3E5B]/45 bg-[#341624] px-2.5 py-1 font-mono-label text-[10px] font-extrabold text-[#FF7A8F]">
          {state.impostorCount} IMPOSTOR{state.impostorCount > 1 ? 'S' : ''}
        </span>
      </div>

      {/* Private Card Peek Row */}
      <div className="rounded-3xl border border-[#423554] bg-[#241C2F] p-4">
        <div className="font-mono-label text-[10px] font-bold uppercase tracking-[0.18em] text-[#B7ABC8]">
          FORGOT YOUR CARD? TAP YOUR AVATAR PRIVATELY
        </div>
        <div className="mt-3 flex gap-2.5 overflow-x-auto pb-1">
          {state.players.map((player) => {
            const char = AvatarCatalog.findById(player.avatarId);
            return (
              <button
                key={player.id}
                type="button"
                onClick={() => setPeekPlayerId(player.id)}
                className="flex shrink-0 flex-col items-center gap-1.5 rounded-2xl border border-[#423554] bg-[#16101E] px-3 py-2.5 transition hover:border-[#F4CD6C]/60"
              >
                <CharacterAvatarThumbnail
                  character={char}
                  pose={player.avatarPose}
                  size={42}
                />
                <span className="max-w-[72px] truncate text-[11px] font-bold text-[#F7F2EC]">
                  {player.name}
                </span>
              </button>
            );
          })}
        </div>
      </div>

      {/* Collapsible Impostor Word Guess */}
      <div className="rounded-3xl border border-[#FF3E5B]/40 bg-[#241C2F] p-4">
        <button
          type="button"
          onClick={() => setGuessOpen((o) => !o)}
          className="flex w-full items-center justify-between gap-2 text-left"
        >
          <div>
            <div className="font-mono-label text-[10px] font-bold uppercase tracking-[0.18em] text-[#FF7A8F]">
              IMPOSTOR WORD GUESS (STEAL ROUND)
            </div>
            <div className="text-xs font-medium text-[#B7ABC8]">
              {state.impostorWordGuess
                ? `Current guess: "${state.impostorWordGuess}"`
                : 'Think you figured out the secret word?'}
            </div>
          </div>
          {guessOpen ? (
            <ChevronUp className="h-5 w-5 text-[#B7ABC8]" />
          ) : (
            <ChevronDown className="h-5 w-5 text-[#B7ABC8]" />
          )}
        </button>

        {guessOpen && (
          <div className="mt-3">
            <input
              type="text"
              value={state.impostorWordGuess}
              onChange={(e) => onUpdateImpostorWordGuess(e.target.value)}
              placeholder="Type exact secret word guess..."
              className="w-full rounded-2xl border border-[#423554] bg-[#16101E] px-3.5 py-2.5 text-sm font-semibold text-[#F7F2EC] placeholder-[#887B9B] focus:border-[#FF3E5B] focus:outline-none"
            />
          </div>
        )}
      </div>

      {/* Emergency Meeting Button */}
      <button
        type="button"
        onClick={() =>
          onTriggerEmergencyMeeting(
            activePlayer?.id,
            'Emergency discussion called at the table!'
          )
        }
        className="inline-flex h-12 w-full items-center justify-center gap-2 rounded-2xl border border-[#FF3E5B]/60 bg-[#341624] font-display text-sm font-extrabold text-[#FF7A8F] transition hover:bg-[#491D31]"
      >
        <Siren className="h-4 w-4" />
        <span>Call Emergency Meeting</span>
      </button>

      {/* Sticky Bottom Voting CTA */}
      <div className="fixed inset-x-0 bottom-0 z-30 border-t border-[#423554]/80 bg-[#1D1626]/95 px-5 py-4 backdrop-blur-md">
        <div className="mx-auto max-w-xl">
          <GoldCtaButton
            text="We're ready to vote"
            onClick={onGoToVoting}
            icon={<Vote className="h-5 w-5" />}
          />
        </div>
      </div>

      {/* Private Card Peek Modal */}
      {peekPlayer && peekBriefing && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/80 p-4 backdrop-blur-sm">
          <div className="max-h-[90vh] w-full max-w-md space-y-3 overflow-y-auto rounded-3xl border border-[#423554] bg-[#1D1626] p-5 shadow-2xl">
            <div className="flex items-center justify-between">
              <span className="font-mono-label text-[10px] font-bold uppercase tracking-[0.2em] text-[#F4CD6C]">
                PRIVATE CARD PEEK
              </span>
              <button
                type="button"
                onClick={() => setPeekPlayerId(null)}
                aria-label="Close private card peek"
                className="rounded-xl p-1.5 text-[#887B9B] hover:bg-[#241C2F] hover:text-[#F7F2EC]"
              >
                <X className="h-5 w-5" />
              </button>
            </div>

            <RoleWordCard
              playerName={peekPlayer.name}
              avatarId={peekPlayer.avatarId}
              avatarPose={peekPlayer.avatarPose}
              briefing={peekBriefing}
              showHint={state.hintsEnabled}
              isUltimateKiller={peekPlayer.id === state.ultimateKillerId}
              isSavior={peekPlayer.id === state.saviorAgentId}
              isDirector={peekPlayer.id === state.agentDirectorId}
            />

            {peekBriefing.role === PlayerRole.IMPOSTOR && (
              <OtherImpostorAllyNote
                allyNames={state.players
                  .filter(
                    (p) =>
                      p.id !== peekPlayer.id && state.impostorIds.has(p.id)
                  )
                  .map((p) => p.name)}
              />
            )}

            {peekPlayer.id === state.ultimateKillerId && (
              <UltimateKillerAbilityCard
                killerId={peekPlayer.id}
                players={state.players}
                selectedTargetId={state.nullifiedVoterId}
                onSelectTarget={onSelectKillerTarget}
              />
            )}

            {peekPlayer.id === state.saviorAgentId && (
              <SaviorAbilityCard
                saviorId={peekPlayer.id}
                players={state.players}
                protectedPlayerId={state.protectedPlayerId}
                onSelectProtected={onSelectSaviorProtected}
              />
            )}

            {peekPlayer.id === state.agentDirectorId && <AgentDirectorBadge />}

            <GoldCtaButton
              text="Hide & Return to Table"
              onClick={() => setPeekPlayerId(null)}
            />
          </div>
        </div>
      )}
    </div>
  );
};

/* ─────────────────────────────────────────────────────────────
 * 2) LOCAL VOTING CONTENT (Pass-phone Secret Ballot)
 * ───────────────────────────────────────────────────────────── */

interface LocalVotingContentProps {
  state: LocalGameUiState;
  onRevealVoterBallot: () => void;
  onCastLocalVote: (voterId: string, suspectId: string) => void;
  onConfirmVoteAndAdvance: () => void;
  onUpdateImpostorWordGuess: (guess: string) => void;
  onSubmitResults: () => void;
}

export const LocalVotingContent: React.FC<LocalVotingContentProps> = ({
  state,
  onRevealVoterBallot,
  onCastLocalVote,
  onConfirmVoteAndAdvance,
  onUpdateImpostorWordGuess,
  onSubmitResults,
}) => {
  const allVoted = state.votingPlayerIndex >= state.players.length;
  const activeVoter = !allVoted ? state.players[state.votingPlayerIndex] : null;
  const currentChoice = activeVoter
    ? state.votesByPlayerId[activeVoter.id] ?? null
    : null;
  const isFinalVoter = state.votingPlayerIndex === state.players.length - 1;

  return (
    <div className="mx-auto flex w-full max-w-xl flex-col gap-5 px-5 pb-12 pt-2">
      {/* 16:9 Voting Table Hero Banner */}
      <div className="relative aspect-video w-full overflow-hidden rounded-3xl border border-[#423554] bg-[#241C2F] shadow-xl">
        <img
          src="/assets/images/img_voting_table_banner.jpg"
          alt="Secret Tribunal Voting Table"
          loading="eager"
          decoding="sync"
          className="h-full w-full object-cover"
        />
        <div className="absolute inset-0 bg-gradient-to-t from-[#16101E] via-[#16101E]/40 to-transparent" />
        <div className="absolute bottom-4 left-4 right-4 flex items-end justify-between gap-2">
          <div>
            <div className="font-mono-label text-[10px] font-bold uppercase tracking-[0.22em] text-[#F4CD6C]">
              SECRET TRIBUNAL
            </div>
            <h2 className="font-display text-2xl font-black text-[#F7F2EC]">
              Point the Finger
            </h2>
          </div>
          <span className="rounded-xl border border-[#423554] bg-[#16101E]/90 px-3 py-1 font-mono-label text-xs font-extrabold text-[#F4CD6C]">
            {Math.min(state.votingPlayerIndex, state.players.length)} /{' '}
            {state.players.length}
          </span>
        </div>
      </div>

      {!allVoted && activeVoter ? (
        !state.isVoteCardRevealed ? (
          /* Pass-Phone Prompt */
          <div className="rounded-3xl border-2 border-[#F4CD6C]/60 bg-[#241C2F] p-6 text-center shadow-xl">
            <CharacterAvatarThumbnail
              character={AvatarCatalog.findById(activeVoter.avatarId)}
              pose={activeVoter.avatarPose}
              size={72}
              borderColor={PALETTE.goldAccent}
              className="mx-auto"
            />
            <div className="mt-4 font-mono-label text-[10px] font-bold uppercase tracking-[0.22em] text-[#F4CD6C]">
              PASS PHONE TO
            </div>
            <h3 className="mt-1 font-display text-3xl font-black text-[#F7F2EC]">
              {activeVoter.name}
            </h3>
            <p className="mt-2 text-xs font-medium text-[#B7ABC8]">
              Keep the screen private before opening your secret ballot.
            </p>
            <div className="mt-5">
              <GoldCtaButton
                text={`Open ${activeVoter.name}'s Ballot`}
                onClick={onRevealVoterBallot}
                icon={<Eye className="h-5 w-5" />}
              />
            </div>
          </div>
        ) : (
          /* 2-Column Suspect Grid */
          <div className="space-y-4">
            <div className="flex items-center justify-between">
              <div>
                <div className="font-mono-label text-[10px] font-bold uppercase tracking-[0.2em] text-[#F4CD6C]">
                  VOTING AS {activeVoter.name.toUpperCase()}
                </div>
                <p className="text-xs text-[#B7ABC8]">
                  Tap the player you suspect is an impostor.
                </p>
              </div>
              {activeVoter.id === state.agentDirectorId && (
                <span className="inline-flex items-center gap-1 rounded-full bg-[#38290E] px-2.5 py-1 font-mono-label text-[10px] font-extrabold text-[#FFDA66]">
                  <Crown className="h-3 w-3" />
                  <span>2x VOTE</span>
                </span>
              )}
            </div>

            <div className="grid grid-cols-2 gap-3">
              {state.players.map((suspect) => {
                const isSelf = suspect.id === activeVoter.id;
                const isPicked = currentChoice === suspect.id;
                const char = AvatarCatalog.findById(suspect.avatarId);

                return (
                  <button
                    key={suspect.id}
                    type="button"
                    disabled={isSelf}
                    onClick={() => onCastLocalVote(activeVoter.id, suspect.id)}
                    className={`flex flex-col items-center gap-2 rounded-3xl border p-4 text-center transition ${
                      isSelf
                        ? 'cursor-not-allowed border-[#423554]/40 bg-[#16101E]/60 opacity-40'
                        : isPicked
                        ? 'border-[#FF3E5B] bg-[#341624] shadow-[0_0_20px_rgba(255,62,91,0.28)]'
                        : 'border-[#423554] bg-[#241C2F] hover:border-[#F4CD6C]/60'
                    }`}
                  >
                    <CharacterAvatarThumbnail
                      character={char}
                      pose={suspect.avatarPose}
                      size={54}
                      borderColor={isPicked ? '#FF3E5B' : char.accentColor}
                    />
                    <div className="w-full truncate font-display text-sm font-extrabold text-[#F7F2EC]">
                      {suspect.name}
                    </div>
                    <div className="font-mono-label text-[10px] font-bold uppercase text-[#887B9B]">
                      {isSelf
                        ? 'Yourself'
                        : isPicked
                        ? 'SUSPECT LOCKED'
                        : 'Tap to vote'}
                    </div>
                    {isPicked && (
                      <span className="flex h-6 w-6 items-center justify-center rounded-full bg-[#FF3E5B] text-white">
                        <Check className="h-3.5 w-3.5" strokeWidth={3} />
                      </span>
                    )}
                  </button>
                );
              })}
            </div>

            <GoldCtaButton
              text={
                isFinalVoter
                  ? 'Lock Final Vote'
                  : 'Lock Vote & Pass Phone'
              }
              disabled={!currentChoice}
              onClick={onConfirmVoteAndAdvance}
              icon={<Lock className="h-4 w-4" />}
            />
          </div>
        )
      ) : (
        /* All Ballots Cast -> Impostor Last-Chance Guess + Reveal */
        <div className="space-y-4">
          <div className="rounded-3xl border border-[#1AE682]/45 bg-[#122C24]/70 p-4 text-center">
            <div className="font-mono-label text-[10px] font-extrabold uppercase tracking-[0.22em] text-[#5CFFB0]">
              ALL BALLOTS SEALED
            </div>
            <p className="mt-1 text-sm font-bold text-[#F7F2EC]">
              Before revealing the votes, the impostor gets one final shot to
              steal the round!
            </p>
          </div>

          <div className="rounded-3xl border border-[#FF3E5B]/50 bg-[#241C2F] p-5">
            <div className="font-mono-label text-[10px] font-bold uppercase tracking-[0.2em] text-[#FF7A8F]">
              IMPOSTOR&apos;S LAST-CHANCE WORD GUESS
            </div>
            <p className="mt-1 text-xs text-[#B7ABC8]">
              Optional: If the impostor knows the exact secret word, enter it
              below to override defeat.
            </p>
            <input
              type="text"
              value={state.impostorWordGuess}
              onChange={(e) => onUpdateImpostorWordGuess(e.target.value)}
              placeholder="Enter exact secret word guess (or leave blank)..."
              className="mt-3 w-full rounded-2xl border border-[#423554] bg-[#16101E] px-4 py-3 text-sm font-semibold text-[#F7F2EC] placeholder-[#887B9B] focus:border-[#FF3E5B] focus:outline-none"
            />
          </div>

          <GoldCtaButton
            text="Reveal the imposter"
            onClick={onSubmitResults}
            icon={<Sparkles className="h-5 w-5" />}
          />
        </div>
      )}
    </div>
  );
};

/* ─────────────────────────────────────────────────────────────
 * 3) LOCAL RESULTS CONTENT (The Big Reveal + Scoreboard)
 * ───────────────────────────────────────────────────────────── */

interface LocalResultsContentProps {
  state: LocalGameUiState;
  onEditSetup: (step: SetupWizardStep) => void;
  onPlayAnotherRound: () => void;
}

export const LocalResultsContent: React.FC<LocalResultsContentProps> = ({
  state,
  onEditSetup,
  onPlayAnotherRound,
}) => {
  const impostors = state.players.filter((p) => state.impostorIds.has(p.id));
  const sortedPlayers = useMemo(
    () => [...state.players].sort((a, b) => b.score - a.score),
    [state.players]
  );

  const killerPlayer = state.players.find(
    (p) => p.id === state.ultimateKillerId
  );
  const rawKillerTarget = state.players.find(
    (p) => p.id === state.rawKillerTargetId
  );
  const saviorPlayer = state.players.find((p) => p.id === state.saviorAgentId);
  const protectedPlayer = state.players.find(
    (p) => p.id === state.protectedPlayerId
  );
  const directorPlayer = state.players.find(
    (p) => p.id === state.agentDirectorId
  );

  const getIdentity = (player: LocalPlayer): GameRoleIdentity =>
    resolveRoleIdentity(
      getRoleForPlayer(state, player.id),
      player.id === state.ultimateKillerId,
      player.id === state.saviorAgentId,
      player.id === state.agentDirectorId
    );

  return (
    <div className="mx-auto flex w-full max-w-xl flex-col gap-5 px-5 pb-12 pt-2">
      {/* Big Reveal Title */}
      <div
        className={`text-center ${
          state.crewWon
            ? 'animate-[verdictPop_520ms_cubic-bezier(0.22,1,0.36,1)]'
            : 'animate-[screenShake_650ms_cubic-bezier(0.36,0.07,0.19,0.97)]'
        }`}
      >
        <div className="font-mono-label text-[10px] font-bold uppercase tracking-[0.24em] text-[#F4CD6C]">
          THE BIG REVEAL
        </div>
        <h2
          className="mt-1 font-display text-4xl font-black tracking-tight"
          style={{
            color: state.crewWon ? PALETTE.normalGlow : PALETTE.impostorGlow,
          }}
        >
          {state.crewWon ? 'AGENTS WINS!' : 'IMPOSTOR SURVIVES!'}
        </h2>
        <p className="mt-1 text-xs font-semibold text-[#B7ABC8]">
          {state.impostorGuessCorrect
            ? 'The impostor nailed the exact secret word and stole the victory!'
            : state.crewWon
            ? 'Sharp deduction — the crew exposed the impostor!'
            : 'Misdirection worked — the impostors survived the vote!'}
        </p>
      </div>

      {/* 16:9 Outcome Artwork Card */}
      <div className="relative aspect-video w-full overflow-hidden rounded-3xl border-2 border-[#423554] bg-[#241C2F] shadow-2xl">
        <img
          src={
            state.crewWon
              ? '/assets/images/img_outcome_crew_win.jpg'
              : '/assets/images/img_outcome_impostor_win.jpg'
          }
          alt={state.crewWon ? 'Agents Victory' : 'Impostor Victory'}
          loading="eager"
          decoding="sync"
          className="h-full w-full object-cover"
        />
        <div className="absolute inset-0 bg-gradient-to-t from-[#16101E] via-[#16101E]/35 to-transparent" />
        <div className="absolute bottom-3.5 left-3.5 right-3.5 flex flex-wrap items-center justify-between gap-2">
          <div className="flex flex-wrap gap-1.5">
            {impostors.map((imp) => (
              <span
                key={imp.id}
                className="inline-flex items-center gap-1.5 rounded-full border border-[#FF3E5B]/75 bg-[#341624]/95 px-3 py-1 text-xs font-extrabold text-[#FF7A8F]"
              >
                <span>Impostor: {imp.name}</span>
              </span>
            ))}
          </div>
        </div>
      </div>

      {/* Tilted Secret Word Card */}
      <div className="-rotate-[1.4deg] rounded-3xl border-2 border-[#F4CD6C]/70 bg-gradient-to-br from-[#3A2F17] to-[#241C2F] p-5 text-center shadow-xl">
        <div className="font-mono-label text-[10px] font-bold uppercase tracking-[0.24em] text-[#FFDE8A]">
          THE SECRET WORD
        </div>
        <div className="mt-1 font-display text-3xl font-black text-[#F7F2EC]">
          {state.activeWord?.word ?? '—'}
        </div>
        {state.activeWord && (
          <div className="mt-1 text-xs font-semibold text-[#B7ABC8]">
            {state.activeWord.category} &bull; {state.activeWord.genre}
            {state.activeHint ? ` • Hint: ${state.activeHint}` : ''}
          </div>
        )}
      </div>

      {/* Special Role Outcome Summary Cards */}
      {(killerPlayer || saviorPlayer || directorPlayer) && (
        <div className="space-y-2.5">
          {killerPlayer && (
            <div className="flex items-center gap-3 rounded-2xl border border-[#FF2985]/45 bg-[#34142B]/80 p-3.5">
              <RoleEmblemBadge identity={GameRoleIdentity.AGENT_KILLER} size={38} />
              <div className="text-xs text-[#F7F2EC]">
                <span className="font-extrabold text-[#FF78B4]">
                  Agent Killer ({killerPlayer.name}):
                </span>{' '}
                {rawKillerTarget
                  ? state.saviorSavedTarget
                    ? `Targeted ${rawKillerTarget.name}, but Savior Agent blocked the sabotage!`
                    : `Nullified ${rawKillerTarget.name}'s vote.`
                  : 'Did not nullify any vote.'}
              </div>
            </div>
          )}

          {saviorPlayer && (
            <div className="flex items-center gap-3 rounded-2xl border border-[#2CCBFF]/45 bg-[#13293E]/80 p-3.5">
              <RoleEmblemBadge identity={GameRoleIdentity.SAVIOR_AGENT} size={38} />
              <div className="text-xs text-[#F7F2EC]">
                <span className="font-extrabold text-[#82E4FF]">
                  Savior Agent ({saviorPlayer.name}):
                </span>{' '}
                {protectedPlayer
                  ? state.saviorSavedTarget
                    ? `Protected ${protectedPlayer.name} and blocked sabotage (+1 bonus)!`
                    : `Shielded ${protectedPlayer.name}'s vote.`
                  : 'Did not deploy a shield.'}
              </div>
            </div>
          )}

          {directorPlayer && (
            <div className="flex items-center gap-3 rounded-2xl border border-[#FFB800]/45 bg-[#38290E]/80 p-3.5">
              <RoleEmblemBadge identity={GameRoleIdentity.AGENT_DIRECTOR} size={38} />
              <div className="text-xs text-[#F7F2EC]">
                <span className="font-extrabold text-[#FFDA66]">
                  Agent Director ({directorPlayer.name}):
                </span>{' '}
                Cast ballot with 2x voting weight.
              </div>
            </div>
          )}
        </div>
      )}

      {/* Scoreboard & Votes */}
      <div className="rounded-3xl border border-[#423554] bg-[#241C2F] p-5">
        <div className="flex items-center justify-between">
          <div className="font-mono-label text-[10px] font-bold uppercase tracking-[0.22em] text-[#F4CD6C]">
            SCOREBOARD &amp; VOTES
          </div>
          <Award className="h-4 w-4 text-[#F4CD6C]" />
        </div>

        <div className="mt-4 space-y-2.5">
          {sortedPlayers.map((player) => {
            const identity = getIdentity(player);
            const style = getRoleVisualStyle(identity);
            const char = AvatarCatalog.findById(player.avatarId);
            const votesReceived = state.roundVoteCounts[player.id] ?? 0;
            const delta = state.roundDeltas[player.id] ?? 0;
            const votedForId = state.votesByPlayerId[player.id];
            const votedForName = state.players.find(
              (p) => p.id === votedForId
            )?.name;
            const wasNullified = state.nullifiedVoterId === player.id;
            const wasShielded = state.protectedPlayerId === player.id;
            const predictedId = state.agentPredictions[player.id];
            const predictedCorrect =
              predictedId && state.impostorIds.has(predictedId);

            return (
              <div
                key={player.id}
                className="flex items-center gap-3 rounded-2xl border border-[#423554] bg-[#16101E] p-3.5"
              >
                <CharacterAvatarThumbnail
                  character={char}
                  pose={player.avatarPose}
                  size={44}
                  borderColor={style.accent}
                />

                <div className="min-w-0 flex-1">
                  <div className="flex flex-wrap items-center gap-1.5">
                    <span className="truncate font-display text-sm font-extrabold text-[#F7F2EC]">
                      {player.name}
                    </span>
                    <RoleEmblemChip identity={identity} useShortLabel />
                  </div>

                  <div className="mt-1 flex flex-wrap items-center gap-2 text-[11px] text-[#887B9B]">
                    <span>
                      {votesReceived} vote{votesReceived === 1 ? '' : 's'}
                    </span>
                    {votedForName && (
                      <span>
                        &bull; Voted {votedForName}
                        {wasNullified ? ' (VOIDED)' : ''}
                      </span>
                    )}
                    {wasShielded && (
                      <span className="inline-flex items-center gap-0.5 text-[#82E4FF]">
                        <Shield className="h-3 w-3" /> Shielded
                      </span>
                    )}
                    {predictedCorrect && (
                      <span className="text-[#FFDE8A]">• Early Read +1</span>
                    )}
                  </div>
                </div>

                <div className="text-right">
                  <div className="font-display text-lg font-black text-[#F7F2EC]">
                    {player.score}
                  </div>
                  <div
                    className={`font-mono-label text-[10px] font-extrabold ${
                      delta > 0 ? 'text-[#1AE682]' : 'text-[#887B9B]'
                    }`}
                  >
                    {delta > 0 ? `+${delta} pts` : '+0'}
                  </div>
                </div>
              </div>
            );
          })}
        </div>
      </div>

      {/* Footer Actions */}
      <div className="grid grid-cols-1 gap-3 sm:grid-cols-2">
        <button
          type="button"
          onClick={() => onEditSetup(SetupWizardStep.RULES)}
          className="inline-flex h-[54px] items-center justify-center gap-2 rounded-2xl border border-[#423554] bg-[#241C2F] font-display text-sm font-extrabold text-[#F7F2EC] transition hover:border-[#F4CD6C]/60"
        >
          <Edit3 className="h-4 w-4" />
          <span>Edit Setup</span>
        </button>
        <GoldCtaButton
          text="One more round"
          onClick={onPlayAnotherRound}
          icon={<RotateCcw className="h-4 w-4" />}
        />
      </div>
    </div>
  );
};
