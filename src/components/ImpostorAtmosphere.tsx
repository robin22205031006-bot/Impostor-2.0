import React from 'react';
import { PALETTE } from '../theme/palette';

interface ImpostorAnimatedBackdropProps {
  primaryOrbColor?: string;
  secondaryOrbColor?: string;
  tertiaryOrbColor?: string;
  children?: React.ReactNode;
}

export const ImpostorAnimatedBackdrop: React.FC<ImpostorAnimatedBackdropProps> = ({
  primaryOrbColor = PALETTE.goldAccent,
  secondaryOrbColor = '#5E467A',
  tertiaryOrbColor = '#46335C',
  children,
}) => {
  return (
    <div className="relative min-h-screen w-full overflow-x-hidden bg-[#1D1626] text-[#F7F2EC]">
      {/* Warm Plum & Gold Ambient Canvas */}
      <div
        aria-hidden="true"
        className="pointer-events-none fixed inset-0 z-0 overflow-hidden"
      >
        <div
          className="absolute inset-0"
          style={{
            background:
              'linear-gradient(180deg, #241C2F 0%, #1D1626 52%, #16101E 100%)',
          }}
        />
        {/* Primary top-right ambient orb */}
        <div
          className="absolute -top-28 -right-24 h-[420px] w-[420px] rounded-full blur-3xl animate-[orbBreathe_9s_ease-in-out_infinite]"
          style={{
            background: `radial-gradient(circle, ${primaryOrbColor}29 0%, ${primaryOrbColor}08 48%, transparent 75%)`,
          }}
        />
        {/* Secondary bottom-left ambient orb */}
        <div
          className="absolute -bottom-24 -left-24 h-[460px] w-[460px] rounded-full blur-3xl animate-[orbBreathe_11s_ease-in-out_infinite_reverse]"
          style={{
            background: `radial-gradient(circle, ${secondaryOrbColor}30 0%, ${secondaryOrbColor}0A 50%, transparent 76%)`,
          }}
        />
        {/* Tertiary center-top subtle plum glow */}
        <div
          className="absolute left-1/2 top-[16%] h-[340px] w-[340px] -translate-x-1/2 rounded-full blur-3xl"
          style={{
            background: `radial-gradient(circle, ${tertiaryOrbColor}26 0%, transparent 72%)`,
          }}
        />
      </div>

      {/* Main Content */}
      <div className="relative z-10 min-h-screen w-full">{children}</div>
    </div>
  );
};

interface GlassSectionCardProps {
  accentColor?: string;
  className?: string;
  children: React.ReactNode;
}

export const GlassSectionCard: React.FC<GlassSectionCardProps> = ({
  accentColor = PALETTE.obsidianOutline,
  className = '',
  children,
}) => {
  const hasCustomAccent = accentColor !== PALETTE.obsidianOutline;
  return (
    <div
      className={`rounded-3xl border bg-[#241C2F]/95 backdrop-blur-md transition-colors ${className}`}
      style={{
        borderColor: hasCustomAccent ? `${accentColor}4D` : PALETTE.obsidianOutline,
      }}
    >
      {children}
    </div>
  );
};

interface PulsingStatusDotProps {
  color?: string;
  size?: number;
}

export const PulsingStatusDot: React.FC<PulsingStatusDotProps> = ({
  color = PALETTE.goldAccent,
  size = 10,
}) => {
  return (
    <span
      className="inline-block shrink-0 rounded-full animate-[statusDotPulse_2.2s_ease-in-out_infinite]"
      style={{
        width: size,
        height: size,
        backgroundColor: color,
        boxShadow: `0 0 10px ${color}80`,
      }}
    />
  );
};
