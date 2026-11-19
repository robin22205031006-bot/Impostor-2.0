import { useEffect, useState } from 'react';
import rawWordsJson from './words.json';
import { CryptoRandom } from './cryptoRandom';
import { LOCATION_WORDS } from './locationWordRepository';
import { GameMode, WordEntry } from '../types/game';

export const CATEGORY_ALL = 'ALL';
export const CATEGORY_CUSTOM = 'Custom';

const KEY_CUSTOM_WORDS = 'impostor_custom_words_json';
const KEY_CUSTOM_CATEGORY_NAME = 'impostor_custom_category_name';

const FALLBACK_ENTRIES: WordEntry[] = [
  { word: 'Fuchka', category: 'Food', genre: 'Savory', hint: 'Hollow', expertHint: 'Crackle' },
  { word: 'Kacchi', category: 'Food', genre: 'Banquet', hint: 'Seal', expertHint: 'Layer' },
  { word: 'Singara', category: 'Food', genre: 'Pastry', hint: 'Fold', expertHint: 'Crust' },
  { word: 'Jilapi', category: 'Food', genre: 'Confection', hint: 'Coil', expertHint: 'Glaze' },
  { word: 'Khichuri', category: 'Food', genre: 'Comfort', hint: 'Pot', expertHint: 'Thunder' },
  { word: 'Panta', category: 'Food', genre: 'Tradition', hint: 'Clay', expertHint: 'Dawn' },
  { word: 'Cha', category: 'Food', genre: 'Brew', hint: 'Leaf', expertHint: 'Ceramic' },
  { word: 'Rosogolla', category: 'Food', genre: 'Sweet', hint: 'Sponge', expertHint: 'Curd' },
  { word: 'Mango', category: 'Food', genre: 'Orchard', hint: 'Pit', expertHint: 'Sap' },
  { word: 'Jackfruit', category: 'Food', genre: 'Harvest', hint: 'Resin', expertHint: 'Rind' },
  { word: 'Haleem', category: 'Food', genre: 'Stew', hint: 'Grain', expertHint: 'Dusk' },
  { word: 'Muri', category: 'Food', genre: 'Crunch', hint: 'Hull', expertHint: 'Toss' },
  { word: 'Bakorkhani', category: 'Food', genre: 'Bakery', hint: 'Flake', expertHint: 'Alley' },
  { word: 'Pizza', category: 'Food', genre: 'Cuisine', hint: 'Crust', expertHint: 'Box' },
  { word: 'Sushi', category: 'Food', genre: 'Cuisine', hint: 'Roll', expertHint: 'Vinegar' },
  { word: 'Burger', category: 'Food', genre: 'Cuisine', hint: 'Stack', expertHint: 'Sesame' },
  { word: 'Boba', category: 'Food', genre: 'Beverage', hint: 'Straw', expertHint: 'Pearl' },
  { word: 'Coffee', category: 'Food', genre: 'Beverage', hint: 'Roast', expertHint: 'Grind' },
  { word: 'Tiger', category: 'Nature', genre: 'Predator', hint: 'Paw', expertHint: 'Dusk' },
  { word: 'Ilish', category: 'Nature', genre: 'Catch', hint: 'Bones', expertHint: 'Tide' },
  { word: 'Doel', category: 'Nature', genre: 'Avian', hint: 'Note', expertHint: 'Branch' },
  { word: 'Crow', category: 'Nature', genre: 'Avian', hint: 'Wire', expertHint: 'Echo' },
  { word: 'Cobra', category: 'Nature', genre: 'Reptile', hint: 'Hiss', expertHint: 'Reed' },
  { word: 'Kingfisher', category: 'Nature', genre: 'Avian', hint: 'Dive', expertHint: 'Flash' },
  { word: 'Dolphin', category: 'Nature', genre: 'Aquatic', hint: 'Echo', expertHint: 'Wake' },
  { word: 'Jackal', category: 'Nature', genre: 'Fauna', hint: 'Howl', expertHint: 'Stalk' },
  { word: 'Goat', category: 'Nature', genre: 'Livestock', hint: 'Tether', expertHint: 'Horn' },
  { word: 'Sundarbans', category: 'Nature', genre: 'Wetland', hint: 'Silt', expertHint: 'Roots' },
  { word: 'Monsoon', category: 'Nature', genre: 'Climate', hint: 'Squall', expertHint: 'Tin' },
  { word: 'Banyan', category: 'Nature', genre: 'Botany', hint: 'Shade', expertHint: 'Trunk' },
  { word: 'Pohela Boishakh', category: 'Culture', genre: 'Festival', hint: 'Procession', expertHint: 'Chalk' },
  { word: 'Shaheed Minar', category: 'Culture', genre: 'Memorial', hint: 'Pillar', expertHint: 'Petal' },
  { word: 'Eid', category: 'Culture', genre: 'Celebration', hint: 'Sighting', expertHint: 'Feast' },
  { word: 'Jamdani', category: 'Culture', genre: 'Textile', hint: 'Thread', expertHint: 'Loom' },
  { word: 'Nakshi Kantha', category: 'Culture', genre: 'Craft', hint: 'Patch', expertHint: 'Quilt' },
  { word: 'Boi Mela', category: 'Culture', genre: 'Gathering', hint: 'Spine', expertHint: 'Ink' },
  { word: 'Baul', category: 'Culture', genre: 'Music', hint: 'String', expertHint: 'Wanderer' },
  { word: 'Cricket', category: 'Culture', genre: 'Sport', hint: 'Over', expertHint: 'Crease' },
  { word: 'Carrom', category: 'Culture', genre: 'Tabletop', hint: 'Pocket', expertHint: 'Powder' },
  { word: 'Ludo', category: 'Culture', genre: 'Tabletop', hint: 'Home', expertHint: 'Roll' },
  { word: 'Kabaddi', category: 'Culture', genre: 'Sport', hint: 'Breath', expertHint: 'Line' },
  { word: 'Rickshaw Art', category: 'Culture', genre: 'Visual', hint: 'Enamel', expertHint: 'Motif' },
  ...LOCATION_WORDS,
];

