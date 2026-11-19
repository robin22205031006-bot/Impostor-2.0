export enum PlayerRole {
  NORMAL = 'NORMAL',
  IMPOSTOR = 'IMPOSTOR',
}

export enum GamePhase {
  LOBBY = 'LOBBY',
  DISCUSSION = 'DISCUSSION',
  VOTING = 'VOTING',
  RESULTS = 'RESULTS',
}

export enum RoundOutcome {
  PENDING = 'PENDING',
  VICTORY = 'VICTORY',
  DEFEAT = 'DEFEAT',
}

export enum TurnStatus {
  IDLE = 'IDLE',
  ACTIVE = 'ACTIVE',
  PAUSED = 'PAUSED',
  EXPIRED = 'EXPIRED',
}

export enum GameMode {
  CLASSIC = 'CLASSIC',
  SPEED_ROUND = 'SPEED_ROUND',
  EXPERT = 'EXPERT',
}

export enum SetupWizardStep {
  WELCOME = 'WELCOME',
  MODE_PICK = 'MODE_PICK',
  PLAYERS = 'PLAYERS',
  RULES = 'RULES',
  PACKS = 'PACKS',
}

export enum LocalPhase {
  SETUP = 'SETUP',
  REVEAL = 'REVEAL',
  PLAYING = 'PLAYING',
  VOTING = 'VOTING',
  RESULTS = 'RESULTS',
}

export enum AvatarPose {
  ORIGINAL = 'ORIGINAL',
  ACTION_STANCE = 'ACTION_STANCE',
  TACTICAL_BUST = 'TACTICAL_BUST',
  CREST_AURA = 'CREST_AURA',
}

export enum AvatarGraphicMotif {
  INK_WASH = 'INK_WASH',
  EMERALD_RUNES = 'EMERALD_RUNES',
  CRIMSON_EMBERS = 'CRIMSON_EMBERS',
  CYBER_HUD = 'CYBER_HUD',
  LUNAR_RIPPLES = 'LUNAR_RIPPLES',
  SOLAR_RAYS = 'SOLAR_RAYS',
  ARCANE_TIDE = 'ARCANE_TIDE',
  BLADE_SLASH = 'BLADE_SLASH',
  FEATHER_PLUME = 'FEATHER_PLUME',
  FOREST_WIND = 'FOREST_WIND',
}

export enum AvatarCategoryFilter {
  ALL = 'ALL',
  BLADES = 'BLADES',
  MYSTIC = 'MYSTIC',
  VANGUARD = 'VANGUARD',
}

export enum GameRoleIdentity {
  AGENT = 'AGENT',
  IMPOSTOR = 'IMPOSTOR',
  AGENT_KILLER = 'AGENT_KILLER',
  SAVIOR_AGENT = 'SAVIOR_AGENT',
  AGENT_DIRECTOR = 'AGENT_DIRECTOR',
}

export interface AvatarCharacter {
  id: string;
  code: string;
  name: string;
  title: string;
  archetype: string;
  filterGroup: AvatarCategoryFilter;
  motif: AvatarGraphicMotif;
  accentColor: string;
  glowColor: string;
  surfaceTint: string;
  baseImage: string;
  actionImage: string;
  skillImage: string;
  stanceImage: string;
  bustBiasX: number;
  bustBiasY: number;
  bustZoom: number;
}

export interface WordEntry {
  word: string;
  category: string;
  genre: string;
  hint: string;
  expertHint: string;
}

export interface PlayerWordBriefing {
  role: PlayerRole;
  secretWord: string | null;
  agentHint: string | null;
  category: string | null;
  genre: string | null;
  isSecretWordHidden: boolean;
}

export interface LocalPlayer {
  id: string;
  name: string;
  score: number;
  avatarId: string;
  avatarPose: AvatarPose;
}

export interface EmergencyMeetingAlert {
  isActive: boolean;
  reporterName: string;
  reason: string;
  countdownSeconds: number;
}

export interface LocalGameUiState {
  phase: LocalPhase;
  setupStep: SetupWizardStep;
  players: LocalPlayer[];
  gameMode: GameMode;
  impostorCount: number;
  turnDurationSeconds: number;
  selectedCategory: string;
  favoritePacks: Set<string>;
  hintsEnabled: boolean;
  categoryCluesEnabled: boolean;
  timerEnabled: boolean;
  impostorAbilityEnabled: boolean;
  saviorAgentEnabled: boolean;
  agentDirectorEnabled: boolean;
  agentPredictionEnabled: boolean;
  roundNumber: number;
  activeWord: WordEntry | null;
  activeHint: string;
  impostorIds: Set<string>;
  ultimateKillerId: string | null;
  rawKillerTargetId: string | null;
  nullifiedVoterId: string | null;
  saviorAgentId: string | null;
  protectedPlayerId: string | null;
  saviorSavedTarget: boolean;
  agentDirectorId: string | null;
  agentPredictions: Record<string, string>;
  revealPlayerIndex: number;
  isRoleCardVisible: boolean;
  turnOrderIds: string[];
  currentTurnIndex: number;
  remainingTurnSeconds: number;
  isTimerPaused: boolean;
  votesByPlayerId: Record<string, string>;
  votingPlayerIndex: number;
  isVoteCardRevealed: boolean;
  impostorWordGuess: string;
  impostorGuessCorrect: boolean;
  crewWon: boolean;
  roundDeltas: Record<string, number>;
  roundVoteCounts: Record<string, number>;
  usedWords: Set<string>;
  emergencyMeeting: EmergencyMeetingAlert;
  errorBanner: string | null;
}

export interface GameState {
  phase: GamePhase;
  players: LocalPlayer[];
  impostorCount: number;
  selectedCategory: string;
  hintsEnabled: boolean;
  sessionSeedNumber: number;
  playerRoles: Record<string, PlayerRole>;
  revealedRolePlayerIds: Set<string>;
  locationWord: WordEntry | null;
  usedWords: Set<string>;
  turnOrderIds: string[];
  currentTurnIndex: number;
  turnStatus: TurnStatus;
  currentTimerCount: number;
  turnDurationSeconds: number;
  votesByPlayerId: Record<string, string>;
  totalVotesPerPlayer: Record<string, number>;
  votedOutPlayerIds: string[];
  roundOutcome: RoundOutcome;
  outcomeReason: string;
  emergencyAlertSoundEnabled: boolean;
  soundEffectsEnabled: boolean;
  emergencyMeeting: EmergencyMeetingAlert;
}

export interface TurnExpiredBroadcast {
  playerName: string;
  turnIndex: number;
  totalTurns: number;
  timestampMs: number;
}

export interface EliminatedPlayerReveal {
  id: string;
  name: string;
  isImpostor: boolean;
  voteCount: number;
  roleIdentity: GameRoleIdentity;
  avatarId: string;
  avatarPose: AvatarPose;
}
