import React from 'react';

interface RoundCelebrationFxProps {
  outcome: 'agents' | 'impostor';
}

interface ConfettiPiece {
  leftPct: number;
  widthPx: number;
  heightPx: number;
  color: string;
  delayMs: number;
  durationMs: number;
  driftPx: number;
  rounded: boolean;
}

const CONFETTI_COLORS = [
  '#1AE682',
  '#5CFFB0',
  '#F4CD6C',
  '#FFDE8A',
  '#2CCBFF',
  '#FF78B4',
];

const CONFETTI_PIECES: ConfettiPiece[] = Array.from({ length: 34 }, (_, idx) => {
  const leftPct = ((idx * 29 + 7) % 96) + 2;
  const widthPx = 7 + (idx % 4) * 2;
  const heightPx = idx % 3 === 0 ? widthPx : 12 + (idx % 4) * 3;
  const color = CONFETTI_COLORS[idx % CONFETTI_COLORS.length];
  const delayMs = (idx % 9) * 95;
  const durationMs = 2100 + (idx % 6) * 280;
  const driftPx = (idx % 2 === 0 ? 1 : -1) * (18 + (idx % 5) * 14);
  return {
    leftPct,
    widthPx,
    heightPx,
    color,
    delayMs,
    durationMs,
    driftPx,
    rounded: idx % 5 === 0,
  };
});

interface EmberParticle {
  leftPct: number;
  sizePx: number;
  color: string;
  delayMs: number;
  durationMs: number;
  driftPx: number;
}

const EMBER_COLORS = ['#FF3E5B', '#FF2985', '#FF7A8F', '#FFB800'];

const EMBER_PARTICLES: EmberParticle[] = Array.from({ length: 18 }, (_, idx) => ({
  leftPct: ((idx * 31 + 11) % 92) + 4,
  sizePx: 6 + (idx % 4) * 2,
  color: EMBER_COLORS[idx % EMBER_COLORS.length],
  delayMs: (idx % 6) * 110,
  durationMs: 1700 + (idx % 5) * 260,
  driftPx: (idx % 2 === 0 ? 1 : -1) * (14 + (idx % 4) * 10),
}));

export const RoundCelebrationFx: React.FC<RoundCelebrationFxProps> = ({
  outcome,
}) => {
  if (outcome === 'agents') {
    return (
      <div
        aria-hidden="true"
        className="pointer-events-none fixed inset-0 z-[60] overflow-hidden"
      >
        {CONFETTI_PIECES.map((piece, index) => (
          <span
            key={index}
            style={
              {
                position: 'absolute',
                top: '-4vh',
                left: `${piece.leftPct}%`,
                width: `${piece.widthPx}px`,
                height: `${piece.heightPx}px`,
                backgroundColor: piece.color,
                borderRadius: piece.rounded ? '9999px' : '2px',
                boxShadow: `0 0 10px ${piece.color}66`,
                animation: `confettiFall ${piece.durationMs}ms cubic-bezier(0.22, 0.61, 0.36, 1) ${piece.delayMs}ms both`,
                '--confetti-drift': `${piece.driftPx}px`,
              } as React.CSSProperties
            }
          />
        ))}
      </div>
    );
  }

  return (
    <div
      aria-hidden="true"
      className="pointer-events-none fixed inset-0 z-[60] overflow-hidden"
    >
      {/* Crimson tension vignette flash */}
      <div
        className="absolute inset-0"
        style={{
          background:
            'radial-gradient(circle at center, transparent 35%, rgba(255, 62, 91, 0.45) 100%)',
          animation: 'crimsonFlash 900ms ease-out forwards',
        }}
      />

      {/* Rising crimson embers */}
      {EMBER_PARTICLES.map((ember, index) => (
        <span
          key={index}
          style={
            {
              position: 'absolute',
              bottom: '-4vh',
              left: `${ember.leftPct}%`,
              width: `${ember.sizePx}px`,
              height: `${ember.sizePx}px`,
              backgroundColor: ember.color,
              borderRadius: '9999px',
              boxShadow: `0 0 12px ${ember.color}`,
              animation: `emberRise ${ember.durationMs}ms ease-out ${ember.delayMs}ms both`,
              '--ember-drift': `${ember.driftPx}px`,
            } as React.CSSProperties
          }
        />
      ))}
    </div>
  );
};
