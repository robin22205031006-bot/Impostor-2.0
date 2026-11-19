import React, { useMemo, useState } from 'react';
import { Check, CheckCircle2, RotateCcw, Vote } from 'lucide-react';
import { AvatarCatalog } from '../data/avatarCatalog';
import { GameState } from '../types/game';
import { CharacterAvatarThumbnail } from './AvatarCardGraphics';
import { GoldCtaButton } from './ImpostorWizardComponents';

interface VotingScreenProps {
  gameState: GameState;
  onCastVote: (voterId: string, suspectId: string) => void;
  onRevealVerdict: () => void;
  onBackToLobby: () => void;
}

export const VotingScreen: React.FC<VotingScreenProps> = ({
  gameState,
  onCastVote,
  onRevealVerdict,
  onBackToLobby,
}) => {
  const [selectedVoterId, setSelectedVoterId] = useState<string>(
    gameState.players[0]?.id ?? ''
  );

  const activeVoter = useMemo(
    () =>
      gameState.players.find((p) => p.id === selectedVoterId) ??
      gameState.players[0] ??
      null,
    [gameState.players, selectedVoterId]
  );

  const currentVoterChoice = activeVoter
    ? gameState.votesByPlayerId[activeVoter.id] ?? null
    : null;
  const castVotesCount = Object.keys(gameState.votesByPlayerId).length;
  const totalPlayers = gameState.players.length;

  return (
    <div className="mx-auto flex w-full max-w-2xl flex-col gap-5 pb-8">
      {/* Header Card */}
      <div className="rounded-3xl border border-[#423554] bg-[#241C2F] p-5">
        <div className="flex items-center justify-between gap-3">
          <div>
            <div className="font-mono-label text-[10px] font-bold uppercase tracking-[0.22em] text-[#F4CD6C]">
              VOTING &bull; SELECT ACTIVE VOTER
            </div>
            <h2 className="mt-1 font-display text-2xl font-extrabold text-[#F7F2EC]">
              Cast Secret Ballots
            </h2>
          </div>
          <div className="rounded-2xl border border-[#423554] bg-[#16101E] px-3.5 py-2 text-right">
            <div className="font-mono-label text-[10px] text-[#887B9B]">
              VOTES LOCKED
            </div>
            <div className="font-display text-lg font-extrabold text-[#F4CD6C]">
              {castVotesCount} / {totalPlayers}
            </div>
          </div>
        </div>

        {/* Voter Selector Chips */}
        <div className="mt-4 flex gap-2 overflow-x-auto pb-1">
          {gameState.players.map((player) => {
            const isSelected = activeVoter?.id === player.id;
            const hasVoted = Boolean(gameState.votesByPlayerId[player.id]);
            const char = AvatarCatalog.findById(player.avatarId);
            return (
              <button
                key={player.id}
                type="button"
                onClick={() => setSelectedVoterId(player.id)}
                className={`flex shrink-0 items-center gap-2 rounded-2xl border px-3 py-2 text-xs font-bold transition ${
                  isSelected
                    ? 'border-[#F4CD6C] bg-[#3A2F17] text-[#FFDE8A]'
                    : 'border-[#423554] bg-[#16101E] text-[#B7ABC8] hover:border-[#F4CD6C]/50'
                }`}
              >
                <CharacterAvatarThumbnail
                  character={char}
                  pose={player.avatarPose}
                  size={24}
                />
                <span>{player.name}</span>
                {hasVoted && (
                  <CheckCircle2 className="h-3.5 w-3.5 text-[#1AE682]" />
                )}
              </button>
            );
          })}
        </div>
      </div>

      {/* Suspect Selection Grid */}
      {activeVoter && (
        <div className="space-y-3">
          <div className="flex items-center justify-between px-1">
            <span className="text-sm font-bold text-[#B7ABC8]">
              Voting as{' '}
              <span className="text-[#F4CD6C]">{activeVoter.name}</span> — tap a
              suspect:
            </span>
          </div>

          <div className="grid grid-cols-1 gap-3 sm:grid-cols-2">
            {gameState.players.map((suspect) => {
              const isSelf = suspect.id === activeVoter.id;
              const isPicked = currentVoterChoice === suspect.id;
              const voteCount = gameState.totalVotesPerPlayer[suspect.id] ?? 0;
              const char = AvatarCatalog.findById(suspect.avatarId);

              return (
                <button
                  key={suspect.id}
                  type="button"
                  disabled={isSelf}
                  onClick={() => onCastVote(activeVoter.id, suspect.id)}
                  className={`flex items-center gap-3.5 rounded-3xl border p-4 text-left transition ${
                    isSelf
                      ? 'cursor-not-allowed border-[#423554]/50 bg-[#16101E]/60 opacity-45'
                      : isPicked
                      ? 'border-[#FF3E5B] bg-[#341624] shadow-[0_0_22px_rgba(255,62,91,0.22)]'
                      : 'border-[#423554] bg-[#241C2F] hover:border-[#F4CD6C]/55'
                  }`}
                >
                  <CharacterAvatarThumbnail
                    character={char}
                    pose={suspect.avatarPose}
                    size={48}
                    borderColor={isPicked ? '#FF3E5B' : char.accentColor}
                  />
                  <div className="min-w-0 flex-1">
                    <div className="truncate font-display text-base font-extrabold text-[#F7F2EC]">
                      {suspect.name}
                    </div>
                    <div className="mt-0.5 font-mono-label text-[10px] font-bold uppercase tracking-wider text-[#887B9B]">
                      {isSelf
                        ? 'Cannot vote yourself'
                        : isPicked
                        ? 'SELECTED FOR ELIMINATION'
                        : `${voteCount} vote${voteCount === 1 ? '' : 's'} so far`}
                    </div>
                  </div>
                  {isPicked && (
                    <span className="flex h-8 w-8 shrink-0 items-center justify-center rounded-full bg-[#FF3E5B] text-white">
                      <Check className="h-4 w-4" strokeWidth={3} />
                    </span>
                  )}
                </button>
              );
            })}
          </div>
        </div>
      )}

      {/* Footer Actions */}
      <div className="space-y-3 pt-2">
        <GoldCtaButton
          text="Reveal Verdict"
          onClick={onRevealVerdict}
          icon={<Vote className="h-5 w-5" />}
        />
        <button
          type="button"
          onClick={onBackToLobby}
          className="inline-flex h-12 w-full items-center justify-center gap-2 rounded-2xl border border-[#423554] bg-[#241C2F] text-sm font-bold text-[#B7ABC8] transition hover:border-[#F4CD6C]/50 hover:text-[#F7F2EC]"
        >
          <RotateCcw className="h-4 w-4" />
          <span>Return to Lobby</span>
        </button>
      </div>
    </div>
  );
};
