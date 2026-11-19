import React, { useEffect, useState } from 'react';
import { Eye, EyeOff, Lightbulb, RefreshCw, Sparkles } from 'lucide-react';
import { CharacterAvatarThumbnail } from '../components/AvatarCardGraphics';
import { GoldCtaButton } from '../components/ImpostorWizardComponents';
import { RoleEmblemBadge, RoleEmblemChip } from '../components/RoleEmblem';
import {
  AgentDirectorBadge,
  AgentPredictionCard,
  OtherImpostorAllyNote,
  SaviorAbilityCard,
  UltimateKillerAbilityCard,
} from '../components/RoundAbilitiesUi';
import { SoundCuePlayer } from '../audio/soundCuePlayer';
import { AvatarCatalog } from '../data/avatarCatalog';
import { getRoleForPlayer, getWordBriefingForPlayer } from '../hooks/useLocalGame';
import { getRoleVisualStyle, PALETTE, resolveRoleIdentity } from '../theme/palette';
import { LocalGameUiState, PlayerRole } from '../types/game';

interface RoleRevealScreenProps {
  state: LocalGameUiState;
  onShowCard: () => void;
  onReshuffleRoles: () => void;
  onHideAndAdvance: () => void;
  onSelectKillerTarget: (targetId: string) => void;
  onSelectSaviorProtected: (targetId: string) => void;
  onSelectAgentPrediction: (agentId: string, suspectId: string) => void;
}

