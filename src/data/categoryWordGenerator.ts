import { GameMode, PlayerRole, PlayerWordBriefing, WordEntry } from '../types/game';
import { CATEGORY_ALL, WordBank, hintForMode } from './wordBank';

export interface GeneratedWordRound {
  entry: WordEntry;
  selectedCategory: string;
  gameMode: GameMode;
  hintsEnabled: boolean;
  categoryCluesEnabled: boolean;
  agentHintText: string;
}

export function createBriefingForRole(
  round: GeneratedWordRound,
  role: PlayerRole
): PlayerWordBriefing {
  const category = round.categoryCluesEnabled ? round.entry.category : null;
  const genre = round.categoryCluesEnabled ? round.entry.genre : null;
  if (role === PlayerRole.IMPOSTOR) {
    return {
      role: PlayerRole.IMPOSTOR,
      secretWord: null,
      agentHint: null,
      category,
      genre,
      isSecretWordHidden: true,
    };
  }
  return {
    role: PlayerRole.NORMAL,
    secretWord: round.entry.word,
    agentHint: round.agentHintText.trim() ? round.agentHintText : null,
    category,
    genre,
    isSecretWordHidden: false,
  };
}

export const CategoryWordGenerator = {
  availableCategories(): string[] {
    return WordBank.categories;
  },

  generate(params: {
    category?: string;
    usedWords?: Set<string>;
    mode?: GameMode;
    hintsEnabled?: boolean;
    categoryCluesEnabled?: boolean;
  } = {}): GeneratedWordRound {
    const {
      category = CATEGORY_ALL,
      usedWords = new Set<string>(),
      mode = GameMode.CLASSIC,
      hintsEnabled = true,
      categoryCluesEnabled = true,
    } = params;

    const picked = WordBank.pickRandomWord(category, usedWords);
    const agentHintText = hintsEnabled ? hintForMode(picked, mode) : '';

    return {
      entry: picked,
      selectedCategory: category,
      gameMode: mode,
      hintsEnabled,
      categoryCluesEnabled,
      agentHintText,
    };
  },
};
