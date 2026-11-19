import React from 'react';
import {
  CheckCircle2,
  Crosshair,
  Crown,
  Eye,
  Shield,
  Users,
} from 'lucide-react';
import { AvatarCatalog } from '../data/avatarCatalog';
import { PALETTE } from '../theme/palette';
import { GameRoleIdentity, LocalPlayer } from '../types/game';
import { CharacterAvatarThumbnail } from './AvatarCardGraphics';
import { RoleEmblemBadge } from './RoleEmblem';

interface UltimateKillerAbilityCardProps {
  killerId: string;
  players: LocalPlayer[];
  selectedTargetId: string | null;
  onSelectTarget: (targetId: string) => void;
}

export const UltimateKillerAbilityCard: React.FC<
  UltimateKillerAbilityCardProps
> = ({ killerId, players, selectedTargetId, onSelectTarget }) => {
  const targets = players.filter((p) => p.id !== killerId);

  return (
    <div
      className="rounded-3xl border p-4"
      style={{
        borderColor: `${PALETTE.killerAccent}66`,
        backgroundColor: `${PALETTE.killerSurface}CC`,
      }}
    >
      <div className="flex items-center gap-3">
        <RoleEmblemBadge identity={GameRoleIdentity.AGENT_KILLER} size={42} />
        <div className="min-w-0 flex-1">
          <div className="flex items-center gap-1.5 font-mono-label text-[10px] font-bold uppercase tracking-[0.18em] text-[#FF78B4]">
            <Crosshair className="h-3.5 w-3.5" />
            <span>AGENT KILLER POWER</span>
          </div>
          <p className="text-xs font-semibold text-[#F7F2EC]">
            Select one player&apos;s vote to secretly nullify this round.
          </p>
        </div>
      </div>

      <div className="mt-3 flex flex-wrap gap-2">
        {targets.map((target) => {
          const selected = target.id === selectedTargetId;
          const char = AvatarCatalog.findById(target.avatarId);
          return (
            <button
              key={target.id}
              type="button"
              onClick={() => onSelectTarget(target.id)}
              className={`inline-flex items-center gap-2 rounded-2xl border px-3 py-1.5 text-xs font-bold transition ${
                selected
                  ? 'border-[#FF2985] bg-[#FF2985]/25 text-[#F7F2EC]'
                  : 'border-[#423554] bg-[#16101E] text-[#B7ABC8] hover:border-[#FF2985]/50'
              }`}
            >
              <CharacterAvatarThumbnail
                character={char}
                pose={target.avatarPose}
                size={22}
              />
              <span>{target.name}</span>
              {selected && <CheckCircle2 className="h-3.5 w-3.5 text-[#FF78B4]" />}
            </button>
          );
        })}
      </div>
    </div>
  );
};

interface AgentPredictionCardProps {
  agentId: string;
  players: LocalPlayer[];
  selectedSuspectId: string | null;
  onSelectSuspect: (suspectId: string) => void;
}

export const AgentPredictionCard: React.FC<AgentPredictionCardProps> = ({
  agentId,
  players,
  selectedSuspectId,
  onSelectSuspect,
}) => {
  const suspects = players.filter((p) => p.id !== agentId);

  return (
    <div className="rounded-3xl border border-[#F4CD6C]/45 bg-[#3A2F17]/40 p-4">
      <div className="flex items-center gap-2 font-mono-label text-[10px] font-bold uppercase tracking-[0.18em] text-[#F4CD6C]">
        <Eye className="h-3.5 w-3.5" />
        <span>BLIND SUSPICION BONUS (+1 PT)</span>
      </div>
      <p className="mt-1 text-xs font-medium text-[#B7ABC8]">
        Optional: Pick who you suspect before hearing clues. +1 bonus point if
        you&apos;re right!
      </p>

      <div className="mt-3 flex flex-wrap gap-2">
        {suspects.map((p) => {
          const selected = p.id === selectedSuspectId;
          return (
            <button
              key={p.id}
              type="button"
              onClick={() => onSelectSuspect(p.id)}
              className={`rounded-xl border px-3 py-1.5 text-xs font-bold transition ${
                selected
                  ? 'border-[#F4CD6C] bg-[#F4CD6C] text-[#1D1626]'
                  : 'border-[#423554] bg-[#16101E] text-[#B7ABC8] hover:border-[#F4CD6C]/50'
              }`}
            >
              {p.name}
            </button>
          );
        })}
      </div>
    </div>
  );
};

