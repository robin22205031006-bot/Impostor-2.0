import React, { useEffect, useMemo, useState } from 'react';
import { Check, Sparkles, X } from 'lucide-react';
import {
  AVATAR_FILTER_LABELS,
  AVATAR_POSE_META,
  AVATAR_POSES_ORDER,
  AvatarCatalog,
} from '../data/avatarCatalog';
import {
  AvatarCategoryFilter,
  AvatarPose,
  LocalPlayer,
} from '../types/game';
import {
  CharacterAvatarCard,
  CharacterAvatarThumbnail,
} from './AvatarCardGraphics';

interface AvatarPickerSheetProps {
  isOpen: boolean;
  players: LocalPlayer[];
  initialPlayerId: string | null;
  onSelectAvatar: (playerId: string, avatarId: string, pose: AvatarPose) => void;
  onClose: () => void;
}

const FILTER_ORDER: AvatarCategoryFilter[] = [
  AvatarCategoryFilter.ALL,
  AvatarCategoryFilter.BLADES,
  AvatarCategoryFilter.MYSTIC,
  AvatarCategoryFilter.VANGUARD,
];

export const AvatarPickerSheet: React.FC<AvatarPickerSheetProps> = ({
  isOpen,
  players,
  initialPlayerId,
  onSelectAvatar,
  onClose,
}) => {
  const [activePlayerId, setActivePlayerId] = useState<string>(
    initialPlayerId ?? players[0]?.id ?? ''
  );
  const [filter, setFilter] = useState<AvatarCategoryFilter>(
    AvatarCategoryFilter.ALL
  );
  const [previewAvatarId, setPreviewAvatarId] = useState<string>('avatar_01');
  const [previewPose, setPreviewPose] = useState<AvatarPose>(
    AvatarPose.ORIGINAL
  );

  const activePlayer = useMemo(
    () => players.find((p) => p.id === activePlayerId) ?? players[0] ?? null,
    [players, activePlayerId]
  );

  useEffect(() => {
    if (!isOpen) return;
    const target =
      players.find((p) => p.id === initialPlayerId) ?? players[0] ?? null;
    if (target) {
      setActivePlayerId(target.id);
      setPreviewAvatarId(target.avatarId);
      setPreviewPose(target.avatarPose);
    }
  }, [isOpen, initialPlayerId, players]);

  const handleSwitchPlayer = (player: LocalPlayer) => {
    setActivePlayerId(player.id);
    setPreviewAvatarId(player.avatarId);
    setPreviewPose(player.avatarPose);
  };

  const filteredCharacters = useMemo(() => {
    if (filter === AvatarCategoryFilter.ALL) return AvatarCatalog.all;
    return AvatarCatalog.all.filter((c) => c.filterGroup === filter);
  }, [filter]);

  if (!isOpen || !activePlayer) return null;

  const previewCharacter = AvatarCatalog.findById(previewAvatarId);
  const isAlreadyEquipped =
    activePlayer.avatarId === previewCharacter.id &&
    activePlayer.avatarPose === previewPose;

  const handleEquip = () => {
    onSelectAvatar(activePlayer.id, previewCharacter.id, previewPose);
  };

  return (
    <div className="fixed inset-0 z-50 flex items-end justify-center bg-black/75 backdrop-blur-sm sm:items-center sm:p-4">
      <div className="flex max-h-[92vh] w-full max-w-2xl flex-col overflow-hidden rounded-t-3xl border border-[#423554] bg-[#1D1626] shadow-2xl sm:rounded-3xl">
        {/* Top Sheet Header */}
        <div className="flex items-center justify-between border-b border-[#423554] bg-[#241C2F] px-5 py-4">
          <div>
            <div className="font-mono-label text-[10px] font-bold uppercase tracking-[0.22em] text-[#F4CD6C]">
              CHARACTER AVATAR DECK
            </div>
            <h2 className="font-display text-lg font-extrabold text-[#F7F2EC]">
              Equip Hero &amp; Stance
            </h2>
          </div>
          <button
            type="button"
            onClick={onClose}
            aria-label="Close avatar deck"
            className="flex h-10 w-10 items-center justify-center rounded-2xl border border-[#423554] bg-[#2C2339] text-[#B7ABC8] hover:text-[#F7F2EC]"
          >
            <X className="h-5 w-5" />
          </button>
        </div>

        <div className="flex-1 space-y-5 overflow-y-auto px-5 py-4">
          {/* Player Selector Chips */}
          {players.length > 0 && (
            <div className="flex gap-2 overflow-x-auto pb-1">
              {players.map((player) => {
                const char = AvatarCatalog.findById(player.avatarId);
                const selected = player.id === activePlayer.id;
                return (
                  <button
                    key={player.id}
                    type="button"
                    onClick={() => handleSwitchPlayer(player)}
                    className={`flex shrink-0 items-center gap-2 rounded-2xl border px-3 py-1.5 text-xs font-bold transition ${
                      selected
                        ? 'border-[#F4CD6C] bg-[#3A2F17] text-[#FFDE8A]'
                        : 'border-[#423554] bg-[#241C2F] text-[#B7ABC8] hover:border-[#F4CD6C]/40'
                    }`}
                  >
                    <CharacterAvatarThumbnail
                      character={char}
                      pose={player.avatarPose}
                      size={26}
                    />
                    <span className="max-w-[96px] truncate">{player.name}</span>
                  </button>
                );
              })}
            </div>
          )}

          {/* Featured Preview + Pose Selector */}
          <div className="grid grid-cols-1 gap-4 rounded-3xl border border-[#423554] bg-[#241C2F] p-4 sm:grid-cols-12">
            <div className="sm:col-span-5">
              <CharacterAvatarCard
                character={previewCharacter}
                pose={previewPose}
                selected
                featured
              />
            </div>

            <div className="flex flex-col justify-between gap-3 sm:col-span-7">
              <div>
                <div className="flex items-center justify-between gap-2">
                  <span
                    className="rounded-lg px-2 py-0.5 font-mono-label text-[10px] font-bold uppercase tracking-wider"
                    style={{
                      backgroundColor: `${previewCharacter.accentColor}28`,
                      color: previewCharacter.glowColor,
                    }}
                  >
                    {previewCharacter.archetype}
                  </span>
                  <span className="font-mono-label text-[11px] text-[#887B9B]">
                    {previewCharacter.code}
                  </span>
                </div>
                <h3 className="mt-1.5 font-display text-2xl font-extrabold text-[#F7F2EC]">
                  {previewCharacter.name}
                </h3>
                <p className="text-xs font-medium text-[#B7ABC8]">
                  {previewCharacter.title} —{' '}
                  {AVATAR_POSE_META[previewPose].description}
                </p>

                <div className="mt-3">
                  <div className="font-mono-label text-[10px] font-bold uppercase tracking-[0.18em] text-[#887B9B]">
                    SELECT STANCE POSE
                  </div>
                  <div className="mt-2 grid grid-cols-2 gap-2">
                    {AVATAR_POSES_ORDER.map((pose) => {
                      const meta = AVATAR_POSE_META[pose];
                      const active = pose === previewPose;
                      return (
                        <button
                          key={pose}
                          type="button"
                          onClick={() => setPreviewPose(pose)}
                          className={`flex items-center justify-between rounded-xl border px-3 py-2 text-left text-xs font-bold transition ${
                            active
                              ? 'border-[#F4CD6C] bg-[#3A2F17] text-[#FFDE8A]'
                              : 'border-[#423554] bg-[#16101E] text-[#B7ABC8] hover:border-[#F4CD6C]/45'
                          }`}
                        >
                          <span className="truncate">{meta.label}</span>
                          <span className="font-mono-label text-[10px] opacity-80">
                            {meta.badgeCode}
                          </span>
                        </button>
                      );
                    })}
                  </div>
                </div>
              </div>

              <button
                type="button"
                onClick={handleEquip}
                className={`inline-flex h-11 w-full items-center justify-center gap-2 rounded-2xl font-display text-sm font-extrabold transition ${
                  isAlreadyEquipped
                    ? 'border border-[#1AE682]/60 bg-[#122C24] text-[#5CFFB0]'
                    : 'bg-[#F4CD6C] text-[#1D1626] hover:bg-[#FFDE8A]'
                }`}
              >
                {isAlreadyEquipped ? (
                  <>
                    <Check className="h-4 w-4" />
                    <span>Equipped on {activePlayer.name}</span>
                  </>
                ) : (
                  <>
                    <Sparkles className="h-4 w-4" />
                    <span>Equip on {activePlayer.name}</span>
                  </>
                )}
              </button>
            </div>
          </div>

          {/* Category Filter Chips */}
          <div className="flex flex-wrap gap-2">
            {FILTER_ORDER.map((cat) => {
              const selected = cat === filter;
              return (
                <button
                  key={cat}
                  type="button"
                  onClick={() => setFilter(cat)}
                  className={`rounded-2xl border px-3.5 py-1.5 text-xs font-bold transition ${
                    selected
                      ? 'border-[#F4CD6C] bg-[#F4CD6C] text-[#1D1626]'
                      : 'border-[#423554] bg-[#241C2F] text-[#B7ABC8] hover:text-[#F7F2EC]'
                  }`}
                >
                  {AVATAR_FILTER_LABELS[cat]}
                </button>
              );
            })}
          </div>

          {/* 2-Column 22-Hero Grid */}
          <div className="grid grid-cols-2 gap-3 pb-2">
            {filteredCharacters.map((char) => {
              const isSelectedCard = char.id === previewCharacter.id;
              const isEquippedOnActive = char.id === activePlayer.avatarId;
              const cardPose = isSelectedCard ? previewPose : AvatarPose.ORIGINAL;
              return (
                <div key={char.id} className="relative">
                  <CharacterAvatarCard
                    character={char}
                    pose={cardPose}
                    selected={isSelectedCard}
                    onClick={() => {
                      setPreviewAvatarId(char.id);
                      onSelectAvatar(activePlayer.id, char.id, previewPose);
                    }}
                  />
                  {isEquippedOnActive && (
                    <span className="pointer-events-none absolute right-2.5 top-11 z-30 rounded-full bg-[#1AE682] px-2 py-0.5 font-mono-label text-[9px] font-extrabold uppercase text-[#1D1626] shadow">
                      Equipped
                    </span>
                  )}
                </div>
              );
            })}
          </div>
        </div>
      </div>
    </div>
  );
};