function parseBuiltInWords(): WordEntry[] {
  try {
    if (!Array.isArray(rawWordsJson) || rawWordsJson.length === 0) {
      return FALLBACK_ENTRIES;
    }
    const parsed: WordEntry[] = [];
    for (const item of rawWordsJson as Array<Partial<WordEntry>>) {
      const word = (item.word ?? '').trim();
      const category = (item.category ?? '').trim();
      const genre = (item.genre ?? '').trim();
      const hint = (item.hint ?? '').trim();
      const expertHint = (item.expertHint ?? '').trim();
      if (word && category) {
        parsed.push({
          word,
          category,
          genre: genre || category,
          hint,
          expertHint: expertHint || hint,
        });
      }
    }
    return parsed.length > 0 ? parsed : FALLBACK_ENTRIES;
  } catch {
    return FALLBACK_ENTRIES;
  }
}

const BUILT_IN_ENTRIES: WordEntry[] = parseBuiltInWords();

function loadInitialCustomCategoryName(): string {
  try {
    if (typeof window === 'undefined') return CATEGORY_CUSTOM;
    const saved = window.localStorage.getItem(KEY_CUSTOM_CATEGORY_NAME);
    const clean = (saved ?? '').trim().slice(0, 30);
    return clean || CATEGORY_CUSTOM;
  } catch {
    return CATEGORY_CUSTOM;
  }
}

function loadInitialCustomWords(categoryName: string): WordEntry[] {
  try {
    if (typeof window === 'undefined') return [];
    const raw = window.localStorage.getItem(KEY_CUSTOM_WORDS);
    if (!raw) return [];
    const arr = JSON.parse(raw) as Array<Partial<WordEntry>>;
    if (!Array.isArray(arr)) return [];
    const list: WordEntry[] = [];
    for (const obj of arr) {
      const word = (obj.word ?? '').trim();
      const genre = (obj.genre ?? '').trim();
      const hint = (obj.hint ?? '').trim();
      if (word) {
        list.push({
          word,
          category: categoryName,
          genre: genre || categoryName,
          hint,
          expertHint: hint,
        });
      }
    }
    return list;
  } catch {
    return [];
  }
}

let customCategoryNameState = loadInitialCustomCategoryName();
let customWordsState: WordEntry[] = loadInitialCustomWords(customCategoryNameState);
const listeners = new Set<() => void>();

function notifyListeners() {
  listeners.forEach((cb) => cb());
}

function saveCustomWords(list: WordEntry[], categoryName: string) {
  try {
    if (typeof window === 'undefined') return;
    window.localStorage.setItem(KEY_CUSTOM_CATEGORY_NAME, categoryName);
    const serialized = list.map((entry) => ({
      word: entry.word,
      genre: entry.genre,
      hint: entry.hint,
    }));
    window.localStorage.setItem(KEY_CUSTOM_WORDS, JSON.stringify(serialized));
  } catch {
    // Ignore storage quota errors
  }
}

export function hintForMode(entry: WordEntry, mode: GameMode): string {
  return mode === GameMode.EXPERT ? entry.expertHint : entry.hint;
}

