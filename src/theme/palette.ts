import { GameMode, GameRoleIdentity, PlayerRole } from '../types/game';

export interface RoleVisualStyle {
  accent: string;
  glow: string;
  deepSurface: string;
  border: string;
}

export const PALETTE = {
  obsidianBg: '#1D1626',
  obsidianSurface: '#241C2F',
  obsidianSurfaceHigh: '#2C2339',
  obsidianSurfaceBright: '#372C47',
  obsidianSurfaceLowest: '#16101E',
  focusedCard: '#31253E',
  obsidianOutline: '#423554',

  textPrimary: '#F7F2EC',
  textSecondary: '#B7ABC8',
  textMuted: '#887B9B',

  goldAccent: '#F4CD6C',
  goldGlow: '#FFDE8A',
  goldSurface: '#3A2F17',
  goldDarkInk: '#1D1626',

  // Cohesive aliases matching Color.kt
  cyanAccent: '#F4CD6C',
  cyanSurface: '#3A2F17',
  timerAccent: '#F4CD6C',
  timerSurface: '#3A2F17',

  normalAccent: '#1AE682',
  normalGlow: '#5CFFB0',
  normalSurface: '#122C24',
  normalBorder: '#12C46D',

  impostorAccent: '#FF3E5B',
  impostorGlow: '#FF7A8F',
  impostorSurface: '#341624',
  impostorBorder: '#E12D48',

  killerAccent: '#FF2985',
  killerGlow: '#FF78B4',
  killerSurface: '#34142B',
  killerBorder: '#E0166C',

  saviorAccent: '#2CCBFF',
  saviorGlow: '#82E4FF',
  saviorSurface: '#13293E',
  saviorBorder: '#12A7DB',

  directorAccent: '#FFB800',
  directorGlow: '#FFDA66',
  directorSurface: '#38290E',
  directorBorder: '#E09600',
} as const;

export const ROLE_IDENTITY_META: Record<
  GameRoleIdentity,
  { displayTitle: string; shortLabel: string; emblemDescription: string }
> = {
  [GameRoleIdentity.AGENT]: {
    displayTitle: 'AGENT',
    shortLabel: 'AGENT',
    emblemDescription: 'Agent tactical shield crest emblem',
  },
  [GameRoleIdentity.IMPOSTOR]: {
    displayTitle: 'IMPOSTOR',
    shortLabel: 'IMPOSTOR',
    emblemDescription: 'Impostor phantom visor emblem',
  },
  [GameRoleIdentity.AGENT_KILLER]: {
    displayTitle: 'AGENT KILLER',
    shortLabel: 'AGENT KILLER',
    emblemDescription: 'Agent Killer assassin crosshair emblem',
  },
  [GameRoleIdentity.SAVIOR_AGENT]: {
    displayTitle: 'SAVIOR AGENT',
    shortLabel: 'SAVIOR',
    emblemDescription: 'Savior Agent aegis halo shield emblem',
  },
  [GameRoleIdentity.AGENT_DIRECTOR]: {
    displayTitle: 'AGENT DIRECTOR',
    shortLabel: 'DIRECTOR',
    emblemDescription: 'Agent Director sovereign command emblem',
  },
};

export function resolveRoleIdentity(
  role: PlayerRole,
  isUltimateKiller = false,
  isSavior = false,
  isDirector = false
): GameRoleIdentity {
  if (role === PlayerRole.IMPOSTOR && isUltimateKiller) return GameRoleIdentity.AGENT_KILLER;
  if (role === PlayerRole.IMPOSTOR) return GameRoleIdentity.IMPOSTOR;
  if (isSavior) return GameRoleIdentity.SAVIOR_AGENT;
  if (isDirector) return GameRoleIdentity.AGENT_DIRECTOR;
  return GameRoleIdentity.AGENT;
}

export function getRoleVisualStyle(identity: GameRoleIdentity): RoleVisualStyle {
  switch (identity) {
    case GameRoleIdentity.AGENT:
      return {
        accent: PALETTE.normalAccent,
        glow: PALETTE.normalGlow,
        deepSurface: PALETTE.normalSurface,
        border: PALETTE.normalBorder,
      };
    case GameRoleIdentity.IMPOSTOR:
      return {
        accent: PALETTE.impostorAccent,
        glow: PALETTE.impostorGlow,
        deepSurface: PALETTE.impostorSurface,
        border: PALETTE.impostorBorder,
      };
    case GameRoleIdentity.AGENT_KILLER:
      return {
        accent: PALETTE.killerAccent,
        glow: PALETTE.killerGlow,
        deepSurface: PALETTE.killerSurface,
        border: PALETTE.killerBorder,
      };
    case GameRoleIdentity.SAVIOR_AGENT:
      return {
        accent: PALETTE.saviorAccent,
        glow: PALETTE.saviorGlow,
        deepSurface: PALETTE.saviorSurface,
        border: PALETTE.saviorBorder,
      };
    case GameRoleIdentity.AGENT_DIRECTOR:
      return {
        accent: PALETTE.directorAccent,
        glow: PALETTE.directorGlow,
        deepSurface: PALETTE.directorSurface,
        border: PALETTE.directorBorder,
      };
  }
}

export const GAME_MODE_META: Record<
  GameMode,
  { title: string; shortTitle: string; subtitle: string; defaultTurnSeconds: number }
> = {
  [GameMode.CLASSIC]: {
    title: 'Classic Mode',
    shortTitle: 'Classic',
    subtitle: 'Bangladeshi, Bollywood & trendy global words, tough 1-word clues, 45s turns',
    defaultTurnSeconds: 45,
  },
  [GameMode.SPEED_ROUND]: {
    title: 'Speed Round',
    shortTitle: 'Speed',
    subtitle: 'Rapid-fire 10s turns — think and guess fast!',
    defaultTurnSeconds: 10,
  },
  [GameMode.EXPERT]: {
    title: 'Expert Mode',
    shortTitle: 'Expert',
    subtitle: 'Cryptic 1-word clues for sharp sleuths, 60s turns',
    defaultTurnSeconds: 60,
  },
};

export const GAME_PHASE_TITLES = {
  LOBBY: 'Lobby',
  DISCUSSION: 'Discussion',
  VOTING: 'Voting',
  RESULTS: 'Results',
} as const;
