import React, { useEffect, useState } from 'react';
import { ArrowLeft, Check, HelpCircle, Minus, Plus, X } from 'lucide-react';
import { PALETTE } from '../theme/palette';

interface ImpostorWordmarkProps {
  large?: boolean;
}

export const ImpostorWordmark: React.FC<ImpostorWordmarkProps> = ({
  large = false,
}) => {
  const fontSizeClass = large
    ? 'text-[40px] sm:text-[48px]'
    : 'text-[28px] sm:text-[32px]';
  const spySize = large ? 46 : 34;

  return (
    <div className="inline-flex select-none items-center justify-center font-display font-black tracking-tight text-[#F7F2EC]">
      <span className={fontSizeClass}>IMP</span>
      {/* Spy fedora inside first O */}
      <span
        className="mx-0.5 inline-flex items-center justify-center rounded-full border-[3px] border-[#F7F2EC] bg-[#241C2F]"
        style={{ width: spySize, height: spySize }}
      >
        <svg
          width={Math.round(spySize * 0.72)}
          height={Math.round(spySize * 0.72)}
          viewBox="0 0 36 36"
          fill="none"
          aria-hidden="true"
        >
          {/* Hat crown & brim */}
          <path
            d="M11 16 L14 8 H22 L25 16 Z"
            fill="#F4CD6C"
          />
          <rect x="6" y="16" width="24" height="3.2" rx="1.6" fill="#F4CD6C" />
          {/* Spy eyes */}
          <circle cx="14" cy="23.5" r="2.1" fill="#F7F2EC" />
          <circle cx="22" cy="23.5" r="2.1" fill="#F7F2EC" />
        </svg>
      </span>
      <span className={fontSizeClass}>ST</span>
      <span className={`${fontSizeClass} text-[#F4CD6C]`}>O</span>
      <span className={fontSizeClass}>R</span>
    </div>
  );
};

interface WizardStepHeaderProps {
  onBack: () => void;
  title?: string;
  trailing?: React.ReactNode;
}

export const WizardStepHeader: React.FC<WizardStepHeaderProps> = ({
  onBack,
  title,
  trailing,
}) => {
  return (
    <div className="flex w-full items-center justify-between gap-3 py-1">
      <button
        type="button"
        onClick={onBack}
        aria-label="Go back"
        className="flex h-11 w-11 shrink-0 items-center justify-center rounded-2xl border border-[#423554] bg-[#241C2F] text-[#F7F2EC] transition hover:border-[#F4CD6C]/60 hover:bg-[#2C2339] active:scale-95"
      >
        <ArrowLeft className="h-5 w-5" />
      </button>
      {title ? (
        <div className="flex-1 truncate text-center font-mono-label text-[11px] font-bold uppercase tracking-[0.22em] text-[#B7ABC8]">
          {title}
        </div>
      ) : (
        <div className="flex-1" />
      )}
      <div className="flex min-w-[44px] items-center justify-end">
        {trailing ?? <span className="h-11 w-11" />}
      </div>
    </div>
  );
};

interface HighlightedHeadlineProps {
  prefix: string;
  highlight: string;
  suffix?: string;
  highlightColor?: string;
  className?: string;
}

export const HighlightedHeadline: React.FC<HighlightedHeadlineProps> = ({
  prefix,
  highlight,
  suffix = '',
  highlightColor = PALETTE.goldAccent,
  className = '',
}) => {
  return (
    <h1
      className={`font-display text-3xl font-extrabold leading-[1.12] tracking-tight text-[#F7F2EC] sm:text-4xl ${className}`}
    >
      {prefix}
      <span style={{ color: highlightColor }}>{highlight}</span>
      {suffix}
    </h1>
  );
};

interface GoldCtaButtonProps {
  text: string;
  onClick: () => void;
  disabled?: boolean;
  icon?: React.ReactNode;
  className?: string;
}

export const GoldCtaButton: React.FC<GoldCtaButtonProps> = ({
  text,
  onClick,
  disabled = false,
  icon,
  className = '',
}) => {
  return (
    <button
      type="button"
      onClick={onClick}
      disabled={disabled}
      className={`inline-flex h-[54px] w-full items-center justify-center gap-2.5 rounded-2xl font-display text-[16px] font-extrabold tracking-wide transition active:scale-[0.985] ${
        disabled
          ? 'cursor-not-allowed border border-[#423554] bg-[#2C2339] text-[#887B9B]'
          : 'bg-[#F4CD6C] text-[#1D1626] shadow-[0_8px_24px_rgba(244,205,108,0.28)] hover:bg-[#FFDE8A]'
      } ${className}`}
    >
      {icon}
      <span>{text}</span>
    </button>
  );
};

interface PlumTogglePillProps {
  checked: boolean;
  onToggle: (next: boolean) => void;
  accentColor?: string;
  ariaLabel?: string;
}

