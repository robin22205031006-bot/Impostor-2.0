import React, { useMemo, useState } from 'react';
import {
  Check,
  Clock,
  Crown,
  Edit3,
  Eye,
  Heart,
  HelpCircle,
  Layers,
  Palette,
  Plus,
  Search,
  Sparkles,
  Trash2,
  Users,
  X,
  Zap,
} from 'lucide-react';
import { CharacterAvatarThumbnail } from '../components/AvatarCardGraphics';
import { AvatarPickerSheet } from '../components/AvatarPickerSheet';
import {
  AddOrRenamePlayerDialog,
  FloatingHeroSprite,
  GoldCtaButton,
  HighlightedHeadline,
  HowToPlaySheet,
  ImpostorWordmark,
  PlumStepperControl,
  PlumTogglePill,
  WizardStepHeader,
} from '../components/ImpostorWizardComponents';
import { RoleEmblemBadge } from '../components/RoleEmblem';
import { AVATAR_POSE_META, AvatarCatalog } from '../data/avatarCatalog';
import { CATEGORY_ALL, useWordBank, WordBank } from '../data/wordBank';
import { GAME_MODE_META, PALETTE } from '../theme/palette';
import {
  AvatarPose,
  GameMode,
  GameRoleIdentity,
  LocalGameUiState,
  SetupWizardStep,
} from '../types/game';

interface GameSetupScreenProps {
  state: LocalGameUiState;
  onSetSetupStep: (step: SetupWizardStep) => void;
  onEnterRoleLobbyMode: () => void;
  onAddPlayer: (name: string) => void;
  onRenamePlayer: (playerId: string, name: string) => void;
  onRemovePlayer: (playerId: string) => void;
  onSetPlayerCount: (count: number) => void;
  onSetPlayerAvatar: (playerId: string, avatarId: string, pose?: AvatarPose) => void;
  onCyclePlayerPose: (playerId: string) => void;
  onSetGameMode: (mode: GameMode) => void;
  onSetImpostorCount: (count: number) => void;
  onSetTurnDuration: (seconds: number) => void;
  onSetTimerEnabled: (enabled: boolean) => void;
  onSetHintsEnabled: (enabled: boolean) => void;
  onSetCategoryCluesEnabled: (enabled: boolean) => void;
  onSetImpostorAbilityEnabled: (enabled: boolean) => void;
  onSetSaviorAgentEnabled: (enabled: boolean) => void;
  onSetAgentDirectorEnabled: (enabled: boolean) => void;
  onSetAgentPredictionEnabled: (enabled: boolean) => void;
  onSetCategory: (category: string) => void;
  onToggleFavoritePack: (category: string) => void;
  onStartRound: () => void;
}

const WELCOME_LINEUP = [
  { id: 'avatar_01', offset: '-mr-4 translate-y-2', size: 'h-24 w-24 sm:h-28 sm:w-28', z: 'z-10', delay: 150 },
  { id: 'avatar_08', offset: '-mr-4 translate-y-1', size: 'h-28 w-28 sm:h-32 sm:w-32', z: 'z-20', delay: 80 },
  { id: 'avatar_05', offset: '', size: 'h-32 w-32 sm:h-36 sm:w-36', z: 'z-30', delay: 0 },
  { id: 'avatar_09', offset: '-ml-4 translate-y-1', size: 'h-28 w-28 sm:h-32 sm:w-32', z: 'z-20', delay: 110 },
  { id: 'avatar_18', offset: '-ml-4 translate-y-2', size: 'h-24 w-24 sm:h-28 sm:w-28', z: 'z-10', delay: 200 },
];

type PackFilterTab = 'ALL' | 'BUILTIN' | 'CUSTOM' | 'FAVORITES';

