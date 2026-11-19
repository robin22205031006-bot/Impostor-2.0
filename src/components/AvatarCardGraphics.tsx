import React from 'react';
import {
  AVATAR_MOTIF_NAMES,
  AVATAR_POSE_META,
  imageForPose,
} from '../data/avatarCatalog';
import {
  AvatarCharacter,
  AvatarGraphicMotif,
  AvatarPose,
} from '../types/game';

interface AvatarMotifBackdropProps {
  motif: AvatarGraphicMotif;
  accent: string;
  glow: string;
}

const AvatarMotifBackdrop: React.FC<AvatarMotifBackdropProps> = ({
  motif,
  accent,
  glow,
}) => {
  switch (motif) {
    case AvatarGraphicMotif.INK_WASH:
      return (
        <svg viewBox="0 0 200 260" className="h-full w-full" preserveAspectRatio="none" aria-hidden="true">
          <circle cx="156" cy="52" r="48" fill={accent} fillOpacity="0.16" />
          <path
            d="M15 210 Q75 150 135 180 T195 110"
            stroke={glow}
            strokeWidth="3.5"
            strokeOpacity="0.28"
            fill="none"
          />
          <path
            d="M0 165 Q65 110 120 135 T200 72"
            stroke={accent}
            strokeWidth="2.5"
            strokeOpacity="0.24"
            fill="none"
          />
        </svg>
      );
    case AvatarGraphicMotif.EMERALD_RUNES:
      return (
        <svg viewBox="0 0 200 260" className="h-full w-full" preserveAspectRatio="none" aria-hidden="true">
          <polygon
            points="100,26 168,125 100,224 32,125"
            stroke={accent}
            strokeWidth="2.5"
            strokeOpacity="0.34"
            fill={accent}
            fillOpacity="0.08"
          />
          <circle cx="100" cy="125" r="46" stroke={glow} strokeWidth="2" strokeDasharray="6 6" strokeOpacity="0.35" fill="none" />
        </svg>
      );
    case AvatarGraphicMotif.CRIMSON_EMBERS:
      return (
        <svg viewBox="0 0 200 260" className="h-full w-full" preserveAspectRatio="none" aria-hidden="true">
          <circle cx="42" cy="198" r="4" fill={glow} fillOpacity="0.65" />
          <circle cx="152" cy="166" r="5" fill={accent} fillOpacity="0.6" />
          <circle cx="118" cy="70" r="3.5" fill={glow} fillOpacity="0.6" />
          <circle cx="64" cy="95" r="3" fill={accent} fillOpacity="0.55" />
          <path d="M18 245 L178 40" stroke={accent} strokeWidth="3" strokeOpacity="0.22" />
        </svg>
      );
    case AvatarGraphicMotif.CYBER_HUD:
      return (
        <svg viewBox="0 0 200 260" className="h-full w-full" preserveAspectRatio="none" aria-hidden="true">
          <path d="M20 44 H180 M20 210 H180" stroke={accent} strokeWidth="1.5" strokeOpacity="0.25" strokeDasharray="4 6" />
          <circle cx="100" cy="120" r="64" stroke={glow} strokeWidth="2" strokeOpacity="0.28" strokeDasharray="14 8" fill="none" />
          <path d="M22 24 H48 M22 24 V50 M178 24 H152 M178 24 V50" stroke={glow} strokeWidth="2.5" strokeOpacity="0.45" fill="none" />
        </svg>
      );
    case AvatarGraphicMotif.LUNAR_RIPPLES:
      return (
        <svg viewBox="0 0 200 260" className="h-full w-full" preserveAspectRatio="none" aria-hidden="true">
          <circle cx="136" cy="64" r="28" fill={glow} fillOpacity="0.24" />
          <circle cx="136" cy="64" r="54" stroke={accent} strokeWidth="1.5" strokeOpacity="0.28" fill="none" />
          <circle cx="136" cy="64" r="82" stroke={glow} strokeWidth="1.5" strokeOpacity="0.18" fill="none" />
        </svg>
      );
    case AvatarGraphicMotif.SOLAR_RAYS:
      return (
        <svg viewBox="0 0 200 260" className="h-full w-full" preserveAspectRatio="none" aria-hidden="true">
          <circle cx="100" cy="108" r="42" fill={accent} fillOpacity="0.18" />
          <path
            d="M100 18 V52 M100 164 V198 M24 108 H58 M142 108 H176 M46 54 L70 78 M130 138 L154 162 M154 54 L130 78 M70 138 L46 162"
            stroke={glow}
            strokeWidth="2.5"
            strokeOpacity="0.3"
            strokeLinecap="round"
          />
        </svg>
      );
    case AvatarGraphicMotif.ARCANE_TIDE:
      return (
        <svg viewBox="0 0 200 260" className="h-full w-full" preserveAspectRatio="none" aria-hidden="true">
          <path d="M-10 170 Q50 135 100 165 T210 155" stroke={glow} strokeWidth="2.5" strokeOpacity="0.32" fill="none" />
          <path d="M-10 198 Q50 165 100 195 T210 185" stroke={accent} strokeWidth="2.5" strokeOpacity="0.26" fill="none" />
          <circle cx="62" cy="74" r="28" stroke={glow} strokeWidth="2" strokeOpacity="0.25" fill="none" />
        </svg>
      );
    case AvatarGraphicMotif.BLADE_SLASH:
      return (
        <svg viewBox="0 0 200 260" className="h-full w-full" preserveAspectRatio="none" aria-hidden="true">
          <path d="M18 232 L188 28" stroke={glow} strokeWidth="4" strokeOpacity="0.32" strokeLinecap="round" />
          <path d="M44 242 L192 78" stroke={accent} strokeWidth="2.5" strokeOpacity="0.25" strokeLinecap="round" />
        </svg>
      );
    case AvatarGraphicMotif.FEATHER_PLUME:
      return (
        <svg viewBox="0 0 200 260" className="h-full w-full" preserveAspectRatio="none" aria-hidden="true">
          <ellipse cx="148" cy="70" rx="22" ry="40" transform="rotate(24 148 70)" stroke={glow} strokeWidth="2" strokeOpacity="0.32" fill={accent} fillOpacity="0.1" />
          <ellipse cx="62" cy="96" rx="18" ry="34" transform="rotate(-18 62 96)" stroke={accent} strokeWidth="2" strokeOpacity="0.28" fill="none" />
        </svg>
      );
    case AvatarGraphicMotif.FOREST_WIND:
      return (
        <svg viewBox="0 0 200 260" className="h-full w-full" preserveAspectRatio="none" aria-hidden="true">
          <path d="M10 92 Q90 64 175 88" stroke={glow} strokeWidth="2.5" strokeOpacity="0.3" strokeLinecap="round" fill="none" />
          <path d="M25 140 Q105 112 190 136" stroke={accent} strokeWidth="2" strokeOpacity="0.28" strokeLinecap="round" fill="none" />
          <circle cx="152" cy="60" r="6" fill={glow} fillOpacity="0.3" />
        </svg>
      );
  }
};