export const PlumTogglePill: React.FC<PlumTogglePillProps> = ({
  checked,
  onToggle,
  accentColor = PALETTE.goldAccent,
  ariaLabel,
}) => {
  return (
    <button
      type="button"
      role="switch"
      aria-checked={checked}
      aria-label={ariaLabel}
      onClick={() => onToggle(!checked)}
      className="relative inline-flex h-8 w-14 shrink-0 cursor-pointer items-center rounded-full border p-1 transition-colors"
      style={{
        backgroundColor: checked ? `${accentColor}30` : '#16101E',
        borderColor: checked ? accentColor : '#423554',
      }}
    >
      <span
        className={`inline-flex h-6 w-6 items-center justify-center rounded-full transition-transform duration-200 ${
          checked ? 'translate-x-6' : 'translate-x-0'
        }`}
        style={{
          backgroundColor: checked ? accentColor : '#887B9B',
        }}
      >
        {checked && <Check className="h-3.5 w-3.5 text-[#1D1626]" strokeWidth={3} />}
      </span>
    </button>
  );
};

interface PlumStepperControlProps {
  value: number;
  min: number;
  max: number;
  onDecrement: () => void;
  onIncrement: () => void;
  label?: string;
  accentColor?: string;
}

export const PlumStepperControl: React.FC<PlumStepperControlProps> = ({
  value,
  min,
  max,
  onDecrement,
  onIncrement,
  label,
  accentColor = PALETTE.goldAccent,
}) => {
  return (
    <div className="inline-flex items-center gap-2 rounded-2xl border border-[#423554] bg-[#16101E] p-1">
      <button
        type="button"
        onClick={onDecrement}
        disabled={value <= min}
        aria-label={`Decrease ${label ?? 'value'}`}
        className="flex h-9 w-9 items-center justify-center rounded-xl bg-[#241C2F] text-[#F7F2EC] transition hover:bg-[#2C2339] disabled:cursor-not-allowed disabled:opacity-35"
      >
        <Minus className="h-4 w-4" />
      </button>
      <span
        className="min-w-[32px] text-center font-display text-lg font-extrabold"
        style={{ color: accentColor }}
      >
        {value}
      </span>
      <button
        type="button"
        onClick={onIncrement}
        disabled={value >= max}
        aria-label={`Increase ${label ?? 'value'}`}
        className="flex h-9 w-9 items-center justify-center rounded-xl bg-[#241C2F] text-[#F7F2EC] transition hover:bg-[#2C2339] disabled:cursor-not-allowed disabled:opacity-35"
      >
        <Plus className="h-4 w-4" />
      </button>
    </div>
  );
};

interface FloatingHeroSpriteProps {
  src: string;
  alt: string;
  accentColor?: string;
  sizeClass?: string;
  showHalo?: boolean;
  delayMs?: number;
  className?: string;
}

export const FloatingHeroSprite: React.FC<FloatingHeroSpriteProps> = ({
  src,
  alt,
  accentColor = PALETTE.goldAccent,
  sizeClass = 'h-32 w-32',
  showHalo = true,
  delayMs = 0,
  className = '',
}) => {
  return (
    <div
      className={`relative inline-flex flex-col items-center justify-end select-none ${sizeClass} ${className}`}
    >
      {showHalo && (
        <div
          aria-hidden="true"
          className="pointer-events-none absolute bottom-1 h-4 w-[76%] rounded-full blur-md"
          style={{
            background: `radial-gradient(ellipse at center, ${accentColor}66 0%, transparent 72%)`,
          }}
        />
      )}
      <img
        src={src}
        alt={alt}
        draggable={false}
        className="relative z-10 h-full w-full object-contain drop-shadow-[0_10px_18px_rgba(0,0,0,0.45)] animate-[floatBob_3.2s_ease-in-out_infinite]"
        style={delayMs ? { animationDelay: `${delayMs}ms` } : undefined}
      />
    </div>
  );
};

interface AddOrRenamePlayerDialogProps {
  isOpen: boolean;
  title: string;
  confirmLabel: string;
  initialName?: string;
  placeholder?: string;
  onConfirm: (name: string) => void;
  onClose: () => void;
}

