import { WordEntry } from '../types/game';
import { CryptoRandom } from './cryptoRandom';

export const LOCATION_WORDS: WordEntry[] = [
  { word: 'Space Station', category: 'Locations', genre: 'Sci-Fi', hint: 'Orbit', expertHint: 'Airlock' },
  { word: 'Submarine', category: 'Locations', genre: 'Maritime', hint: 'Periscope', expertHint: 'Depth' },
  { word: 'Hospital', category: 'Locations', genre: 'Civic', hint: 'Ward', expertHint: 'Pulse' },
  { word: 'Casino', category: 'Locations', genre: 'Nightlife', hint: 'Chips', expertHint: 'Velvet' },
  { word: 'Airport', category: 'Locations', genre: 'Transit', hint: 'Terminal', expertHint: 'Runway' },
  { word: 'Pirate Ship', category: 'Locations', genre: 'Adventure', hint: 'Mast', expertHint: 'Plank' },
  { word: 'Art Museum', category: 'Locations', genre: 'Culture', hint: 'Gallery', expertHint: 'Frame' },
  { word: 'Movie Theater', category: 'Locations', genre: 'Entertainment', hint: 'Projector', expertHint: 'Popcorn' },
  { word: 'Ski Resort', category: 'Locations', genre: 'Alpine', hint: 'Gondola', expertHint: 'Powder' },
  { word: 'Train Station', category: 'Locations', genre: 'Transit', hint: 'Platform', expertHint: 'Whistle' },
  { word: 'Volcano Lab', category: 'Locations', genre: 'Science', hint: 'Magma', expertHint: 'Sensor' },
  { word: 'Cruise Ship', category: 'Locations', genre: 'Voyage', hint: 'Deck', expertHint: 'Horizon' },
  { word: 'University Campus', category: 'Locations', genre: 'Academic', hint: 'Quad', expertHint: 'Lecture' },
  { word: 'Police Station', category: 'Locations', genre: 'Civic', hint: 'Precinct', expertHint: 'Badge' },
  { word: 'Amusement Park', category: 'Locations', genre: 'Leisure', hint: 'Coaster', expertHint: 'Ticket' },
  { word: 'Desert Oasis', category: 'Locations', genre: 'Nature', hint: 'Mirage', expertHint: 'Palm' },
  { word: 'Underground Bunker', category: 'Locations', genre: 'Stealth', hint: 'Hatch', expertHint: 'Ration' },
  { word: 'Botanical Garden', category: 'Locations', genre: 'Nature', hint: 'Greenhouse', expertHint: 'Pollen' },
  { word: 'Film Studio', category: 'Locations', genre: 'Media', hint: 'Clapper', expertHint: 'Rig' },
  { word: 'Coral Reef', category: 'Locations', genre: 'Ocean', hint: 'Lagoon', expertHint: 'Snorkel' },
];

export const LocationWordRepository = {
  getAllLocations(): WordEntry[] {
    return LOCATION_WORDS;
  },

  pickRandomLocationWord(excludeWords: Set<string> = new Set()): WordEntry {
    const fresh = LOCATION_WORDS.filter((entry) => !excludeWords.has(entry.word));
    const pool = fresh.length > 0 ? fresh : LOCATION_WORDS;
    return CryptoRandom.pickOne(pool) ?? pool[0];
  },
};
