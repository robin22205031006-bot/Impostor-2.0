import React, { useEffect, useRef } from 'react';
import { PALETTE } from '../theme/palette';
import { TurnStatus } from '../types/game';

interface CircularCountdownTimerProps {
  remainingSeconds: number;
  totalSeconds: number;
  isPaused: boolean;
  isActive: boolean;
  size?: number;
  onTimeExpired?: () => void;
  className?: string;
}

export const CircularCountdownTimer: React.FC<CircularCountdownTimerProps> = ({
  remainingSeconds,
  totalSeconds,
  isPaused,
  isActive,
  size = 152,
  onTimeExpired,
  className = '',
}) => {
  const prevSecondsRef = useRef<number>(remainingSeconds);

  useEffect(() => {
    if (
      prevSecondsRef.current > 0 &&
      remainingSeconds === 0 &&
      onTimeExpired
    ) {
      onTimeExpired();
    }
    prevSecondsRef.current = remainingSeconds;
  }, [remainingSeconds, onTimeExpired]);

  const safeTotal = Math.max(1, totalSeconds);
  const progress = Math.max(0, Math.min(1, remainingSeconds / safeTotal));
  const isExpired = remainingSeconds <= 0 && !isActive;
  const isUrgent = remainingSeconds > 0 && remainingSeconds <= 10 && !isPaused;

  const accentColor =
    isExpired || remainingSeconds <= 5
      ? PALETTE.impostorAccent
      : remainingSeconds <= 15
      ? PALETTE.goldAccent
      : PALETTE.normalAccent;

  const statusText = isExpired
    ? 'EXPIRED'
    : isPaused
    ? 'PAUSED'
    : isUrgent
    ? 'HURRY'
    : 'LIVE';

  const mins = Math.floor(remainingSeconds / 60);
  const secs = remainingSeconds % 60;
  const formattedTime =
    mins > 0
      ? `${mins}:${String(secs).padStart(2, '0')}`
      : `${remainingSeconds}s`;

  const viewBoxSize = 200;
  const cx = 100;
  const cy = 100;
  const radius = 80;
  const circumference = 2 * Math.PI * radius;
  const dashOffset = circumference * (1 - progress);
  const tipAngleRad = (-90 + 360 * progress) * (Math.PI / 180);
  const tipX = cx + radius * Math.cos(tipAngleRad);
  const tipY = cy + radius * Math.sin(tipAngleRad);

  return (
    <div
      className={`relative inline-flex select-none items-center justify-center ${
        isUrgent ? 'animate-[timerUrgentPulse_1s_ease-in-out_infinite]' : ''
      } ${className}`}
      style={{ width: size, height: size }}
    >
      {/* Shockwave rings when expired */}
      {isExpired && (
        <>
          <span
            aria-hidden="true"
            className="pointer-events-none absolute inset-0 rounded-full border-2 border-[#FF3E5B] animate-[shockwaveExpand_1.6s_ease-out_infinite]"
          />
          <span
            aria-hidden="true"
            className="pointer-events-none absolute inset-0 rounded-full border border-[#FF7A8F] animate-[shockwaveExpand_1.6s_ease-out_0.7s_infinite]"
          />
        </>
      )}

      {/* Ambient halo behind timer */}
      <div
        aria-hidden="true"
        className="pointer-events-none absolute inset-3 rounded-full blur-xl"
        style={{
          background: `radial-gradient(circle, ${accentColor}38 0%, transparent 72%)`,
        }}
      />

      <svg
        width={size}
        height={size}
        viewBox={`0 0 ${viewBoxSize} ${viewBoxSize}`}
        className="relative z-10"
      >
        {/* 60 Radial Tick Marks */}
        {Array.from({ length: 60 }, (_, idx) => {
          const angle = (idx * 6 - 90) * (Math.PI / 180);
          const isMajor = idx % 5 === 0;
          const rOuter = 94;
          const rInner = isMajor ? 87 : 90;
          const activeTick = idx / 60 <= progress;
          return (
            <line
              key={idx}
              x1={cx + rInner * Math.cos(angle)}
              y1={cy + rInner * Math.sin(angle)}
              x2={cx + rOuter * Math.cos(angle)}
              y2={cy + rOuter * Math.sin(angle)}
              stroke={activeTick ? accentColor : '#423554'}
              strokeWidth={isMajor ? 2.2 : 1.2}
              strokeOpacity={activeTick ? 0.85 : 0.45}
              strokeLinecap="round"
            />
          );
        })}

        {/* Track Ring */}
        <circle
          cx={cx}
          cy={cy}
          r={radius}
          fill="#16101ECC"
          stroke="#2C2339"
          strokeWidth="10"
        />

        {/* Progress Arc */}
        <circle
          cx={cx}
          cy={cy}
          r={radius}
          fill="none"
          stroke={accentColor}
          strokeWidth="10"
          strokeLinecap="round"
          strokeDasharray={circumference}
          strokeDashoffset={dashOffset}
          transform={`rotate(-90 ${cx} ${cy})`}
          className="transition-[stroke-dashoffset,stroke] duration-300 ease-linear"
        />

        {/* Luminous Tip Orb */}
        {progress > 0.01 && (
          <circle
            cx={tipX}
            cy={tipY}
            r="6.5"
            fill="#F7F2EC"
            stroke={accentColor}
            strokeWidth="3"
          />
        )}
      </svg>

      {/* Center Readout */}
      <div className="pointer-events-none absolute inset-0 z-20 flex flex-col items-center justify-center text-center">
        <span
          className="font-display text-3xl font-black tracking-tight"
          style={{ color: accentColor }}
        >
          {formattedTime}
        </span>
        <span className="mt-0.5 font-mono-label text-[10px] font-extrabold uppercase tracking-[0.2em] text-[#B7ABC8]">
          {statusText}
        </span>
      </div>
    </div>
  );
};

interface TurnTimerBarProps {
  remainingSeconds: number;
  totalSeconds: number;
  turnStatus: TurnStatus;
  className?: string;
}

export const TurnTimerBar: React.FC<TurnTimerBarProps> = ({
  remainingSeconds,
  totalSeconds,
  turnStatus,
  className = '',
}) => {
  const safeTotal = Math.max(1, totalSeconds);
  const pct = Math.max(0, Math.min(100, (remainingSeconds / safeTotal) * 100));
  const barColor =
    turnStatus === TurnStatus.EXPIRED || remainingSeconds <= 5
      ? PALETTE.impostorAccent
      : remainingSeconds <= 15
      ? PALETTE.goldAccent
      : PALETTE.normalAccent;

  return (
    <div className={`w-full ${className}`}>
      <div className="h-2.5 w-full overflow-hidden rounded-full bg-[#16101E] ring-1 ring-[#423554]">
        <div
          className="h-full rounded-full transition-[width,background-color] duration-300 ease-linear"
          style={{
            width: `${pct}%`,
            backgroundColor: barColor,
          }}
        />
      </div>
    </div>
  );
};