export const AddOrRenamePlayerDialog: React.FC<AddOrRenamePlayerDialogProps> = ({
  isOpen,
  title,
  confirmLabel,
  initialName = '',
  placeholder = 'Player name...',
  onConfirm,
  onClose,
}) => {
  const [name, setName] = useState(initialName);

  useEffect(() => {
    if (isOpen) {
      setName(initialName);
    }
  }, [isOpen, initialName]);

  if (!isOpen) return null;

  const trimmed = name.trim();

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!trimmed) return;
    onConfirm(trimmed);
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/75 p-4 backdrop-blur-sm">
      <div className="w-full max-w-md rounded-3xl border border-[#423554] bg-[#241C2F] p-6 shadow-2xl">
        <div className="flex items-center justify-between gap-2">
          <h3 className="font-display text-xl font-extrabold text-[#F7F2EC]">
            {title}
          </h3>
          <button
            type="button"
            onClick={onClose}
            aria-label="Close dialog"
            className="rounded-xl p-1.5 text-[#887B9B] hover:bg-[#2C2339] hover:text-[#F7F2EC]"
          >
            <X className="h-5 w-5" />
          </button>
        </div>

        <form onSubmit={handleSubmit} className="mt-4 space-y-4">
          <input
            type="text"
            autoFocus
            maxLength={24}
            value={name}
            onChange={(e) => setName(e.target.value)}
            placeholder={placeholder}
            className="w-full rounded-2xl border border-[#423554] bg-[#16101E] px-4 py-3 text-base font-semibold text-[#F7F2EC] placeholder-[#887B9B] focus:border-[#F4CD6C] focus:outline-none"
          />

          <div className="flex items-center justify-end gap-2.5">
            <button
              type="button"
              onClick={onClose}
              className="rounded-2xl border border-[#423554] bg-[#2C2339] px-4 py-2.5 text-sm font-bold text-[#B7ABC8] hover:text-[#F7F2EC]"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={!trimmed}
              className="rounded-2xl bg-[#F4CD6C] px-5 py-2.5 text-sm font-extrabold text-[#1D1626] transition hover:bg-[#FFDE8A] disabled:cursor-not-allowed disabled:opacity-45"
            >
              {confirmLabel}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

interface HowToPlaySheetProps {
  isOpen: boolean;
  onClose: () => void;
}

export const HowToPlaySheet: React.FC<HowToPlaySheetProps> = ({
  isOpen,
  onClose,
}) => {
  if (!isOpen) return null;

  const steps = [
    {
      num: '01',
      title: 'Pass the phone secretly',
      body: 'Each player taps to flip their dossier card. Agents see the secret word (and optional hint). Impostors see only the category—or nothing at all.',
    },
    {
      num: '02',
      title: 'Give sharp 1-word clues',
      body: 'Go around the table! Give a single clue that proves you know the word without handing it away to the impostor.',
    },
    {
      num: '03',
      title: 'Use special powers & debate',
      body: 'Enable Agent Killer (one-shot blind execution), Savior Agent (secret vote shield), and Agent Director (double vote weight) for tactical twists.',
    },
    {
      num: '04',
      title: 'Vote & expose—or steal the round',
      body: 'Lock in secret votes around the table. Even if caught, an impostor who guesses the exact secret word steals the victory!',
    },
  ];

  return (
    <div className="fixed inset-0 z-50 flex items-end justify-center bg-black/75 backdrop-blur-sm sm:items-center sm:p-4">
      <div className="max-h-[88vh] w-full max-w-lg overflow-y-auto rounded-t-3xl border border-[#423554] bg-[#241C2F] p-6 shadow-2xl sm:rounded-3xl">
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-2.5">
            <div className="flex h-10 w-10 items-center justify-center rounded-2xl bg-[#3A2F17] text-[#F4CD6C]">
              <HelpCircle className="h-5 w-5" />
            </div>
            <div>
              <div className="font-mono-label text-[11px] font-bold uppercase tracking-[0.18em] text-[#F4CD6C]">
                Field Manual
              </div>
              <h2 className="font-display text-xl font-extrabold text-[#F7F2EC]">
                How to Play Impostor
              </h2>
            </div>
          </div>
          <button
            type="button"
            onClick={onClose}
            aria-label="Close how to play"
            className="rounded-xl p-2 text-[#887B9B] hover:bg-[#2C2339] hover:text-[#F7F2EC]"
          >
            <X className="h-5 w-5" />
          </button>
        </div>

        <div className="mt-5 space-y-3">
          {steps.map((s) => (
            <div
              key={s.num}
              className="flex gap-3.5 rounded-2xl border border-[#423554] bg-[#1D1626] p-4"
            >
              <div className="flex h-8 w-8 shrink-0 items-center justify-center rounded-xl bg-[#3A2F17] font-mono-label text-xs font-extrabold text-[#F4CD6C]">
                {s.num}
              </div>
              <div>
                <h3 className="font-display text-base font-bold text-[#F7F2EC]">
                  {s.title}
                </h3>
                <p className="mt-1 text-sm leading-relaxed text-[#B7ABC8]">
                  {s.body}
                </p>
              </div>
            </div>
          ))}
        </div>

        <div className="mt-6">
          <GoldCtaButton text="Got it, let's play" onClick={onClose} />
        </div>
      </div>
    </div>
  );
};
