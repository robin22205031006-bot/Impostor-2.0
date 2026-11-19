import React from 'react';
import { EyeOff, Lightbulb, ShieldAlert } from 'lucide-react';
import { AvatarCatalog } from '../data/avatarCatalog';
import {
  getRoleVisualStyle,
  resolveRoleIdentity,
} from '../theme/palette';
import {
  AvatarPose,
  PlayerRole,
  PlayerWordBriefing,
} from '../types/game';
import { CharacterAvatarThumbnail } from './AvatarCardGraphics';
import { RoleEmblemBadge, RoleEmblemChip } from './RoleEmblem';

interface RoleWordCardProps {
  playerName: string;
  avatarId: string;
  avatarPose?: AvatarPose;
  briefing: PlayerWordBriefing;
  showHint?: boolean;
  isUltimateKiller?: boolean;
  isSavior?: boolean;
  isDirector?: boolean;
  className?: string;
}

export const RoleWordCard: React.FC<RoleWordCardProps> = ({
  playerName,
  avatarId,
  avatarPose = AvatarPose.ORIGINAL,
  briefing,
  showHint = true,
  isUltimateKiller = false,
  isSavior = false,
  isDirector = false,
  className = '',
}) => {
  const isImpostor = briefing.role === PlayerRole.IMPOSTOR;
  const identity = resolveRoleIdentity(
    briefing.role,
    isUltimateKiller,
    isSavior,
    isDirector
  );
  const style = getRoleVisualStyle(identity);
  const character = AvatarCatalog.findById(avatarId);

  return (
    <div
      className={`relative overflow-hidden rounded-3xl border-2 p-5 shadow-2xl ${className}`}
      style={{
        borderColor: `${style.accent}B3`,
        background: `linear-gradient(160deg, ${style.deepSurface} 0%, #1D1626 55%, #16101E 100%)`,
        boxShadow: `0 12px 34px ${style.accent}24`,
      }}
    >
      {/* Top Category + Role Emblem Chip */}
      <div className="flex flex-wrap items-center justify-between gap-2">
        <div className="flex flex-wrap items-center gap-1.5">
          {briefing.category && (
            <span className="rounded-xl border border-[#423554] bg-[#16101E]/90 px-2.5 py-1 font-mono-label text-[10px] font-bold uppercase tracking-wider text-[#F4CD6C]">
              {briefing.category}
            </span>
          )}
          {briefing.genre && (
            <span className="rounded-xl border border-[#423554] bg-[#241C2F]/80 px-2.5 py-1 font-mono-label text-[10px] font-semibold uppercase tracking-wider text-[#B7ABC8]">
              {briefing.genre}
            </span>
          )}
        </div>
        <RoleEmblemChip identity={identity} />
      </div>

      {/* Player Avatar + Role Emblem Medallion */}
      <div className="mt-5 flex items-center gap-3.5">
        <CharacterAvatarThumbnail
          character={character}
          pose={avatarPose}
          size={58}
          borderColor={style.accent}
        />
        <div className="min-w-0 flex-1">
          <div className="font-mono-label text-[10px] font-bold uppercase tracking-[0.2em] text-[#887B9B]">
            CLASSIFIED DOSSIER
          </div>
          <div className="truncate font-display text-xl font-extrabold text-[#F7F2EC]">
            {playerName}
          </div>
        </div>
        <RoleEmblemBadge identity={identity} size={52} />
      </div>

      {/* Word / Hidden Role Box */}
      <div
        className="mt-5 rounded-2xl border p-4 text-center"
        style={{
          borderColor: `${style.accent}66`,
          backgroundColor: '#16101ED9',
        }}
      >
        {isImpostor || briefing.isSecretWordHidden ? (
          <div className="space-y-1.5">
            <div className="inline-flex items-center gap-1.5 font-mono-label text-[11px] font-extrabold uppercase tracking-[0.2em] text-[#FF7A8F]">
              <EyeOff className="h-3.5 w-3.5" />
              <span>SECRET WORD: HIDDEN</span>
            </div>
            <p className="text-sm font-medium leading-relaxed text-[#B7ABC8]">
              Blend in with the agents, read the table&apos;s clues carefully,
              and avoid getting exposed!
            </p>
          </div>
        ) : (
          <div className="space-y-1">
            <div
              className="font-mono-label text-[10px] font-bold uppercase tracking-[0.22em]"
              style={{ color: style.glow }}
            >
              SECRET WORD
            </div>
            <div className="font-display text-3xl font-black tracking-tight text-[#F7F2EC]">
              {briefing.secretWord ?? '—'}
            </div>
          </div>
        )}
      </div>

      {/* Agent Hint Box */}
      {!isImpostor && showHint && briefing.agentHint && (
        <div className="mt-3.5 flex items-start gap-2.5 rounded-2xl border border-[#F4CD6C]/35 bg-[#3A2F17]/45 p-3.5">
          <Lightbulb className="mt-0.5 h-4 w-4 shrink-0 text-[#F4CD6C]" />
          <div>
            <div className="font-mono-label text-[10px] font-bold uppercase tracking-[0.18em] text-[#F4CD6C]">
              CLUE / HINT
            </div>
            <p className="mt-0.5 text-xs font-medium leading-relaxed text-[#F7F2EC]">
              {briefing.agentHint}
            </p>
          </div>
        </div>
      )}

      {/* Impostor reminder when category clues disabled */}
      {isImpostor && !briefing.category && (
        <div className="mt-3.5 flex items-center justify-center gap-2 rounded-2xl border border-[#FF3E5B]/30 bg-[#341624]/60 px-3 py-2 text-xs font-semibold text-[#FF7A8F]">
          <ShieldAlert className="h-4 w-4 shrink-0" />
          <span>No category clue in this rule setup — stay sharp!</span>
        </div>
      )}
    </div>
  );
};
