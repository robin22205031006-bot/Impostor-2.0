import React from 'react';
import {
  getRoleVisualStyle,
  ROLE_IDENTITY_META,
} from '../theme/palette';
import { GameRoleIdentity } from '../types/game';

interface RoleEmblemVectorProps {
  identity: GameRoleIdentity;
  color: string;
  size: number;
}

const RoleEmblemVector: React.FC<RoleEmblemVectorProps> = ({
  identity,
  color,
  size,
}) => {
  switch (identity) {
    case GameRoleIdentity.AGENT:
      return (
        <svg
          width={size}
          height={size}
          viewBox="0 0 100 100"
          fill="none"
          aria-hidden="true"
        >
          <path
            d="M50 10 L82 24 V50 C82 70 67 84 50 92 C33 84 18 70 18 50 V24 Z"
            stroke={color}
            strokeWidth="7"
            strokeLinejoin="round"
            fill={`${color}1E`}
          />
          <path
            d="M34 52 L50 37 L66 52 M34 64 L50 49 L66 64"
            stroke={color}
            strokeWidth="6"
            strokeLinecap="round"
            strokeLinejoin="round"
          />
        </svg>
      );
    case GameRoleIdentity.IMPOSTOR:
      return (
        <svg
          width={size}
          height={size}
          viewBox="0 0 100 100"
          fill="none"
          aria-hidden="true"
        >
          <path
            d="M22 30 L34 15 L50 26 L66 15 L78 30 L85 55 L50 86 L15 55 Z"
            stroke={color}
            strokeWidth="6.5"
            strokeLinejoin="round"
            fill={`${color}24`}
          />
          <path
            d="M28 45 L44 50 L36 56 Z M72 45 L56 50 L64 56 Z"
            fill={color}
          />
          <path
            d="M50 34 V72"
            stroke={color}
            strokeWidth="4.5"
            strokeLinecap="round"
            opacity="0.7"
          />
        </svg>
      );
    case GameRoleIdentity.AGENT_KILLER:
      return (
        <svg
          width={size}
          height={size}
          viewBox="0 0 100 100"
          fill="none"
          aria-hidden="true"
        >
          <circle
            cx="50"
            cy="50"
            r="34"
            stroke={color}
            strokeWidth="5.5"
            fill={`${color}1C`}
          />
          <path
            d="M50 8 V26 M50 74 V92 M8 50 H26 M74 50 H92"
            stroke={color}
            strokeWidth="6"
            strokeLinecap="round"
          />
          <path
            d="M35 35 L65 65 M65 35 L35 65"
            stroke={color}
            strokeWidth="6.5"
            strokeLinecap="round"
          />
          <circle cx="50" cy="50" r="5" fill={color} />
        </svg>
      );
    case GameRoleIdentity.SAVIOR_AGENT:
      return (
        <svg
          width={size}
          height={size}
          viewBox="0 0 100 100"
          fill="none"
          aria-hidden="true"
        >
          <path
            d="M50 12 L80 28 V60 L50 88 L20 60 V28 Z"
            stroke={color}
            strokeWidth="6.5"
            strokeLinejoin="round"
            fill={`${color}20`}
          />
          <path
            d="M50 32 V68 M32 50 H68"
            stroke={color}
            strokeWidth="8"
            strokeLinecap="round"
          />
        </svg>
      );
    case GameRoleIdentity.AGENT_DIRECTOR:
      return (
        <svg
          width={size}
          height={size}
          viewBox="0 0 100 100"
          fill="none"
          aria-hidden="true"
        >
          <path
            d="M16 68 L22 30 L39 48 L50 22 L61 48 L78 30 L84 68 Z"
            stroke={color}
            strokeWidth="6.5"
            strokeLinejoin="round"
            fill={`${color}24`}
          />
          <path
            d="M22 79 H78"
            stroke={color}
            strokeWidth="7"
            strokeLinecap="round"
          />
          <circle cx="50" cy="18" r="4.5" fill={color} />
        </svg>
      );
  }
};

interface RoleEmblemBadgeProps {
  identity: GameRoleIdentity;
  size?: number;
  animateGlow?: boolean;
  className?: string;
}

export const RoleEmblemBadge: React.FC<RoleEmblemBadgeProps> = ({
  identity,
  size = 56,
  animateGlow = true,
  className = '',
}) => {
  const style = getRoleVisualStyle(identity);
  const meta = ROLE_IDENTITY_META[identity];

  return (
    <div
      role="img"
      aria-label={meta.emblemDescription}
      className={`relative inline-flex shrink-0 items-center justify-center rounded-full ${className}`}
      style={{ width: size, height: size }}
    >
      {/* Ambient glow */}
      <div
        className={`absolute inset-0 rounded-full blur-md ${
          animateGlow ? 'animate-[statusDotPulse_2.8s_ease-in-out_infinite]' : ''
        }`}
        style={{
          background: `radial-gradient(circle, ${style.glow}45 0%, ${style.accent}18 55%, transparent 78%)`,
        }}
      />
      {/* Orbiting sweep ring */}
      {animateGlow && (
        <svg
          width={size}
          height={size}
          viewBox="0 0 100 100"
          className="absolute inset-0 animate-[spinSlow_9s_linear_infinite]"
          aria-hidden="true"
        >
          <circle
            cx="50"
            cy="50"
            r="46"
            fill="none"
            stroke={style.glow}
            strokeWidth="3"
            strokeDasharray="65 225"
            strokeLinecap="round"
            opacity="0.7"
          />
        </svg>
      )}
      {/* Medallion body */}
      <div
        className="relative flex h-[78%] w-[78%] items-center justify-center rounded-full border-[1.5px]"
        style={{
          background: `linear-gradient(135deg, ${style.deepSurface} 0%, #16101E 100%)`,
          borderColor: `${style.accent}A6`,
        }}
      >
        <RoleEmblemVector
          identity={identity}
          color={style.accent}
          size={Math.round(size * 0.48)}
        />
      </div>
    </div>
  );
};

interface RoleEmblemChipProps {
  identity: GameRoleIdentity;
  useShortLabel?: boolean;
  className?: string;
}

export const RoleEmblemChip: React.FC<RoleEmblemChipProps> = ({
  identity,
  useShortLabel = false,
  className = '',
}) => {
  const style = getRoleVisualStyle(identity);
  const meta = ROLE_IDENTITY_META[identity];
  const label = useShortLabel ? meta.shortLabel : meta.displayTitle;

  return (
    <span
      className={`inline-flex items-center gap-1.5 rounded-full border px-2.5 py-1 text-[11px] font-extrabold tracking-wider ${className}`}
      style={{
        backgroundColor: `${style.deepSurface}E6`,
        borderColor: `${style.accent}99`,
        color: style.glow,
      }}
    >
      <RoleEmblemVector identity={identity} color={style.accent} size={15} />
      <span>{label}</span>
    </span>
  );
};
