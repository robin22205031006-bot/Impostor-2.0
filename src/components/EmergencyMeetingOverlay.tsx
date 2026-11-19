import React from 'react';
import { AlertTriangle, Siren } from 'lucide-react';
import { EmergencyMeetingAlert } from '../types/game';

interface EmergencyMeetingOverlayProps {
  alert: EmergencyMeetingAlert;
  onDismissToVoting: () => void;
}

export const EmergencyMeetingOverlay: React.FC<
  EmergencyMeetingOverlayProps
> = ({ alert, onDismissToVoting }) => {
  if (!alert.isActive) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-[#16101E]/95 p-5 backdrop-blur-md">
      {/* Pulsing Crimson Siren Backdrop */}
      <div
        aria-hidden="true"
        className="pointer-events-none absolute inset-0 animate-[statusDotPulse_1.2s_ease-in-out_infinite]"
        style={{
          background:
            'radial-gradient(circle at center, rgba(255,62,91,0.28) 0%, rgba(255,62,91,0.06) 52%, transparent 78%)',
        }}
      />

      <div className="relative z-10 w-full max-w-md rounded-3xl border-2 border-[#FF3E5B]/80 bg-[#241C2F] p-6 text-center shadow-[0_0_50px_rgba(255,62,91,0.35)]">
        {/* Shockwave Siren Medallion */}
        <div className="relative mx-auto flex h-24 w-24 items-center justify-center">
          <span
            aria-hidden="true"
            className="absolute inset-0 rounded-full border-2 border-[#FF3E5B] animate-[shockwaveExpand_1.4s_ease-out_infinite]"
          />
          <span
            aria-hidden="true"
            className="absolute inset-0 rounded-full border border-[#FF7A8F] animate-[shockwaveExpand_1.4s_ease-out_0.5s_infinite]"
          />
          <div className="relative flex h-20 w-20 items-center justify-center rounded-full border-2 border-[#FF3E5B] bg-[#341624] text-[#FF3E5B]">
            <Siren className="h-10 w-10" />
          </div>
        </div>

        <div className="mt-4 inline-flex items-center gap-1.5 rounded-full border border-[#FF3E5B]/50 bg-[#341624] px-3 py-1 font-mono-label text-[10px] font-extrabold uppercase tracking-[0.22em] text-[#FF7A8F]">
          <AlertTriangle className="h-3.5 w-3.5" />
          <span>REPORT INITIATED</span>
        </div>

        <h2 className="mt-3 font-display text-3xl font-black tracking-tight text-[#F7F2EC]">
          EMERGENCY MEETING
        </h2>

        <p className="mt-2 text-sm font-semibold text-[#B7ABC8]">
          Called by{' '}
          <span className="font-extrabold text-[#F4CD6C]">
            {alert.reporterName || 'A Player'}
          </span>
        </p>

        {alert.reason && (
          <div className="mt-3 rounded-2xl border border-[#423554] bg-[#16101E] px-4 py-3 text-sm font-medium text-[#F7F2EC]">
            &ldquo;{alert.reason}&rdquo;
          </div>
        )}

        <div className="mt-5 flex items-center justify-center gap-2 font-mono-label text-xs font-bold text-[#FF7A8F]">
          <span>Proceeding to voting in</span>
          <span className="inline-flex h-7 min-w-[28px] items-center justify-center rounded-lg bg-[#341624] px-2 text-sm font-black text-[#F7F2EC]">
            {alert.countdownSeconds}s
          </span>
        </div>

        <button
          type="button"
          onClick={onDismissToVoting}
          className="mt-5 inline-flex h-12 w-full items-center justify-center rounded-2xl bg-[#FF3E5B] font-display text-base font-extrabold text-white shadow-[0_8px_22px_rgba(255,62,91,0.35)] transition hover:bg-[#FF5570] active:scale-[0.985]"
        >
          Proceed to Voting Now
        </button>
      </div>
    </div>
  );
};