interface SaviorAbilityCardProps {
  saviorId: string;
  players: LocalPlayer[];
  protectedPlayerId: string | null;
  onSelectProtected: (playerId: string) => void;
}

export const SaviorAbilityCard: React.FC<SaviorAbilityCardProps> = ({
  saviorId,
  players,
  protectedPlayerId,
  onSelectProtected,
}) => {
  const candidates = players.filter((p) => p.id !== saviorId);

  return (
    <div
      className="rounded-3xl border p-4"
      style={{
        borderColor: `${PALETTE.saviorAccent}66`,
        backgroundColor: `${PALETTE.saviorSurface}CC`,
      }}
    >
      <div className="flex items-center gap-3">
        <RoleEmblemBadge identity={GameRoleIdentity.SAVIOR_AGENT} size={42} />
        <div className="min-w-0 flex-1">
          <div className="flex items-center gap-1.5 font-mono-label text-[10px] font-bold uppercase tracking-[0.18em] text-[#82E4FF]">
            <Shield className="h-3.5 w-3.5" />
            <span>SAVIOR AGENT AEGIS SHIELD</span>
          </div>
          <p className="text-xs font-semibold text-[#F7F2EC]">
            Shield a player&apos;s vote against Agent Killer interference.
          </p>
        </div>
      </div>

      <div className="mt-3 flex flex-wrap gap-2">
        {candidates.map((target) => {
          const selected = target.id === protectedPlayerId;
          return (
            <button
              key={target.id}
              type="button"
              onClick={() => onSelectProtected(target.id)}
              className={`inline-flex items-center gap-1.5 rounded-2xl border px-3 py-1.5 text-xs font-bold transition ${
                selected
                  ? 'border-[#2CCBFF] bg-[#2CCBFF]/25 text-[#F7F2EC]'
                  : 'border-[#423554] bg-[#16101E] text-[#B7ABC8] hover:border-[#2CCBFF]/50'
              }`}
            >
              <span>{target.name}</span>
              {selected && <Shield className="h-3.5 w-3.5 text-[#82E4FF]" />}
            </button>
          );
        })}
      </div>
    </div>
  );
};

export const AgentDirectorBadge: React.FC = () => {
  return (
    <div
      className="flex items-center gap-3 rounded-3xl border p-4"
      style={{
        borderColor: `${PALETTE.directorAccent}66`,
        backgroundColor: `${PALETTE.directorSurface}CC`,
      }}
    >
      <RoleEmblemBadge identity={GameRoleIdentity.AGENT_DIRECTOR} size={42} />
      <div className="min-w-0 flex-1">
        <div className="flex items-center gap-1.5 font-mono-label text-[10px] font-bold uppercase tracking-[0.18em] text-[#FFDA66]">
          <Crown className="h-3.5 w-3.5" />
          <span>AGENT DIRECTOR PASSIVE</span>
        </div>
        <p className="text-xs font-semibold text-[#F7F2EC]">
          Your vote carries <strong className="text-[#FFDA66]">2x weight</strong>{' '}
          during the final tally!
        </p>
      </div>
    </div>
  );
};

interface OtherImpostorAllyNoteProps {
  allyNames: string[];
}

export const OtherImpostorAllyNote: React.FC<OtherImpostorAllyNoteProps> = ({
  allyNames,
}) => {
  if (allyNames.length === 0) return null;
  return (
    <div className="flex items-center gap-2.5 rounded-2xl border border-[#FF3E5B]/45 bg-[#341624]/80 px-4 py-3">
      <Users className="h-4 w-4 shrink-0 text-[#FF7A8F]" />
      <div className="text-xs font-semibold text-[#F7F2EC]">
        <span className="text-[#FF7A8F]">Fellow Impostor Ally:</span>{' '}
        {allyNames.join(', ')}
      </div>
    </div>
  );
};