export const WordBank = {
  CATEGORY_ALL,
  CATEGORY_CUSTOM,

  get customCategoryName(): string {
    return customCategoryNameState;
  },

  get customEntries(): WordEntry[] {
    return customWordsState;
  },

  get entries(): WordEntry[] {
    return [...BUILT_IN_ENTRIES, ...customWordsState];
  },

  get builtInCount(): number {
    return BUILT_IN_ENTRIES.length;
  },

  get builtInCategories(): string[] {
    const unique = new Set(BUILT_IN_ENTRIES.map((e) => e.category));
    return Array.from(unique).sort((a, b) => a.localeCompare(b));
  },

  get categories(): string[] {
    const all = [CATEGORY_ALL, ...this.builtInCategories, customCategoryNameState];
    return Array.from(new Set(all));
  },

  isCustomCategory(category: string): boolean {
    const trimmed = category.trim().toLowerCase();
    return (
      trimmed === CATEGORY_CUSTOM.toLowerCase() ||
      trimmed === customCategoryNameState.trim().toLowerCase()
    );
  },

  setCustomCategoryName(rawName: string): string {
    const cleanName = rawName.trim().slice(0, 30) || CATEGORY_CUSTOM;
    const previousName = customCategoryNameState;
    customCategoryNameState = cleanName;
    customWordsState = customWordsState.map((entry) => ({
      ...entry,
      category: cleanName,
      genre: entry.genre === previousName ? cleanName : entry.genre,
    }));
    saveCustomWords(customWordsState, cleanName);
    notifyListeners();
    return cleanName;
  },

  addCustomWord(
    rawWord: string,
    rawGenre: string,
    rawHint: string,
    rawCategoryName?: string
  ): WordEntry | null {
    const word = rawWord.trim().slice(0, 40);
    if (!word) return null;
    const activeCategory =
      rawCategoryName && rawCategoryName.trim()
        ? this.setCustomCategoryName(rawCategoryName)
        : customCategoryNameState;
    const genre = rawGenre.trim().slice(0, 40) || activeCategory;
    const hint = rawHint.trim().slice(0, 60);
    const newEntry: WordEntry = {
      word,
      category: activeCategory,
      genre,
      hint,
      expertHint: hint,
    };
    customWordsState = [
      ...customWordsState.filter((e) => e.word.toLowerCase() !== word.toLowerCase()),
      newEntry,
    ];
    saveCustomWords(customWordsState, activeCategory);
    notifyListeners();
    return newEntry;
  },

  removeCustomWord(word: string): void {
    customWordsState = customWordsState.filter(
      (e) => e.word.toLowerCase() !== word.toLowerCase()
    );
    saveCustomWords(customWordsState, customCategoryNameState);
    notifyListeners();
  },

  pickRandomWord(selectedCategory: string, usedWords: Set<string> = new Set()): WordEntry {
    const pool = this.entries;
    let categoryPool: WordEntry[];
    if (selectedCategory === CATEGORY_ALL) {
      categoryPool = pool;
    } else if (this.isCustomCategory(selectedCategory)) {
      categoryPool = customWordsState.length > 0 ? customWordsState : pool;
    } else {
      const matched = pool.filter(
        (e) => e.category.toLowerCase() === selectedCategory.toLowerCase()
      );
      categoryPool = matched.length > 0 ? matched : pool;
    }

    const freshPool = categoryPool.filter((e) => !usedWords.has(e.word));
    const finalPool = freshPool.length > 0 ? freshPool : categoryPool;
    return CryptoRandom.pickOne(finalPool) ?? finalPool[0];
  },

  wordsForCategory(category: string): WordEntry[] {
    if (category === CATEGORY_ALL) return this.entries;
    if (this.isCustomCategory(category)) return customWordsState;
    return this.entries.filter((e) => e.category.toLowerCase() === category.toLowerCase());
  },

  isGuessMatch(guess: string, secretWord: string): boolean {
    const normalizedGuess = guess.trim().toLowerCase().replace(/[^a-z0-9]/g, '');
    const normalizedTarget = secretWord.trim().toLowerCase().replace(/[^a-z0-9]/g, '');
    if (!normalizedGuess || !normalizedTarget) return false;
    return normalizedGuess === normalizedTarget;
  },
};

export function useWordBank() {
  const [snapshot, setSnapshot] = useState(() => ({
    customCategoryName: WordBank.customCategoryName,
    customWords: WordBank.customEntries,
    categories: WordBank.categories,
  }));

  useEffect(() => {
    const update = () => {
      setSnapshot({
        customCategoryName: WordBank.customCategoryName,
        customWords: WordBank.customEntries,
        categories: WordBank.categories,
      });
    };
    listeners.add(update);
    return () => {
      listeners.delete(update);
    };
  }, []);

  return snapshot;
}
