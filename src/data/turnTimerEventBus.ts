import { TurnExpiredBroadcast } from '../types/game';

type Listener = (event: TurnExpiredBroadcast) => void;

const listeners = new Set<Listener>();
let lastSignature = '';
let lastEmitMs = 0;

export const TurnTimerEventBus = {
  emitExpired(playerName: string, turnIndex: number, totalTurns: number): void {
    const now = Date.now();
    const signature = `${turnIndex}|${totalTurns}|${playerName}`;
    if (signature === lastSignature && now - lastEmitMs < 1500) {
      return;
    }
    lastSignature = signature;
    lastEmitMs = now;
    const payload: TurnExpiredBroadcast = {
      playerName,
      turnIndex,
      totalTurns,
      timestampMs: now,
    };
    listeners.forEach((cb) => cb(payload));
  },

  subscribe(listener: Listener): () => void {
    listeners.add(listener);
    return () => {
      listeners.delete(listener);
    };
  },
};
