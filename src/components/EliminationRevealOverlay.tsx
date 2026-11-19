import React, { useEffect, useRef, useState } from 'react';
import { ShieldCheck, Skull, Sparkles } from 'lucide-react';
import { AvatarCatalog } from '../data/avatarCatalog';
import { getRoleVisualStyle, ROLE_IDENTITY_META } from '../theme/palette';
import { EliminatedPlayerReveal } from '../types/game';
import { CharacterAvatarThumbnail } from './AvatarCardGraphics';
import { RoleEmblemBadge, RoleEmblemChip } from './RoleEmblem';
import { RoundCelebrationFx } from './RoundCelebrationFx';

interface EliminationRevealOverlayProps {
  visible: boolean;
  eliminatedPlayers: EliminatedPlayerReveal[];
  crewWon: boolean;
  verdictTitle: string;
  verdictSubtitle: string;
  onRevealVerdict: () => void;
  onContinueToResults: () => void;
}

export const EliminationRevealOverlay: React.FC<
  EliminationRevealOverlayProps
> = ({
  visible,
  eliminatedPlayers,
  crewWon,
  verdictTitle,
  verdictSubtitle,
  onRevealVerdict,
  onContinueToResults,
}) => {
  const [revealed, setRevealed] = useState(false);
  const verdictFiredRef = useRef(false);

  useEffect(() => {
    if (!visible) {
      setRevealed(false);
      verdictFiredRef.current = false;
      return;
    }

    setRevealed(false);
    verdictFiredRef.current = false;
    const timer = window.setTimeout(() => {
      setRevealed(true);
      if (!verdictFiredRef.current) {
        verdictFiredRef.current = true;
        onRevealVerdict();
      }
    }, 950);

    return () => window.clearTimeout(timer);
  }, [visible, onRevealVerdict]);

  if (!visible) return null;

  const handleSkipReveal = () => {
    setRevealed(true);
    if (!verdictFiredRef.current) {
      verdictFiredRef.current = true;
      onRevealVerdict();
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-[#16101E]/95 p-5 backdrop-blur-md">
      {revealed && (
        <RoundCelebrationFx outcome={crewWon ? 'agents' : 'impostor'} />
      )}
      <div
        className={`relative z-10 w-full max-w-lg rounded-3xl border bg-[#241C2F] p-6 shadow-2xl ${
          revealed
            ? crewWon
              ? 'border-[#1AE682]/60 animate-[verdictPop_520ms_cubic-bezier(0.22,1,0.36,1)]'
              : 'border-[#FF3E5B]/75 animate-[screenShake_650ms_cubic-bezier(0.36,0.07,0.19,0.97)]'
            : 'border-[#423554]'
        }`}
      >
        {!revealed ? (
          <div className="py-10 text-center">
            <div className="mx-auto flex h-20 w-20 items-center justify-center rounded-full border-2 border-[#F4CD6C]/70 bg-[#3A2F17] text-[#F4CD6C] animate-pulse">
              <Sparkles className="h-9 w-9" />
            </div>
            <div className="mt-5 font-mono-label text-xs font-extrabold uppercase tracking-[0.24em] text-[#F4CD6C]">
              REVEALING TRUE IDENTITY...
            </div>
            <p className="mt-2 text-sm font-medium text-[#B7ABC8]">
              Locking the final ballots and decrypting dossiers.
            </p>
            <button
              type="button"
              onClick={handleSkipReveal}
              className="mt-6 rounded-2xl border border-[#423554] bg-[#2C2339] px-5 py-2.5 text-xs font-bold text-[#F7F2EC] transition hover:border-[#F4CD6C]/60"
            >
              Skip Reveal
            </button>
          </div>
        ) : (
          <div className="space-y-5">
            {/* Eliminated Players Card(s) */}
            <div>
              <div className="text-center font-mono-label text-[11px] font-bold uppercase tracking-[0.22em] text-[#F4CD6C]">
                TRUE ROLE REVEALED
              </div>

              {eliminatedPlayers.length === 0 ? (
                <div className="mt-3 rounded-2xl border border-[#423554] bg-[#16101E] p-5 text-center">
                  <div className="font-display text-xl font-extrabold text-[#F7F2EC]">
                    No Player Eliminated
                  </div>
                  <p className="mt-1 text-xs font-medium text-[#B7ABC8]">
                    The table ended in a tie or zero decisive votes.
                  </p>
                </div>
              ) : (
                <div className="mt-3 space-y-2.5">
                  {eliminatedPlayers.map((player) => {
                    const style = getRoleVisualStyle(player.roleIdentity);
                    const meta = ROLE_IDENTITY_META[player.roleIdentity];
                    const char = AvatarCatalog.findById(player.avatarId);
                    return (
                      <div
                        key={player.id}
                        className="flex items-center gap-3.5 rounded-2xl border-2 p-3.5"
                        style={{
                          borderColor: `${style.accent}99`,
                          backgroundColor: style.deepSurface,
                        }}
                      >
                        <CharacterAvatarThumbnail
                          character={char}
                          pose={player.avatarPose}
                          size={52}
                          borderColor={style.accent}
                        />
                        <div className="min-w-0 flex-1">
                          <div className="truncate font-display text-lg font-extrabold text-[#F7F2EC]">
                            {player.name}
                          </div>
                          <div className="mt-0.5 flex flex-wrap items-center gap-2">
                            <RoleEmblemChip
                              identity={player.roleIdentity}
                              useShortLabel
                            />
                            <span className="font-mono-label text-[11px] font-bold text-[#B7ABC8]">
                              {player.voteCount} vote
                              {player.voteCount === 1 ? '' : 's'}
                            </span>
                          </div>
                          <div className="mt-1 text-[11px] text-[#B7ABC8]">
                            Confirmed {meta.displayTitle}
                          </div>
                        </div>
                        <RoleEmblemBadge
                          identity={player.roleIdentity}
                          size={44}
                        />
                      </div>
                    );
                  })}
                </div>
              )}
            </div>

            {/* Verdict Banner */}
            <div
              className="rounded-2xl border p-4 text-center"
              style={{
                borderColor: crewWon
                  ? 'rgba(26,230,130,0.6)'
                  : 'rgba(255,62,91,0.6)',
                backgroundColor: crewWon ? '#122C24' : '#341624',
              }}
            >
              <div className="flex items-center justify-center gap-2">
                {crewWon ? (
                  <ShieldCheck className="h-5 w-5 text-[#1AE682]" />
                ) : (
                  <Skull className="h-5 w-5 text-[#FF3E5B]" />
                )}
                <h3
                  className="font-display text-xl font-black"
                  style={{ color: crewWon ? '#5CFFB0' : '#FF7A8F' }}
                >
                  {verdictTitle}
                </h3>
              </div>
              <p className="mt-1 text-xs font-medium text-[#F7F2EC]">
                {verdictSubtitle}
              </p>
            </div>

            <button
              type="button"
              onClick={onContinueToResults}
              className="inline-flex h-[52px] w-full items-center justify-center rounded-2xl bg-[#F4CD6C] font-display text-base font-extrabold text-[#1D1626] shadow-[0_8px_22px_rgba(244,205,108,0.28)] transition hover:bg-[#FFDE8A] active:scale-[0.985]"
            >
              View Full Breakdown &amp; Scores
            </button>
          </div>
        )}
      </div>
    </div>
  );
};