interface CharacterAvatarThumbnailProps {
  character: AvatarCharacter;
  pose?: AvatarPose;
  size?: number;
  borderColor?: string;
  showBadgeDot?: boolean;
  className?: string;
}

export const CharacterAvatarThumbnail: React.FC<CharacterAvatarThumbnailProps> = ({
  character,
  pose = AvatarPose.ORIGINAL,
  size = 52,
  borderColor,
  showBadgeDot = false,
  className = '',
}) => {
  const effectiveBorder = borderColor ?? character.accentColor;
  const imgSrc = imageForPose(character, pose);
  const posX = ((character.bustBiasX + 1) / 2) * 100;
  const posY = ((character.bustBiasY + 1) / 2) * 100;
  const isHex = pose === AvatarPose.CREST_AURA;

  return (
    <div
      className={`relative shrink-0 select-none ${className}`}
      style={{ width: size, height: size }}
    >
      <div
        className={`relative h-full w-full overflow-hidden border-2 ${
          isHex ? 'rounded-2xl' : 'rounded-full'
        }`}
        style={{
          borderColor: effectiveBorder,
          background: `radial-gradient(circle at 50% 35%, ${character.accentColor}40 0%, ${character.surfaceTint} 75%, #120D18 100%)`,
          boxShadow: `0 0 14px ${character.accentColor}30`,
          clipPath: isHex
            ? 'polygon(50% 2%, 95% 26%, 95% 74%, 50% 98%, 5% 74%, 5% 26%)'
            : undefined,
        }}
      >
        <img
          src={imgSrc}
          alt={character.name}
          draggable={false}
          className="h-full w-full object-cover"
          style={{
            objectPosition: `${posX}% ${posY}%`,
            transform: `scale(${character.bustZoom})`,
            transformOrigin: `${posX}% ${posY}%`,
          }}
        />
        {pose === AvatarPose.TACTICAL_BUST && (
          <div
            aria-hidden="true"
            className="pointer-events-none absolute inset-1 rounded-full border border-dashed"
            style={{ borderColor: `${character.glowColor}80` }}
          />
        )}
      </div>

      {showBadgeDot && (
        <span
          className="absolute -bottom-0.5 -right-0.5 h-3.5 w-3.5 rounded-full border-2 border-[#1D1626]"
          style={{ backgroundColor: character.accentColor }}
        />
      )}
    </div>
  );
};

