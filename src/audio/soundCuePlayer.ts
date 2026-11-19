import { useEffect, useState } from 'react';

interface ToneSegment {
  freqHz: number;
  durationMs: number;
  gain: number;
}

class SoundCueManager {
  private soundEnabled = true;
  private emergencyAlertSoundEnabled = true;
  private audioCtx: AudioContext | null = null;
  private listeners = new Set<() => void>();

  private notify() {
    this.listeners.forEach((fn) => fn());
  }

  subscribe(listener: () => void): () => void {
    this.listeners.add(listener);
    return () => {
      this.listeners.delete(listener);
    };
  }

  getSoundEnabled(): boolean {
    return this.soundEnabled;
  }

  getEmergencyAlertSoundEnabled(): boolean {
    return this.emergencyAlertSoundEnabled;
  }

  toggleSound = () => {
    this.soundEnabled = !this.soundEnabled;
    this.notify();
  };

  setSoundEnabled = (enabled: boolean) => {
    this.soundEnabled = enabled;
    this.notify();
  };

  toggleEmergencyAlertSound = () => {
    this.emergencyAlertSoundEnabled = !this.emergencyAlertSoundEnabled;
    this.notify();
  };

  setEmergencyAlertSoundEnabled = (enabled: boolean) => {
    this.emergencyAlertSoundEnabled = enabled;
    this.notify();
  };

  playRoleRevealCue(isImpostor: boolean) {
    if (!this.soundEnabled) return;
    const notes: ToneSegment[] = isImpostor
      ? [
          { freqHz: 220.0, durationMs: 130, gain: 0.22 },
          { freqHz: 0.0, durationMs: 25, gain: 0.0 },
          { freqHz: 185.0, durationMs: 220, gain: 0.24 },
        ]
      : [
          { freqHz: 523.25, durationMs: 110, gain: 0.16 },
          { freqHz: 0.0, durationMs: 18, gain: 0.0 },
          { freqHz: 659.25, durationMs: 180, gain: 0.18 },
        ];
    this.playSequence(notes);
  }

  playTimerExpiredCue() {
    if (!this.soundEnabled) return;
    this.playSequence([
      { freqHz: 783.99, durationMs: 95, gain: 0.18 },
      { freqHz: 0.0, durationMs: 65, gain: 0.0 },
      { freqHz: 659.25, durationMs: 150, gain: 0.2 },
    ]);
  }

  playEmergencyMeetingCue() {
    if (!this.soundEnabled || !this.emergencyAlertSoundEnabled) return;
    this.playSequence([
      { freqHz: 880.0, durationMs: 110, gain: 0.22 },
      { freqHz: 0.0, durationMs: 45, gain: 0.0 },
      { freqHz: 622.25, durationMs: 130, gain: 0.24 },
      { freqHz: 0.0, durationMs: 45, gain: 0.0 },
      { freqHz: 880.0, durationMs: 180, gain: 0.24 },
    ]);
  }

  playVotingResultsCue(crewWon: boolean) {
    if (!this.soundEnabled) return;
    const notes: ToneSegment[] = crewWon
      ? [
          { freqHz: 523.25, durationMs: 110, gain: 0.16 },
          { freqHz: 659.25, durationMs: 110, gain: 0.17 },
          { freqHz: 783.99, durationMs: 220, gain: 0.19 },
        ]
      : [
          { freqHz: 392.0, durationMs: 120, gain: 0.18 },
          { freqHz: 311.13, durationMs: 130, gain: 0.19 },
          { freqHz: 261.63, durationMs: 240, gain: 0.2 },
        ];
    this.playSequence(notes);
  }

  private getContext(): AudioContext | null {
    if (typeof window === 'undefined') return null;
    const Ctor =
      window.AudioContext ||
      (window as unknown as { webkitAudioContext?: typeof AudioContext }).webkitAudioContext;
    if (!Ctor) return null;
    if (!this.audioCtx) {
      this.audioCtx = new Ctor();
    }
    if (this.audioCtx.state === 'suspended') {
      void this.audioCtx.resume().catch(() => {});
    }
    return this.audioCtx;
  }

  private playSequence(segments: ToneSegment[]) {
    try {
      const ctx = this.getContext();
      if (!ctx) return;

      const sampleRate = 22050;
      const totalSamples = segments.reduce(
        (acc, seg) => acc + Math.floor((sampleRate * seg.durationMs) / 1000),
        0
      );
      if (totalSamples <= 0) return;

      const audioBuffer = ctx.createBuffer(1, totalSamples, sampleRate);
      const channelData = audioBuffer.getChannelData(0);
      const fadeSamples = Math.floor((sampleRate * 14) / 1000);
      let cursor = 0;

      for (const seg of segments) {
        const count = Math.floor((sampleRate * seg.durationMs) / 1000);
        if (count <= 0) continue;
        for (let i = 0; i < count; i++) {
          if (seg.freqHz <= 0 || seg.gain <= 0) {
            channelData[cursor++] = 0;
            continue;
          }
          const attack = i < fadeSamples ? i / Math.max(1, fadeSamples) : 1;
          const release = i > count - fadeSamples ? (count - i) / Math.max(1, fadeSamples) : 1;
          const env = Math.max(0, Math.min(1, Math.min(attack, release)));
          const angle = 2 * Math.PI * seg.freqHz * (i / sampleRate);
          // Blend fundamental + gentle 2nd harmonic matching SoundCuePlayer.kt
          const wave = 0.85 * Math.sin(angle) + 0.15 * Math.sin(angle * 2);
          channelData[cursor++] = Math.max(-1, Math.min(1, wave * env * seg.gain));
        }
      }

      const source = ctx.createBufferSource();
      source.buffer = audioBuffer;
      source.connect(ctx.destination);
      source.start();
    } catch {
      // Gracefully no-op if browser blocks autoplay or lacks Web Audio
    }
  }
}

export const SoundCuePlayer = new SoundCueManager();

export function useSoundSettings() {
  const [soundEnabled, setSoundEnabled] = useState(SoundCuePlayer.getSoundEnabled());
  const [emergencyAlertSoundEnabled, setEmergencyAlertSoundEnabled] = useState(
    SoundCuePlayer.getEmergencyAlertSoundEnabled()
  );

  useEffect(() => {
    return SoundCuePlayer.subscribe(() => {
      setSoundEnabled(SoundCuePlayer.getSoundEnabled());
      setEmergencyAlertSoundEnabled(SoundCuePlayer.getEmergencyAlertSoundEnabled());
    });
  }, []);

  return {
    soundEnabled,
    emergencyAlertSoundEnabled,
    toggleSound: SoundCuePlayer.toggleSound,
    setSoundEnabled: SoundCuePlayer.setSoundEnabled,
    toggleEmergencyAlertSound: SoundCuePlayer.toggleEmergencyAlertSound,
    setEmergencyAlertSoundEnabled: SoundCuePlayer.setEmergencyAlertSoundEnabled,
  };
}