export const RoleRevealScreen: React.FC<RoleRevealScreenProps> = ({
  state,
  onShowCard,
  onReshuffleRoles,
  onHideAndAdvance,
  onSelectKillerTarget,
  onSelectSaviorProtected,
  onSelectAgentPrediction,
}) => {
  const [lockSeconds, setLockSeconds] = useState(0);

  const player = state.players[state.revealPlayerIndex] ?? state.players[0];
  const isLastPlayer = state.revealPlayerIndex >= state.players.length - 1;
  const totalAgents = Math.max(0, state.players.length - state.impostorCount);
  const canReshuffle =
    state.revealPlayerIndex === 0 && !state.isRoleCardVisible;

  useEffect(() => {
    if (!state.isRoleCardVisible) {
      setLockSeconds(0);
      return;
    }
    setLockSeconds(3);
    const id = window.setInterval(() => {
      setLockSeconds((prev) => (prev > 0 ? prev - 1 : 0));
    }, 1000);
    return () => window.clearInterval(id);
  }, [state.isRoleCardVisible, state.revealPlayerIndex]);

  if (!player) return null;

  const briefing = getWordBriefingForPlayer(state, player.id);
  const role = getRoleForPlayer(state, player.id);
  const isImpostor = role === PlayerRole.IMPOSTOR;
  const isKiller = player.id === state.ultimateKillerId;
  const isSavior = player.id === state.saviorAgentId;
  const isDirector = player.id === state.agentDirectorId;
  const identity = resolveRoleIdentity(role, isKiller, isSavior, isDirector);
  const roleStyle = getRoleVisualStyle(identity);
  const character = AvatarCatalog.findById(player.avatarId);

  const allyNames = state.players
    .filter((p) => p.id !== player.id && state.impostorIds.has(p.id))
    .map((p) => p.name);

  const handleFlipReveal = () => {
    if (state.isRoleCardVisible) return;
    SoundCuePlayer.playRoleRevealCue(isImpostor);
    onShowCard();
  };

  return (
    <div className="mx-auto flex w-full max-w-xl flex-col gap-5 px-5 pb-10 pt-2">
      {/* Top Progress Row */}
      <div className="flex items-center justify-between gap-2">
        <span className="rounded-full border border-[#423554] bg-[#241C2F] px-3 py-1 font-mono-label text-[10px] font-bold uppercase tracking-wider text-[#B7ABC8]">
          {state.impostorCount} IMPOSTOR{state.impostorCount > 1 ? 'S' : ''} &bull;{' '}
          {totalAgents} AGENT{totalAgents === 1 ? '' : 'S'}
        </span>

        {canReshuffle ? (
          <button
            type="button"
            onClick={onReshuffleRoles}
            className="inline-flex items-center gap-1.5 rounded-full border border-[#F4CD6C]/50 bg-[#3A2F17]/60 px-3 py-1 font-mono-label text-[10px] font-extrabold uppercase tracking-wider text-[#FFDE8A] hover:bg-[#3A2F17]"
          >
            <RefreshCw className="h-3 w-3" />
            <span>Re-deal</span>
          </button>
        ) : (
          <span className="font-mono-label text-[11px] font-bold text-[#F4CD6C]">
            Player {state.revealPlayerIndex + 1} / {state.players.length}
          </span>
        )}
      </div>

      {/* Player Title + Progress Dots */}
      <div className="text-center">
        <div className="font-mono-label text-[10px] font-bold uppercase tracking-[0.24em] text-[#F4CD6C]">
          FOR YOUR EYES ONLY
        </div>
        <h2 className="mt-1 font-display text-3xl font-black text-[#F7F2EC]">
          {player.name}
        </h2>
        <div className="mt-3 flex items-center justify-center gap-1.5">
          {state.players.map((p, idx) => (
            <span
              key={p.id}
              className={`h-2 rounded-full transition-all ${
                idx === state.revealPlayerIndex
                  ? 'w-7 bg-[#F4CD6C]'
                  : idx < state.revealPlayerIndex
                  ? 'w-2.5 bg-[#1AE682]'
                  : 'w-2 bg-[#423554]'
              }`}
            />
          ))}
        </div>
      </div>

      {/* 3D Perspective Flip Card */}
      <div className="perspective-1200 w-full">
        <div
          onClick={!state.isRoleCardVisible ? handleFlipReveal : undefined}
          role={!state.isRoleCardVisible ? 'button' : undefined}
          tabIndex={!state.isRoleCardVisible ? 0 : undefined}
          onKeyDown={
            !state.isRoleCardVisible
              ? (e) => {
                  if (e.key === 'Enter' || e.key === ' ') {
                    e.preventDefault();
                    handleFlipReveal();
                  }
                }
              : undefined
          }
          className={`transform-style-3d relative min-h-[340px] w-full transition-transform duration-700 ${
            state.isRoleCardVisible
              ? '[transform:rotateY(180deg)]'
              : 'cursor-pointer hover:scale-[1.01]'
          }`}
        >
          {/* Card Front Face (Hidden) */}
          <div className="backface-hidden absolute inset-0 flex flex-col items-center justify-center rounded-3xl border-2 border-[#F4CD6C]/55 bg-gradient-to-br from-[#2C2339] via-[#241C2F] to-[#16101E] p-6 text-center shadow-2xl">
            <CharacterAvatarThumbnail
              character={character}
              pose={player.avatarPose}
              size={84}
              borderColor={PALETTE.goldAccent}
            />
            <div className="mt-4 inline-flex items-center gap-1.5 rounded-full border border-[#F4CD6C]/40 bg-[#3A2F17]/70 px-3 py-1 font-mono-label text-[10px] font-extrabold uppercase tracking-[0.2em] text-[#FFDE8A]">
              <Sparkles className="h-3.5 w-3.5" />
              <span>CLASSIFIED DOSSIER</span>
            </div>
            <h3 className="mt-3 font-display text-2xl font-extrabold text-[#F7F2EC]">
              Hand phone to {player.name}
            </h3>
            <p className="mt-1.5 text-sm font-medium text-[#B7ABC8]">
              Make sure nobody else is peeking, then tap to flip your secret role.
            </p>
            <div className="mt-5 inline-flex items-center gap-2 rounded-2xl bg-[#F4CD6C] px-5 py-2.5 font-display text-sm font-extrabold text-[#1D1626] shadow-lg">
              <Eye className="h-4 w-4" />
              <span>Tap to flip</span>
            </div>
          </div>

          {/* Card Back Face (Revealed) */}
          <div
            className="backface-hidden [transform:rotateY(180deg)] absolute inset-0 flex flex-col justify-between rounded-3xl border-2 p-5 shadow-2xl"
            style={{
              borderColor: roleStyle.accent,
              background: `linear-gradient(160deg, ${roleStyle.deepSurface} 0%, #1D1626 60%, #16101E 100%)`,
            }}
          >
            <div className="flex flex-wrap items-center justify-between gap-2">
              <div className="flex flex-wrap gap-1.5">
                {briefing?.category && (
                  <span className="rounded-xl border border-[#423554] bg-[#16101E] px-2.5 py-1 font-mono-label text-[10px] font-bold uppercase text-[#F4CD6C]">
                    {briefing.category}
                  </span>
                )}
                {briefing?.genre && (
                  <span className="rounded-xl border border-[#423554] bg-[#241C2F] px-2.5 py-1 font-mono-label text-[10px] font-semibold uppercase text-[#B7ABC8]">
                    {briefing.genre}
                  </span>
                )}
              </div>
              <RoleEmblemChip identity={identity} />
            </div>

            <div className="my-3 flex items-center justify-center">
              <RoleEmblemBadge identity={identity} size={68} />
            </div>

            <div
              className="rounded-2xl border p-4 text-center"
              style={{
                borderColor: `${roleStyle.accent}66`,
                backgroundColor: '#16101EE0',
              }}
            >
              {isImpostor ? (
                <>
                  <div className="inline-flex items-center gap-1.5 font-mono-label text-[11px] font-extrabold uppercase tracking-[0.2em] text-[#FF7A8F]">
                    <EyeOff className="h-3.5 w-3.5" />
                    <span>SECRET WORD: HIDDEN</span>
                  </div>
                  <p className="mt-1.5 text-xs font-medium leading-relaxed text-[#B7ABC8]">
                    Listen carefully to the clues around the table and blend in
                    without getting caught.
                  </p>
                </>
              ) : (
                <>
                  <div
                    className="font-mono-label text-[10px] font-bold uppercase tracking-[0.22em]"
                    style={{ color: roleStyle.glow }}
                  >
                    SECRET WORD
                  </div>
                  <div className="mt-1 font-display text-3xl font-black text-[#F7F2EC]">
                    {briefing?.secretWord ?? '—'}
                  </div>
                </>
              )}
            </div>

            {!isImpostor && state.hintsEnabled && briefing?.agentHint && (
              <div className="mt-3 flex items-center gap-2 rounded-2xl border border-[#F4CD6C]/40 bg-[#3A2F17]/50 px-3.5 py-2.5">
                <Lightbulb className="h-4 w-4 shrink-0 text-[#F4CD6C]" />
                <span className="text-xs font-semibold text-[#F7F2EC]">
                  Hint: <strong className="text-[#FFDE8A]">{briefing.agentHint}</strong>
                </span>
              </div>
            )}
          </div>
        </div>
      </div>

      {/* Post-Flip Special Role Cards */}
      {state.isRoleCardVisible && (
        <div className="space-y-3">
          {isImpostor && <OtherImpostorAllyNote allyNames={allyNames} />}
          {isKiller && (
            <UltimateKillerAbilityCard
              killerId={player.id}
              players={state.players}
              selectedTargetId={state.nullifiedVoterId}
              onSelectTarget={onSelectKillerTarget}
            />
          )}
          {isSavior && (
            <SaviorAbilityCard
              saviorId={player.id}
              players={state.players}
              protectedPlayerId={state.protectedPlayerId}
              onSelectProtected={onSelectSaviorProtected}
            />
          )}
          {isDirector && <AgentDirectorBadge />}
          {!isImpostor && state.agentPredictionEnabled && (
            <AgentPredictionCard
              agentId={player.id}
              players={state.players}
              selectedSuspectId={state.agentPredictions[player.id] ?? null}
              onSelectSuspect={(suspectId) =>
                onSelectAgentPrediction(player.id, suspectId)
              }
            />
          )}
        </div>
      )}

      {/* Bottom CTA */}
      <div className="pt-1">
        {!state.isRoleCardVisible ? (
          <GoldCtaButton
            text="Read your card first — Tap card to flip"
            disabled
            onClick={() => {}}
          />
        ) : (
          <GoldCtaButton
            text={
              isLastPlayer
                ? lockSeconds > 0
                  ? `Got it • start (${lockSeconds}s)`
                  : 'Got it • start discussion'
                : lockSeconds > 0
                ? `Pass the phone (${lockSeconds}s)`
                : 'Pass the phone'
            }
            disabled={lockSeconds > 0}
            onClick={onHideAndAdvance}
          />
        )}
      </div>
    </div>
  );
};