interface CharacterAvatarCardProps {
  character: AvatarCharacter;
  pose?: AvatarPose;
  selected?: boolean;
  featured?: boolean;
  onClick?: () => void;
  className?: string;
}

export const CharacterAvatarCard: React.FC<CharacterAvatarCardProps> = ({
  character,
  pose = AvatarPose.ORIGINAL,
  selected = false,
  featured = false,
  onClick,
  className = '',
}) => {
  const poseMeta = AVATAR_POSE_META[pose];
  const imgSrc = imageForPose(character, pose);

  return (
    <div
      onClick={onClick}
      role={onClick ? 'button' : undefined}
      tabIndex={onClick ? 0 : undefined}
      onKeyDown={
        onClick
          ? (e) => {
              if (e.key === 'Enter' || e.key === ' ') {
                e.preventDefault();
                onClick();
              }
            }
          : undefined
      }
      className={`group relative overflow-hidden rounded-3xl border transition-all ${
        onClick ? 'cursor-pointer active:scale-[0.985]' : ''
      } ${featured ? 'min-h-[260px]' : 'h-[235px]'} ${className}`}
      style={{
        borderColor: selected ? character.accentColor : '#423554',
        borderWidth: selected ? '2px' : '1px',
        background: `linear-gradient(180deg, ${character.surfaceTint} 0%, #1D1626 100%)`,
        boxShadow: selected
          ? `0 0 24px ${character.accentColor}40`
          : '0 6px 18px rgba(0,0,0,0.35)',
      }}
    >
      {/* Dynamic SVG Motif Backdrop */}
      <div className="pointer-events-none absolute inset-0">
        <AvatarMotifBackdrop
          motif={character.motif}
          accent={character.accentColor}
          glow={character.glowColor}
        />
      </div>

      {/* Pose Aura Behind Sprite */}
      <div
        aria-hidden="true"
        className="pointer-events-none absolute left-1/2 top-1/2 h-40 w-40 -translate-x-1/2 -translate-y-1/2 rounded-full blur-2xl"
        style={{
          background: `radial-gradient(circle, ${character.accentColor}45 0%, transparent 72%)`,
        }}
      />

      {/* Top Metadata Badges */}
      <div className="relative z-20 flex items-center justify-between px-3 pt-3">
        <span
          className="rounded-lg border px-2 py-0.5 font-mono-label text-[10px] font-bold tracking-wider"
          style={{
            backgroundColor: '#16101ECC',
            borderColor: `${character.accentColor}66`,
            color: character.glowColor,
          }}
        >
          {character.code}
        </span>
        <span
          className="rounded-lg border px-2 py-0.5 font-mono-label text-[10px] font-bold tracking-wider"
          style={{
            backgroundColor: `${character.accentColor}26`,
            borderColor: `${character.accentColor}77`,
            color: character.glowColor,
          }}
        >
          {poseMeta.badgeCode}
        </span>
      </div>

      {/* Full Cutout Sprite */}
      <div className="relative z-10 flex h-[155px] w-full items-center justify-center px-2 pt-1">
        <img
          src={imgSrc}
          alt={`${character.name} — ${poseMeta.label}`}
          draggable={false}
          className="max-h-full max-w-full object-contain drop-shadow-[0_10px_18px_rgba(0,0,0,0.55)] transition-transform duration-300 group-hover:scale-105"
        />
      </div>

      {/* Bottom Nameplate Scrim */}
      <div className="absolute inset-x-0 bottom-0 z-20 bg-gradient-to-t from-[#16101E] via-[#16101EDB] to-transparent px-3.5 pb-3 pt-6">
        <div className="flex items-end justify-between gap-2">
          <div className="min-w-0">
            <div className="truncate font-display text-base font-extrabold text-[#F7F2EC]">
              {character.name}
            </div>
            <div className="truncate text-[11px] font-semibold text-[#B7ABC8]">
              {character.title}
            </div>
          </div>
          {featured && (
            <span className="shrink-0 rounded-full bg-[#2C2339] px-2.5 py-0.5 text-[10px] font-bold text-[#B7ABC8]">
              {AVATAR_MOTIF_NAMES[character.motif]}
            </span>
          )}
        </div>
      </div>
    </div>
  );
};
