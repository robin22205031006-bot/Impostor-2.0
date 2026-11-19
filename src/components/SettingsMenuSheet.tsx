import React from 'react';
import { BellRing, Clock, Volume2, X } from 'lucide-react';
import { GoldCtaButton, PlumTogglePill } from './ImpostorWizardComponents';

interface SettingsMenuSheetProps {
  isOpen: boolean;
  emergencyAlertSoundEnabled: boolean;
  soundEffectsEnabled: boolean;
  turnDurationSeconds: number;
  onToggleEmergencyAlertSound: (enabled: boolean) => void;
  onToggleSoundEffects: (enabled: boolean) => void;
  onTestEmergencySound: () => void;
  onSetTurnDuration: (seconds: number) => void;
  onClose: () => void;
}

const DURATION_PRESETS = [15, 30, 45, 60, 90, 120];

export const SettingsMenuSheet: React.FC<SettingsMenuSheetProps> = ({
  isOpen,
  emergencyAlertSoundEnabled,
  soundEffectsEnabled,
  turnDurationSeconds,
  onToggleEmergencyAlertSound,
  onToggleSoundEffects,
  onTestEmergencySound,
  onSetTurnDuration,
  onClose,
}) => {
  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-end justify-center bg-black/75 backdrop-blur-sm sm:items-center sm:p-4">
      <div className="w-full max-w-md rounded-t-3xl border border-[#423554] bg-[#241C2F] p-6 shadow-2xl sm:rounded-3xl">
        <div className="flex items-center justify-between">
          <div>
            <div className="font-mono-label text-[10px] font-bold uppercase tracking-[0.22em] text-[#F4CD6C]">
              AUDIO &amp; TIMER
            </div>
            <h2 className="font-display text-xl font-extrabold text-[#F7F2EC]">
              Game Settings
            </h2>
          </div>
          <button
            type="button"
            onClick={onClose}
            aria-label="Close settings"
            className="rounded-xl p-2 text-[#887B9B] hover:bg-[#2C2339] hover:text-[#F7F2EC]"
          >
            <X className="h-5 w-5" />
          </button>
        </div>

        <div className="mt-5 space-y-4">
          {/* Emergency Alert Sound */}
          <div className="rounded-2xl border border-[#423554] bg-[#1D1626] p-4">
            <div className="flex items-center justify-between gap-3">
              <div className="flex items-center gap-3">
                <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-[#341624] text-[#FF3E5B]">
                  <BellRing className="h-5 w-5" />
                </div>
                <div>
                  <div className="font-display text-sm font-bold text-[#F7F2EC]">
                    Emergency Alert Sound
                  </div>
                  <div className="text-xs text-[#887B9B]">
                    Dual-tone siren when a meeting is called
                  </div>
                </div>
              </div>
              <PlumTogglePill
                checked={emergencyAlertSoundEnabled}
                onToggle={onToggleEmergencyAlertSound}
                accentColor="#FF3E5B"
                ariaLabel="Toggle emergency alert sound"
              />
            </div>

            <button
              type="button"
              onClick={onTestEmergencySound}
              className="mt-3 inline-flex items-center gap-1.5 rounded-xl border border-[#423554] bg-[#2C2339] px-3 py-1.5 text-xs font-bold text-[#F4CD6C] hover:border-[#F4CD6C]/60"
            >
              <BellRing className="h-3.5 w-3.5" />
              <span>Test Alert</span>
            </button>
          </div>

          {/* All Sound Effects */}
          <div className="flex items-center justify-between gap-3 rounded-2xl border border-[#423554] bg-[#1D1626] p-4">
            <div className="flex items-center gap-3">
              <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-[#3A2F17] text-[#F4CD6C]">
                <Volume2 className="h-5 w-5" />
              </div>
              <div>
                <div className="font-display text-sm font-bold text-[#F7F2EC]">
                  All Sound Effects
                </div>
                <div className="text-xs text-[#887B9B]">
                  Role reveal, timer expiration &amp; verdict cues
                </div>
              </div>
            </div>
            <PlumTogglePill
              checked={soundEffectsEnabled}
              onToggle={onToggleSoundEffects}
              ariaLabel="Toggle all sound effects"
            />
          </div>

          {/* Discussion Timer Duration */}
          <div className="rounded-2xl border border-[#423554] bg-[#1D1626] p-4">
            <div className="flex items-center justify-between">
              <div className="flex items-center gap-2.5">
                <Clock className="h-4 w-4 text-[#F4CD6C]" />
                <span className="font-display text-sm font-bold text-[#F7F2EC]">
                  Discussion Timer Duration
                </span>
              </div>
              <span className="font-display text-base font-extrabold text-[#F4CD6C]">
                {turnDurationSeconds}s
              </span>
            </div>

            <div className="mt-3 flex items-center gap-2">
              <button
                type="button"
                onClick={() => onSetTurnDuration(turnDurationSeconds - 5)}
                className="rounded-xl border border-[#423554] bg-[#241C2F] px-3 py-1.5 text-xs font-extrabold text-[#F7F2EC] hover:border-[#F4CD6C]/50"
              >
                -5s
              </button>
              <div className="flex flex-1 flex-wrap gap-1.5">
                {DURATION_PRESETS.map((preset) => (
                  <button
                    key={preset}
                    type="button"
                    onClick={() => onSetTurnDuration(preset)}
                    className={`flex-1 rounded-xl border py-1.5 text-xs font-bold transition ${
                      turnDurationSeconds === preset
                        ? 'border-[#F4CD6C] bg-[#F4CD6C] text-[#1D1626]'
                        : 'border-[#423554] bg-[#241C2F] text-[#B7ABC8] hover:text-[#F7F2EC]'
                    }`}
                  >
                    {preset}s
                  </button>
                ))}
              </div>
              <button
                type="button"
                onClick={() => onSetTurnDuration(turnDurationSeconds + 5)}
                className="rounded-xl border border-[#423554] bg-[#241C2F] px-3 py-1.5 text-xs font-extrabold text-[#F7F2EC] hover:border-[#F4CD6C]/50"
              >
                +5s
              </button>
            </div>
          </div>
        </div>

        <div className="mt-6">
          <GoldCtaButton text="Done" onClick={onClose} />
        </div>
      </div>
    </div>
  );
};