export const GameSetupScreen: React.FC<GameSetupScreenProps> = ({
  state,
  onSetSetupStep,
  onEnterRoleLobbyMode,
  onAddPlayer,
  onRenamePlayer,
  onRemovePlayer,
  onSetPlayerCount,
  onSetPlayerAvatar,
  onCyclePlayerPose,
  onSetGameMode,
  onSetImpostorCount,
  onSetTurnDuration,
  onSetTimerEnabled,
  onSetHintsEnabled,
  onSetCategoryCluesEnabled,
  onSetImpostorAbilityEnabled,
  onSetSaviorAgentEnabled,
  onSetAgentDirectorEnabled,
  onSetAgentPredictionEnabled,
  onSetCategory,
  onToggleFavoritePack,
  onStartRound,
}) => {
  const [howToPlayOpen, setHowToPlayOpen] = useState(false);
  const [addPlayerOpen, setAddPlayerOpen] = useState(false);
  const [renameTarget, setRenameTarget] = useState<{ id: string; name: string } | null>(null);
  const [avatarDeckPlayerId, setAvatarDeckPlayerId] = useState<string | null>(null);

  // Packs step UI state
  const wordBankSnapshot = useWordBank();
  const [packSearch, setPackSearch] = useState('');
  const [packFilter, setPackFilter] = useState<PackFilterTab>('ALL');
  const [peekCategory, setPeekCategory] = useState<string | null>(null);
  const [addCustomWordOpen, setAddCustomWordOpen] = useState(false);

  // Custom word modal inputs
  const [customPackTitleInput, setCustomPackTitleInput] = useState('');
  const [newWordText, setNewWordText] = useState('');
  const [newGenreText, setNewGenreText] = useState('');
  const [newHintText, setNewHintText] = useState('');

  const allCategories = wordBankSnapshot.categories;
  const filteredCategories = useMemo(() => {
    return allCategories.filter((cat) => {
      const isCustom = WordBank.isCustomCategory(cat);
      const isAll = cat === CATEGORY_ALL;
      const isFav = state.favoritePacks.has(cat);

      if (packFilter === 'BUILTIN' && (isCustom || isAll)) return false;
      if (packFilter === 'CUSTOM' && !isCustom) return false;
      if (packFilter === 'FAVORITES' && !isFav) return false;

      if (packSearch.trim()) {
        const q = packSearch.trim().toLowerCase();
        const label = isAll ? 'All Categories Mixed' : cat;
        if (label.toLowerCase().includes(q)) return true;
        return WordBank.wordsForCategory(cat).some(
          (entry) =>
            entry.word.toLowerCase().includes(q) ||
            entry.genre.toLowerCase().includes(q)
        );
      }
      return true;
    });
  }, [allCategories, packFilter, packSearch, state.favoritePacks]);

  // STEP 1: WELCOME
  if (state.setupStep === SetupWizardStep.WELCOME) {
    return (
      <div className="mx-auto flex min-h-screen w-full max-w-xl flex-col items-center justify-between px-5 pb-10 pt-10 text-center">
        <div className="w-full pt-4">
          <div className="inline-flex items-center gap-2 rounded-full border border-[#F4CD6C]/35 bg-[#3A2F17]/50 px-3.5 py-1 font-mono-label text-[10px] font-extrabold uppercase tracking-[0.22em] text-[#FFDE8A]">
            <Sparkles className="h-3.5 w-3.5 text-[#F4CD6C]" />
            <span>SOCIAL DEDUCTION PARTY GAME</span>
          </div>
          <div className="mt-5">
            <ImpostorWordmark large />
          </div>
        </div>

        {/* 5-Hero Overlapping Fan Lineup */}
        <div className="my-6 flex w-full flex-col items-center">
          <div className="relative flex items-end justify-center px-2 py-4">
            {WELCOME_LINEUP.map((slot) => {
              const char = AvatarCatalog.findById(slot.id);
              return (
                <div key={slot.id} className={`relative ${slot.offset} ${slot.z}`}>
                  <FloatingHeroSprite
                    src={char.baseImage}
                    alt={char.name}
                    accentColor={char.accentColor}
                    sizeClass={slot.size}
                    delayMs={slot.delay}
                  />
                </div>
              );
            })}
          </div>

          <h1 className="mt-4 font-display text-3xl font-extrabold leading-tight tracking-tight text-[#F7F2EC] sm:text-4xl">
            Your friends.{' '}
            <span className="text-[#F4CD6C]">One terrible liar.</span>
          </h1>
          <p className="mt-2.5 max-w-md text-sm font-medium leading-relaxed text-[#B7ABC8]">
            Pass one phone around the table, drop sharp 1-word clues, trigger
            tactical role powers, and unmask the hidden impostor.
          </p>
        </div>

        <div className="w-full space-y-3">
          <GoldCtaButton
            text="Start a game"
            onClick={() => onSetSetupStep(SetupWizardStep.MODE_PICK)}
            icon={<Sparkles className="h-5 w-5" />}
          />
          <button
            type="button"
            onClick={() => setHowToPlayOpen(true)}
            className="inline-flex h-12 w-full items-center justify-center gap-2 rounded-2xl border border-[#423554] bg-[#241C2F] font-display text-sm font-bold text-[#F7F2EC] transition hover:border-[#F4CD6C]/60"
          >
            <HelpCircle className="h-4 w-4 text-[#F4CD6C]" />
            <span>How to play</span>
          </button>
        </div>

        <HowToPlaySheet
          isOpen={howToPlayOpen}
          onClose={() => setHowToPlayOpen(false)}
        />
      </div>
    );
  }

  // STEP 2: MODE PICK
  if (state.setupStep === SetupWizardStep.MODE_PICK) {
    const kuroha = AvatarCatalog.findById('avatar_01');
    const zero = AvatarCatalog.findById('avatar_18');

    return (
      <div className="mx-auto flex min-h-screen w-full max-w-xl flex-col px-5 pb-10 pt-5">
        <WizardStepHeader
          title="STEP 1 OF 4 &bull; CHOOSE MODE"
          onBack={() => onSetSetupStep(SetupWizardStep.WELCOME)}
        />

        <div className="mt-6">
          <HighlightedHeadline
            prefix="How are we "
            highlight="playing tonight?"
          />
          <p className="mt-2 text-sm font-medium text-[#B7ABC8]">
            Pick your table format—full pass-and-play arena or fast role assignment.
          </p>
        </div>

        <div className="mt-6 space-y-4">
          {/* Card 1: One Phone */}
          <button
            type="button"
            onClick={() => onSetSetupStep(SetupWizardStep.PLAYERS)}
            className="group relative flex w-full items-center justify-between gap-4 overflow-hidden rounded-3xl border-2 border-[#F4CD6C]/70 bg-[#241C2F] p-5 text-left shadow-[0_10px_30px_rgba(244,205,108,0.14)] transition hover:border-[#FFDE8A] active:scale-[0.99]"
          >
            <div className="min-w-0 flex-1 space-y-2">
              <span className="inline-flex items-center gap-1.5 rounded-full bg-[#3A2F17] px-2.5 py-0.5 font-mono-label text-[10px] font-extrabold uppercase tracking-wider text-[#FFDE8A]">
                <Sparkles className="h-3 w-3" />
                <span>RECOMMENDED &bull; PASS &amp; PLAY</span>
              </span>
              <h2 className="font-display text-2xl font-extrabold text-[#F7F2EC]">
                One phone
              </h2>
              <p className="text-xs font-medium leading-relaxed text-[#B7ABC8]">
                Pass one device for 3D secret reveals, live turn timer, special
                role abilities, secret voting &amp; scoreboard.
              </p>
            </div>
            <FloatingHeroSprite
              src={kuroha.baseImage}
              alt={kuroha.name}
              accentColor={kuroha.accentColor}
              sizeClass="h-28 w-24 shrink-0"
            />
          </button>

          {/* Card 2: Role Assignment */}
          <button
            type="button"
            onClick={onEnterRoleLobbyMode}
            className="group relative flex w-full items-center justify-between gap-4 overflow-hidden rounded-3xl border border-[#423554] bg-[#241C2F] p-5 text-left transition hover:border-[#2CCBFF]/70 active:scale-[0.99]"
          >
            <div className="min-w-0 flex-1 space-y-2">
              <span className="inline-flex items-center gap-1.5 rounded-full bg-[#13293E] px-2.5 py-0.5 font-mono-label text-[10px] font-extrabold uppercase tracking-wider text-[#82E4FF]">
                <Users className="h-3 w-3" />
                <span>QUICK DEAL LOBBY</span>
              </span>
              <h2 className="font-display text-2xl font-extrabold text-[#F7F2EC]">
                Role Assignment
              </h2>
              <p className="text-xs font-medium leading-relaxed text-[#B7ABC8]">
                Instantly deal roles, reveal or peek cards in one grid, run a
                timed discussion &amp; cast quick table votes.
              </p>
            </div>
            <FloatingHeroSprite
              src={zero.baseImage}
              alt={zero.name}
              accentColor={zero.accentColor}
              sizeClass="h-28 w-24 shrink-0"
              delayMs={180}
            />
          </button>
        </div>
      </div>
    );
  }

  // STEP 3: PLAYERS + AVATAR DECK
  if (state.setupStep === SetupWizardStep.PLAYERS) {
    const stagePlayers = state.players.slice(0, 3);
    const centerPlayer = stagePlayers[1] ?? stagePlayers[0];
    const leftPlayer = stagePlayers[0];
    const rightPlayer = stagePlayers[2];

    return (
      <div className="mx-auto flex min-h-screen w-full max-w-xl flex-col px-5 pb-28 pt-5">
        <WizardStepHeader
          title="STEP 2 OF 4 &bull; PLAYERS"
          onBack={() => onSetSetupStep(SetupWizardStep.MODE_PICK)}
          trailing={
            <button
              type="button"
              onClick={() =>
                setAvatarDeckPlayerId(state.players[0]?.id ?? null)
              }
              className="inline-flex items-center gap-1.5 rounded-2xl border border-[#F4CD6C]/50 bg-[#3A2F17] px-3 py-2 text-xs font-extrabold text-[#FFDE8A] hover:bg-[#4B3D1E]"
            >
              <Palette className="h-3.5 w-3.5" />
              <span>Avatar Deck</span>
            </button>
          }
        />

        <div className="mt-5 flex items-center justify-between gap-3">
          <div>
            <HighlightedHeadline prefix="Who's " highlight="in the room?" />
            <p className="mt-1 text-xs font-medium text-[#B7ABC8]">
              2–16 players &bull; Tap the center hero to cycle pose
            </p>
          </div>
          <PlumStepperControl
            value={state.players.length}
            min={2}
            max={16}
            label="player count"
            onDecrement={() => onSetPlayerCount(state.players.length - 1)}
            onIncrement={() => onSetPlayerCount(state.players.length + 1)}
          />
        </div>

        {/* Interactive 3-Hero Stage */}
        {stagePlayers.length > 0 && (
          <div className="mt-5 flex items-end justify-center rounded-3xl border border-[#423554] bg-[#241C2F]/80 px-4 py-4">
            {leftPlayer && state.players.length > 1 && leftPlayer.id !== centerPlayer?.id && (
              <button
                type="button"
                onClick={() => setAvatarDeckPlayerId(leftPlayer.id)}
                className="flex flex-col items-center opacity-85 transition hover:opacity-100"
              >
                <FloatingHeroSprite
                  src={
                    AvatarCatalog.findById(leftPlayer.avatarId).baseImage
                  }
                  alt={leftPlayer.name}
                  accentColor={
                    AvatarCatalog.findById(leftPlayer.avatarId).accentColor
                  }
                  sizeClass="h-24 w-24"
                  delayMs={120}
                />
                <span className="mt-1 max-w-[76px] truncate text-[11px] font-bold text-[#B7ABC8]">
                  {leftPlayer.name}
                </span>
              </button>
            )}

            {centerPlayer && (
              <button
                type="button"
                onClick={() => onCyclePlayerPose(centerPlayer.id)}
                title="Tap to cycle hero stance pose"
                className="mx-3 flex flex-col items-center transition active:scale-95"
              >
                <FloatingHeroSprite
                  src={
                    AvatarCatalog.findById(centerPlayer.avatarId).baseImage
                  }
                  alt={centerPlayer.name}
                  accentColor={
                    AvatarCatalog.findById(centerPlayer.avatarId).accentColor
                  }
                  sizeClass="h-32 w-32"
                />
                <div className="mt-1 flex items-center gap-1.5 rounded-full border border-[#F4CD6C]/50 bg-[#16101E] px-2.5 py-0.5">
                  <span className="max-w-[96px] truncate text-xs font-extrabold text-[#F7F2EC]">
                    {centerPlayer.name}
                  </span>
                  <span className="font-mono-label text-[9px] font-bold text-[#F4CD6C]">
                    {AVATAR_POSE_META[centerPlayer.avatarPose].badgeCode}
                  </span>
                </div>
              </button>
            )}

            {rightPlayer && (
              <button
                type="button"
                onClick={() => setAvatarDeckPlayerId(rightPlayer.id)}
                className="flex flex-col items-center opacity-85 transition hover:opacity-100"
              >
                <FloatingHeroSprite
                  src={
                    AvatarCatalog.findById(rightPlayer.avatarId).baseImage
                  }
                  alt={rightPlayer.name}
                  accentColor={
                    AvatarCatalog.findById(rightPlayer.avatarId).accentColor
                  }
                  sizeClass="h-24 w-24"
                  delayMs={220}
                />
                <span className="mt-1 max-w-[76px] truncate text-[11px] font-bold text-[#B7ABC8]">
                  {rightPlayer.name}
                </span>
              </button>
            )}
          </div>
        )}

        {/* Player Rows */}
        <div className="mt-4 space-y-2.5">
          {state.players.map((player) => {
            const char = AvatarCatalog.findById(player.avatarId);
            const poseMeta = AVATAR_POSE_META[player.avatarPose];
            return (
              <div
                key={player.id}
                className="flex items-center gap-3 rounded-2xl border border-[#423554] bg-[#241C2F] px-3.5 py-2.5"
              >
                <button
                  type="button"
                  onClick={() => setAvatarDeckPlayerId(player.id)}
                  aria-label={`Change avatar for ${player.name}`}
                  className="shrink-0 transition hover:scale-105"
                >
                  <CharacterAvatarThumbnail
                    character={char}
                    pose={player.avatarPose}
                    size={46}
                    showBadgeDot
                  />
                </button>

                <div className="min-w-0 flex-1">
                  <div className="flex items-center gap-2">
                    <span className="truncate font-display text-base font-extrabold text-[#F7F2EC]">
                      {player.name}
                    </span>
                    <button
                      type="button"
                      onClick={() => onCyclePlayerPose(player.id)}
                      className="rounded-lg border border-[#423554] bg-[#16101E] px-2 py-0.5 font-mono-label text-[10px] font-bold text-[#F4CD6C] hover:border-[#F4CD6C]/60"
                    >
                      {poseMeta.badgeCode}
                    </button>
                  </div>
                  <div className="truncate text-[11px] font-medium text-[#887B9B]">
                    {char.name} &bull; {char.title}
                  </div>
                </div>

                <button
                  type="button"
                  onClick={() =>
                    setRenameTarget({ id: player.id, name: player.name })
                  }
                  aria-label={`Rename ${player.name}`}
                  className="rounded-xl p-2 text-[#B7ABC8] hover:bg-[#2C2339] hover:text-[#F4CD6C]"
                >
                  <Edit3 className="h-4 w-4" />
                </button>

                <button
                  type="button"
                  disabled={state.players.length <= 2}
                  onClick={() => onRemovePlayer(player.id)}
                  aria-label={`Remove ${player.name}`}
                  className="rounded-xl p-2 text-[#887B9B] hover:bg-[#341624] hover:text-[#FF3E5B] disabled:cursor-not-allowed disabled:opacity-30"
                >
                  <Trash2 className="h-4 w-4" />
                </button>
              </div>
            );
          })}

          {state.players.length < 16 && (
            <button
              type="button"
              onClick={() => setAddPlayerOpen(true)}
              className="flex h-12 w-full items-center justify-center gap-2 rounded-2xl border border-dashed border-[#F4CD6C]/50 bg-[#241C2F]/60 text-sm font-bold text-[#F4CD6C] transition hover:bg-[#3A2F17]/40"
            >
              <Plus className="h-4 w-4" />
              <span>Add a friend</span>
            </button>
          )}
        </div>

        {/* Sticky Footer CTA */}
        <div className="fixed inset-x-0 bottom-0 z-30 border-t border-[#423554]/80 bg-[#1D1626]/95 px-5 py-4 backdrop-blur-md">
          <div className="mx-auto max-w-xl">
            <GoldCtaButton
              text="Everyone's here"
              onClick={() => onSetSetupStep(SetupWizardStep.RULES)}
            />
          </div>
        </div>

        <AddOrRenamePlayerDialog
          isOpen={addPlayerOpen}
          title="Add a Friend"
          confirmLabel="Add Player"
          placeholder={`Player ${state.players.length + 1}`}
          onConfirm={(name) => {
            onAddPlayer(name);
            setAddPlayerOpen(false);
          }}
          onClose={() => setAddPlayerOpen(false)}
        />

        <AddOrRenamePlayerDialog
          isOpen={Boolean(renameTarget)}
          title="Rename Player"
          confirmLabel="Save Name"
          initialName={renameTarget?.name ?? ''}
          onConfirm={(name) => {
            if (renameTarget) onRenamePlayer(renameTarget.id, name);
            setRenameTarget(null);
          }}
          onClose={() => setRenameTarget(null)}
        />

        <AvatarPickerSheet
          isOpen={avatarDeckPlayerId !== null}
          players={state.players}
          initialPlayerId={avatarDeckPlayerId}
          onSelectAvatar={onSetPlayerAvatar}
          onClose={() => setAvatarDeckPlayerId(null)}
        />
      </div>
    );
  }

  // STEP 4: RULES & SPECIAL ROLES
  if (state.setupStep === SetupWizardStep.RULES) {
    const maxImpostors = Math.max(1, state.players.length - 1);
    const modes: GameMode[] = [
      GameMode.CLASSIC,
      GameMode.SPEED_ROUND,
      GameMode.EXPERT,
    ];
    const timerPresets = [15, 30, 45, 60, 90, 120];

    return (
      <div className="mx-auto flex min-h-screen w-full max-w-xl flex-col px-5 pb-28 pt-5">
        <WizardStepHeader
          title="STEP 3 OF 4 &bull; RULES"
          onBack={() => onSetSetupStep(SetupWizardStep.PLAYERS)}
        />

        <div className="mt-5">
          <HighlightedHeadline prefix="Set the " highlight="table rules" />
          <p className="mt-1.5 text-xs font-medium text-[#B7ABC8]">
            Tune pacing, impostor count, timer, and optional tactical role powers.
          </p>
        </div>

        <div className="mt-5 space-y-4">
          {/* Pace & Style */}
          <div className="rounded-3xl border border-[#423554] bg-[#241C2F] p-4">
            <div className="font-mono-label text-[10px] font-bold uppercase tracking-[0.2em] text-[#F4CD6C]">
              PACE &amp; STYLE
            </div>
            <div className="mt-3 grid grid-cols-1 gap-2.5">
              {modes.map((mode) => {
                const meta = GAME_MODE_META[mode];
                const selected = state.gameMode === mode;
                return (
                  <button
                    key={mode}
                    type="button"
                    onClick={() => onSetGameMode(mode)}
                    className={`flex items-start justify-between gap-3 rounded-2xl border p-3.5 text-left transition ${
                      selected
                        ? 'border-[#F4CD6C] bg-[#3A2F17]/55'
                        : 'border-[#423554] bg-[#16101E] hover:border-[#F4CD6C]/45'
                    }`}
                  >
                    <div>
                      <div className="flex items-center gap-2">
                        <span className="font-display text-base font-extrabold text-[#F7F2EC]">
                          {meta.title}
                        </span>
                        <span className="rounded-md bg-[#2C2339] px-2 py-0.5 font-mono-label text-[10px] font-bold text-[#F4CD6C]">
                          {meta.defaultTurnSeconds}s
                        </span>
                      </div>
                      <p className="mt-1 text-xs leading-relaxed text-[#B7ABC8]">
                        {meta.subtitle}
                      </p>
                    </div>
                    <span
                      className={`mt-1 flex h-5 w-5 shrink-0 items-center justify-center rounded-full border ${
                        selected
                          ? 'border-[#F4CD6C] bg-[#F4CD6C] text-[#1D1626]'
                          : 'border-[#423554]'
                      }`}
                    >
                      {selected && <Check className="h-3.5 w-3.5" strokeWidth={3} />}
                    </span>
                  </button>
                );
              })}
            </div>
          </div>

          {/* Impostors Count */}
          <div className="flex items-center justify-between gap-3 rounded-3xl border border-[#423554] bg-[#241C2F] p-4">
            <div>
              <div className="font-display text-base font-extrabold text-[#F7F2EC]">
                Number of Impostors
              </div>
              <div className="text-xs text-[#887B9B]">
                Max {maxImpostors} for {state.players.length} players
              </div>
            </div>
            <PlumStepperControl
              value={state.impostorCount}
              min={1}
              max={maxImpostors}
              label="impostors"
              accentColor={PALETTE.impostorAccent}
              onDecrement={() => onSetImpostorCount(state.impostorCount - 1)}
              onIncrement={() => onSetImpostorCount(state.impostorCount + 1)}
            />
          </div>

          {/* Turn Timer */}
          <div className="rounded-3xl border border-[#423554] bg-[#241C2F] p-4">
            <div className="flex items-center justify-between gap-3">
              <div className="flex items-center gap-2.5">
                <Clock className="h-5 w-5 text-[#F4CD6C]" />
                <div>
                  <div className="font-display text-base font-extrabold text-[#F7F2EC]">
                    Turn Timer
                  </div>
                  <div className="text-xs text-[#887B9B]">
                    {state.timerEnabled
                      ? `${state.turnDurationSeconds}s per speaker`
                      : 'Free-talk untimed discussion'}
                  </div>
                </div>
              </div>
              <PlumTogglePill
                checked={state.timerEnabled}
                onToggle={onSetTimerEnabled}
                ariaLabel="Toggle turn timer"
              />
            </div>

            {state.timerEnabled && (
              <div className="mt-3 flex flex-wrap gap-1.5">
                {timerPresets.map((sec) => (
                  <button
                    key={sec}
                    type="button"
                    onClick={() => onSetTurnDuration(sec)}
                    className={`rounded-xl border px-3 py-1.5 text-xs font-bold transition ${
                      state.turnDurationSeconds === sec
                        ? 'border-[#F4CD6C] bg-[#F4CD6C] text-[#1D1626]'
                        : 'border-[#423554] bg-[#16101E] text-[#B7ABC8] hover:text-[#F7F2EC]'
                    }`}
                  >
                    {sec}s
                  </button>
                ))}
              </div>
            )}
          </div>

          {/* Clues & Hints Toggles */}
          <div className="space-y-3 rounded-3xl border border-[#423554] bg-[#241C2F] p-4">
            <div className="flex items-center justify-between gap-3">
              <div>
                <div className="font-display text-sm font-extrabold text-[#F7F2EC]">
                  Agent Hints
                </div>
                <div className="text-xs text-[#887B9B]">
                  Show a 1-word clue on Agent cards
                </div>
              </div>
              <PlumTogglePill
                checked={state.hintsEnabled}
                onToggle={onSetHintsEnabled}
                ariaLabel="Toggle agent hints"
              />
            </div>

            <div className="h-px bg-[#423554]/60" />

            <div className="flex items-center justify-between gap-3">
              <div>
                <div className="font-display text-sm font-extrabold text-[#F7F2EC]">
                  Category &amp; Genre Clues
                </div>
                <div className="text-xs text-[#887B9B]">
                  Reveal the word category to everyone including impostors
                </div>
              </div>
              <PlumTogglePill
                checked={state.categoryCluesEnabled}
                onToggle={onSetCategoryCluesEnabled}
                ariaLabel="Toggle category clues"
              />
            </div>
          </div>

          {/* Special Roles */}
          <div className="space-y-3 rounded-3xl border border-[#423554] bg-[#241C2F] p-4">
            <div className="font-mono-label text-[10px] font-bold uppercase tracking-[0.2em] text-[#F4CD6C]">
              SPECIAL TACTICAL ROLES
            </div>

            <div className="flex items-center justify-between gap-3 rounded-2xl border border-[#423554] bg-[#16101E] p-3">
              <div className="flex items-center gap-3">
                <RoleEmblemBadge identity={GameRoleIdentity.AGENT_KILLER} size={40} />
                <div>
                  <div className="font-display text-sm font-extrabold text-[#F7F2EC]">
                    Agent Killer
                  </div>
                  <div className="text-[11px] text-[#887B9B]">
                    Impostor can secretly nullify 1 player&apos;s vote
                  </div>
                </div>
              </div>
              <PlumTogglePill
                checked={state.impostorAbilityEnabled}
                onToggle={onSetImpostorAbilityEnabled}
                accentColor={PALETTE.killerAccent}
                ariaLabel="Toggle Agent Killer"
              />
            </div>

            <div className="flex items-center justify-between gap-3 rounded-2xl border border-[#423554] bg-[#16101E] p-3">
              <div className="flex items-center gap-3">
                <RoleEmblemBadge identity={GameRoleIdentity.SAVIOR_AGENT} size={40} />
                <div>
                  <div className="font-display text-sm font-extrabold text-[#F7F2EC]">
                    Savior Agent
                  </div>
                  <div className="text-[11px] text-[#887B9B]">
                    Shield 1 player&apos;s vote against Killer interference
                  </div>
                </div>
              </div>
              <PlumTogglePill
                checked={state.saviorAgentEnabled}
                onToggle={onSetSaviorAgentEnabled}
                accentColor={PALETTE.saviorAccent}
                ariaLabel="Toggle Savior Agent"
              />
            </div>

            <div className="flex items-center justify-between gap-3 rounded-2xl border border-[#423554] bg-[#16101E] p-3">
              <div className="flex items-center gap-3">
                <RoleEmblemBadge identity={GameRoleIdentity.AGENT_DIRECTOR} size={40} />
                <div>
                  <div className="font-display text-sm font-extrabold text-[#F7F2EC]">
                    Agent Director
                  </div>
                  <div className="text-[11px] text-[#887B9B]">
                    One Agent&apos;s ballot counts with 2x voting weight
                  </div>
                </div>
              </div>
              <PlumTogglePill
                checked={state.agentDirectorEnabled}
                onToggle={onSetAgentDirectorEnabled}
                accentColor={PALETTE.directorAccent}
                ariaLabel="Toggle Agent Director"
              />
            </div>

            <div className="flex items-center justify-between gap-3 rounded-2xl border border-[#423554] bg-[#16101E] p-3">
              <div className="flex items-center gap-3">
                <div className="flex h-10 w-10 items-center justify-center rounded-full border border-[#F4CD6C]/60 bg-[#3A2F17] text-[#F4CD6C]">
                  <Zap className="h-5 w-5" />
                </div>
                <div>
                  <div className="font-display text-sm font-extrabold text-[#F7F2EC]">
                    Blind Suspicion Prediction
                  </div>
                  <div className="text-[11px] text-[#887B9B]">
                    +1 bonus point if an Agent predicts an impostor early
                  </div>
                </div>
              </div>
              <PlumTogglePill
                checked={state.agentPredictionEnabled}
                onToggle={onSetAgentPredictionEnabled}
                ariaLabel="Toggle blind suspicion prediction"
              />
            </div>
          </div>
        </div>

        {/* Sticky Footer CTA */}
        <div className="fixed inset-x-0 bottom-0 z-30 border-t border-[#423554]/80 bg-[#1D1626]/95 px-5 py-4 backdrop-blur-md">
          <div className="mx-auto max-w-xl">
            <GoldCtaButton
              text="Pick your packs"
              onClick={() => onSetSetupStep(SetupWizardStep.PACKS)}
            />
          </div>
        </div>
      </div>
    );
  }

  // STEP 5: WORD PACKS & CUSTOM WORDS
  const peekEntries = peekCategory ? WordBank.wordsForCategory(peekCategory) : [];
  const peekIsCustom = peekCategory ? WordBank.isCustomCategory(peekCategory) : false;

  const handleOpenAddCustomWord = () => {
    setCustomPackTitleInput(wordBankSnapshot.customCategoryName);
    setNewWordText('');
    setNewGenreText('');
    setNewHintText('');
    setAddCustomWordOpen(true);
  };

  const handleSaveCustomWord = (e: React.FormEvent) => {
    e.preventDefault();
    const added = WordBank.addCustomWord(
      newWordText,
      newGenreText,
      newHintText,
      customPackTitleInput
    );
    if (added) {
      onSetCategory(added.category);
      setAddCustomWordOpen(false);
    }
  };

  return (
    <div className="mx-auto flex min-h-screen w-full max-w-xl flex-col px-5 pb-32 pt-5">
      <WizardStepHeader
        title="STEP 4 OF 4 &bull; WORD PACKS"
        onBack={() => onSetSetupStep(SetupWizardStep.RULES)}
      />

      <div className="mt-5">
        <HighlightedHeadline prefix="Choose your " highlight="word pack" />
        <p className="mt-1.5 text-xs font-medium text-[#B7ABC8]">
          Pick any category, peek inside, or craft your own custom table pack.
        </p>
      </div>

      {/* Search + Filters */}
      <div className="mt-4 space-y-3">
        <div className="relative">
          <Search className="pointer-events-none absolute left-3.5 top-1/2 h-4 w-4 -translate-y-1/2 text-[#887B9B]" />
          <input
            type="text"
            value={packSearch}
            onChange={(e) => setPackSearch(e.target.value)}
            placeholder="Search word packs..."
            className="w-full rounded-2xl border border-[#423554] bg-[#241C2F] py-2.5 pl-10 pr-4 text-sm font-medium text-[#F7F2EC] placeholder-[#887B9B] focus:border-[#F4CD6C] focus:outline-none"
          />
        </div>

        <div className="flex flex-wrap items-center justify-between gap-2">
          <div className="flex flex-wrap gap-1.5">
            {(
              [
                ['ALL', 'All'],
                ['BUILTIN', 'Built-in'],
                ['CUSTOM', 'Custom'],
                ['FAVORITES', 'Favorites'],
              ] as Array<[PackFilterTab, string]>
            ).map(([key, label]) => (
              <button
                key={key}
                type="button"
                onClick={() => setPackFilter(key)}
                className={`rounded-xl border px-3 py-1.5 text-xs font-bold transition ${
                  packFilter === key
                    ? 'border-[#F4CD6C] bg-[#F4CD6C] text-[#1D1626]'
                    : 'border-[#423554] bg-[#241C2F] text-[#B7ABC8] hover:text-[#F7F2EC]'
                }`}
              >
                {label}
              </button>
            ))}
          </div>

          <button
            type="button"
            onClick={handleOpenAddCustomWord}
            className="inline-flex items-center gap-1.5 rounded-xl border border-[#F4CD6C]/55 bg-[#3A2F17] px-3 py-1.5 text-xs font-extrabold text-[#FFDE8A] hover:bg-[#4C3D1F]"
          >
            <Plus className="h-3.5 w-3.5" />
            <span>New Word</span>
          </button>
        </div>
      </div>

      {/* Pack Cards */}
      <div className="mt-4 space-y-2.5">
        {filteredCategories.length === 0 ? (
          <div className="rounded-3xl border border-[#423554] bg-[#241C2F] p-6 text-center">
            <p className="text-sm font-bold text-[#F7F2EC]">
              No packs match your current filter.
            </p>
            <p className="mt-1 text-xs text-[#887B9B]">
              Clear your search or switch back to All packs to continue.
            </p>
            <button
              type="button"
              onClick={() => {
                setPackSearch('');
                setPackFilter('ALL');
              }}
              className="mt-4 rounded-xl bg-[#F4CD6C] px-4 py-2 text-xs font-extrabold text-[#1D1626]"
            >
              Show All Packs
            </button>
          </div>
        ) : (
          filteredCategories.map((cat) => {
            const isAll = cat === CATEGORY_ALL;
            const isCustom = WordBank.isCustomCategory(cat);
            const count = WordBank.wordsForCategory(cat).length;
            const selected = state.selectedCategory === cat;
            const isFav = state.favoritePacks.has(cat);

            return (
              <div
                key={cat}
                onClick={() => onSetCategory(cat)}
                role="button"
                tabIndex={0}
                onKeyDown={(e) => {
                  if (e.key === 'Enter' || e.key === ' ') {
                    e.preventDefault();
                    onSetCategory(cat);
                  }
                }}
                className={`flex cursor-pointer items-center justify-between gap-3 rounded-2xl border p-4 transition ${
                  selected
                    ? 'border-[#F4CD6C] bg-[#3A2F17]/50 shadow-[0_0_20px_rgba(244,205,108,0.14)]'
                    : 'border-[#423554] bg-[#241C2F] hover:border-[#F4CD6C]/45'
                }`}
              >
                <div className="flex min-w-0 items-center gap-3">
                  <div
                    className={`flex h-11 w-11 shrink-0 items-center justify-center rounded-2xl border ${
                      selected
                        ? 'border-[#F4CD6C] bg-[#F4CD6C] text-[#1D1626]'
                        : 'border-[#423554] bg-[#16101E] text-[#F4CD6C]'
                    }`}
                  >
                    {isCustom ? (
                      <Crown className="h-5 w-5" />
                    ) : (
                      <Layers className="h-5 w-5" />
                    )}
                  </div>
                  <div className="min-w-0">
                    <div className="flex items-center gap-2">
                      <span className="truncate font-display text-base font-extrabold text-[#F7F2EC]">
                        {isAll ? 'All Categories (Mixed)' : cat}
                      </span>
                      {isCustom && (
                        <span className="rounded-md bg-[#F4CD6C]/20 px-1.5 py-0.5 font-mono-label text-[9px] font-bold uppercase text-[#F4CD6C]">
                          Custom
                        </span>
                      )}
                    </div>
                    <div className="text-xs font-medium text-[#887B9B]">
                      {isAll
                        ? `${count} words across ${WordBank.builtInCategories.length} genres`
                        : isCustom && count === 0
                        ? '0 words — tap New Word above to add'
                        : `${count} word${count === 1 ? '' : 's'}`}
                    </div>
                  </div>
                </div>

                <div
                  className="flex items-center gap-1.5"
                  onClick={(e) => e.stopPropagation()}
                >
                  <button
                    type="button"
                    onClick={() => setPeekCategory(cat)}
                    aria-label={`Peek inside ${cat}`}
                    className="inline-flex items-center gap-1 rounded-xl border border-[#423554] bg-[#16101E] px-2.5 py-1.5 text-[11px] font-bold text-[#B7ABC8] hover:border-[#F4CD6C]/50 hover:text-[#F7F2EC]"
                  >
                    <Eye className="h-3.5 w-3.5" />
                    <span>Peek</span>
                  </button>
                  <button
                    type="button"
                    onClick={() => onToggleFavoritePack(cat)}
                    aria-label={`Favorite ${cat}`}
                    className="rounded-xl p-2 text-[#887B9B] hover:bg-[#16101E]"
                  >
                    <Heart
                      className={`h-4 w-4 ${
                        isFav ? 'fill-[#FF3E5B] text-[#FF3E5B]' : ''
                      }`}
                    />
                  </button>
                  {selected && (
                    <span className="flex h-6 w-6 items-center justify-center rounded-full bg-[#F4CD6C] text-[#1D1626]">
                      <Check className="h-3.5 w-3.5" strokeWidth={3} />
                    </span>
                  )}
                </div>
              </div>
            );
          })
        )}
      </div>

      {/* Sticky Footer */}
      <div className="fixed inset-x-0 bottom-0 z-30 border-t border-[#423554]/80 bg-[#1D1626]/95 px-5 py-4 backdrop-blur-md">
        <div className="mx-auto flex max-w-xl items-center gap-3">
          {state.selectedCategory !== CATEGORY_ALL && (
            <button
              type="button"
              onClick={() => onSetCategory(CATEGORY_ALL)}
              className="h-[54px] shrink-0 rounded-2xl border border-[#423554] bg-[#241C2F] px-4 text-xs font-extrabold text-[#B7ABC8] hover:border-[#F4CD6C]/50 hover:text-[#F7F2EC]"
            >
              Use All
            </button>
          )}
          {WordBank.isCustomCategory(state.selectedCategory) &&
          wordBankSnapshot.customWords.length === 0 ? (
            <GoldCtaButton
              text="Add custom word to start"
              icon={<Plus className="h-5 w-5" />}
              onClick={handleOpenAddCustomWord}
            />
          ) : (
            <GoldCtaButton text="Pass the phone" onClick={onStartRound} />
          )}
        </div>
      </div>

      {/* Pack Peek Modal */}
      {peekCategory && (
        <div className="fixed inset-0 z-50 flex items-end justify-center bg-black/75 backdrop-blur-sm sm:items-center sm:p-4">
          <div className="flex max-h-[85vh] w-full max-w-lg flex-col overflow-hidden rounded-t-3xl border border-[#423554] bg-[#241C2F] shadow-2xl sm:rounded-3xl">
            <div className="flex items-center justify-between border-b border-[#423554] px-5 py-4">
              <div>
                <div className="font-mono-label text-[10px] font-bold uppercase tracking-[0.2em] text-[#F4CD6C]">
                  PACK INSPECTOR
                </div>
                <h3 className="font-display text-lg font-extrabold text-[#F7F2EC]">
                  {peekCategory === CATEGORY_ALL
                    ? 'All Categories'
                    : peekCategory}{' '}
                  ({peekEntries.length})
                </h3>
              </div>
              <button
                type="button"
                onClick={() => setPeekCategory(null)}
                aria-label="Close pack peek"
                className="rounded-xl p-2 text-[#887B9B] hover:bg-[#2C2339] hover:text-[#F7F2EC]"
              >
                <X className="h-5 w-5" />
              </button>
            </div>

            <div className="flex-1 space-y-2.5 overflow-y-auto p-5">
              {peekIsCustom && (
                <div className="mb-3 flex items-center gap-2 rounded-2xl border border-[#423554] bg-[#16101E] p-3">
                  <input
                    type="text"
                    maxLength={30}
                    defaultValue={wordBankSnapshot.customCategoryName}
                    onBlur={(e) => {
                      const renamed = WordBank.setCustomCategoryName(
                        e.target.value
                      );
                      setPeekCategory(renamed);
                      onSetCategory(renamed);
                    }}
                    className="flex-1 bg-transparent text-sm font-bold text-[#F7F2EC] focus:outline-none"
                    placeholder="Custom pack name"
                  />
                  <button
                    type="button"
                    onClick={handleOpenAddCustomWord}
                    className="rounded-xl bg-[#F4CD6C] px-3 py-1.5 text-xs font-extrabold text-[#1D1626]"
                  >
                    + Add
                  </button>
                </div>
              )}

              {peekEntries.length === 0 ? (
                <div className="rounded-2xl border border-[#423554] bg-[#16101E] p-6 text-center text-sm text-[#887B9B]">
                  <p>No words in this pack yet.</p>
                  {peekIsCustom && (
                    <button
                      type="button"
                      onClick={handleOpenAddCustomWord}
                      className="mt-3 rounded-xl bg-[#F4CD6C] px-4 py-2 text-xs font-extrabold text-[#1D1626]"
                    >
                      Add First Word
                    </button>
                  )}
                </div>
              ) : (
                peekEntries.map((entry) => (
                  <div
                    key={entry.word}
                    className="flex items-center justify-between gap-3 rounded-2xl border border-[#423554] bg-[#16101E] px-4 py-3"
                  >
                    <div className="min-w-0">
                      <div className="truncate font-display text-sm font-extrabold text-[#F7F2EC]">
                        {entry.word}
                      </div>
                      <div className="text-[11px] text-[#887B9B]">
                        {entry.genre}
                        {entry.hint ? ` • Clue: ${entry.hint}` : ''}
                      </div>
                    </div>
                    {peekIsCustom && (
                      <button
                        type="button"
                        onClick={() => WordBank.removeCustomWord(entry.word)}
                        aria-label={`Remove ${entry.word}`}
                        className="rounded-lg p-1.5 text-[#887B9B] hover:bg-[#341624] hover:text-[#FF3E5B]"
                      >
                        <Trash2 className="h-4 w-4" />
                      </button>
                    )}
                  </div>
                ))
              )}
            </div>
          </div>
        </div>
      )}

      {/* Add Custom Word Modal */}
      {addCustomWordOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/75 p-4 backdrop-blur-sm">
          <div className="w-full max-w-md rounded-3xl border border-[#423554] bg-[#241C2F] p-6 shadow-2xl">
            <div className="flex items-center justify-between">
              <h3 className="font-display text-xl font-extrabold text-[#F7F2EC]">
                Add Custom Word
              </h3>
              <button
                type="button"
                onClick={() => setAddCustomWordOpen(false)}
                aria-label="Close custom word modal"
                className="rounded-xl p-1.5 text-[#887B9B] hover:text-[#F7F2EC]"
              >
                <X className="h-5 w-5" />
              </button>
            </div>

            <form onSubmit={handleSaveCustomWord} className="mt-4 space-y-3">
              <div>
                <label className="font-mono-label text-[10px] font-bold uppercase tracking-wider text-[#B7ABC8]">
                  Pack Name
                </label>
                <input
                  type="text"
                  maxLength={30}
                  value={customPackTitleInput}
                  onChange={(e) => setCustomPackTitleInput(e.target.value)}
                  placeholder="Inside Jokes"
                  className="mt-1 w-full rounded-2xl border border-[#423554] bg-[#16101E] px-3.5 py-2.5 text-sm font-semibold text-[#F7F2EC] focus:border-[#F4CD6C] focus:outline-none"
                />
              </div>

              <div>
                <label className="font-mono-label text-[10px] font-bold uppercase tracking-wider text-[#B7ABC8]">
                  Secret Word *
                </label>
                <input
                  type="text"
                  autoFocus
                  maxLength={40}
                  value={newWordText}
                  onChange={(e) => setNewWordText(e.target.value)}
                  placeholder="e.g. Midnight Biryani"
                  className="mt-1 w-full rounded-2xl border border-[#423554] bg-[#16101E] px-3.5 py-2.5 text-sm font-semibold text-[#F7F2EC] focus:border-[#F4CD6C] focus:outline-none"
                />
              </div>

              <div className="grid grid-cols-2 gap-2.5">
                <div>
                  <label className="font-mono-label text-[10px] font-bold uppercase tracking-wider text-[#B7ABC8]">
                    Genre (Optional)
                  </label>
                  <input
                    type="text"
                    maxLength={40}
                    value={newGenreText}
                    onChange={(e) => setNewGenreText(e.target.value)}
                    placeholder="Late Night"
                    className="mt-1 w-full rounded-2xl border border-[#423554] bg-[#16101E] px-3.5 py-2.5 text-sm font-semibold text-[#F7F2EC] focus:border-[#F4CD6C] focus:outline-none"
                  />
                </div>
                <div>
                  <label className="font-mono-label text-[10px] font-bold uppercase tracking-wider text-[#B7ABC8]">
                    1-Word Hint (Optional)
                  </label>
                  <input
                    type="text"
                    maxLength={60}
                    value={newHintText}
                    onChange={(e) => setNewHintText(e.target.value)}
                    placeholder="Saffron"
                    className="mt-1 w-full rounded-2xl border border-[#423554] bg-[#16101E] px-3.5 py-2.5 text-sm font-semibold text-[#F7F2EC] focus:border-[#F4CD6C] focus:outline-none"
                  />
                </div>
              </div>

              <div className="flex justify-end gap-2 pt-2">
                <button
                  type="button"
                  onClick={() => setAddCustomWordOpen(false)}
                  className="rounded-2xl border border-[#423554] bg-[#2C2339] px-4 py-2.5 text-sm font-bold text-[#B7ABC8]"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={!newWordText.trim()}
                  className="rounded-2xl bg-[#F4CD6C] px-5 py-2.5 text-sm font-extrabold text-[#1D1626] disabled:opacity-45"
                >
                  Save Word
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
