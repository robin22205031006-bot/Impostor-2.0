import { useEffect, useState } from 'react';
import { AvatarCatalog } from './avatarCatalog';

const SCENE_ASSETS: string[] = [
  '/assets/images/img_voting_table_banner.jpg',
  '/assets/images/img_outcome_crew_win.jpg',
  '/assets/images/img_outcome_impostor_win.jpg',
  '/assets/images/img_hero_dossier_1795037633334.jpg',
  '/assets/images/impostor_icon_fg_1795037633004.jpg',
];

const WELCOME_HERO_IDS = ['avatar_01', 'avatar_08', 'avatar_05', 'avatar_09', 'avatar_18'];

function buildOrderedAssetList(): string[] {
  const ordered: string[] = [];
  const seen = new Set<string>();

  const push = (url: string) => {
    if (url && !seen.has(url)) {
      seen.add(url);
      ordered.push(url);
    }
  };

  // 1. Welcome hero lineup (highest priority)
  for (const id of WELCOME_HERO_IDS) {
    push(AvatarCatalog.findById(id).baseImage);
  }

  // 2. All 22 characters' default/base poses
  for (const char of AvatarCatalog.all) {
    push(char.baseImage);
  }

  // 3. Scene banners (voting, crew win, impostor win, dossier, app icon)
  for (const asset of SCENE_ASSETS) {
    push(asset);
  }

  // 4. All alternate stance poses (action, skill, stance) for all 22 heroes
  for (const char of AvatarCatalog.all) {
    push(char.actionImage);
    push(char.skillImage);
    push(char.stanceImage);
  }

  return ordered;
}

export const ALL_PRELOAD_ASSETS: string[] = buildOrderedAssetList();

const decodedImages = new Map<string, HTMLImageElement>();
const inFlightUrls = new Set<string>();
const pendingQueue: string[] = [];
const queuedSet = new Set<string>();
const listeners = new Set<() => void>();

let started = false;
let workersRunning = 0;
const MAX_CONCURRENCY = 6;

export interface AssetPreloadStatus {
  loaded: number;
  total: number;
  ready: boolean;
  percent: number;
}

function getSnapshot(): AssetPreloadStatus {
  const total = ALL_PRELOAD_ASSETS.length;
  const loaded = decodedImages.size;
  return {
    loaded,
    total,
    ready: loaded >= total,
    percent: total === 0 ? 100 : Math.min(100, Math.round((loaded / total) * 100)),
  };
}

function notifyListeners() {
  for (const listener of listeners) {
    listener();
  }
}

function loadAndDecodeImage(url: string): Promise<void> {
  if (decodedImages.has(url)) return Promise.resolve();
  if (typeof window === 'undefined' || typeof Image === 'undefined') {
    return Promise.resolve();
  }

  return new Promise((resolve) => {
    const img = new Image();
    img.decoding = 'async';

    const finish = () => {
      if (typeof img.decode === 'function') {
        img
          .decode()
          .catch(() => undefined)
          .finally(() => {
            decodedImages.set(url, img);
            notifyListeners();
            resolve();
          });
      } else {
        decodedImages.set(url, img);
        notifyListeners();
        resolve();
      }
    };

    img.onload = finish;
    img.onerror = () => {
      // Do not block the queue if a single image fails transiently
      decodedImages.set(url, img);
      notifyListeners();
      resolve();
    };
    img.src = url;

    if (img.complete && img.naturalWidth > 0) {
      finish();
    }
  });
}

function pumpQueue() {
  while (workersRunning < MAX_CONCURRENCY && pendingQueue.length > 0) {
    const nextUrl = pendingQueue.shift();
    if (!nextUrl) continue;
    queuedSet.delete(nextUrl);
    if (decodedImages.has(nextUrl) || inFlightUrls.has(nextUrl)) continue;

    workersRunning++;
    inFlightUrls.add(nextUrl);

    loadAndDecodeImage(nextUrl).finally(() => {
      inFlightUrls.delete(nextUrl);
      workersRunning--;
      pumpQueue();
    });
  }
}

function warmupFonts() {
  if (typeof document === 'undefined' || !('fonts' in document)) return;
  try {
    const families = ['Space Grotesk', 'Plus Jakarta Sans', 'JetBrains Mono'];
    for (const family of families) {
      document.fonts.load(`700 16px "${family}"`).catch(() => undefined);
      document.fonts.load(`800 24px "${family}"`).catch(() => undefined);
    }
  } catch {
    // Ignore font prewarm errors in unsupported environments
  }
}

export function preloadAllGameAssets(): void {
  if (typeof window === 'undefined') return;
  if (!started) {
    started = true;
    warmupFonts();
    for (const url of ALL_PRELOAD_ASSETS) {
      if (!decodedImages.has(url) && !queuedSet.has(url)) {
        pendingQueue.push(url);
        queuedSet.add(url);
      }
    }
  }
  pumpQueue();
}

export function prioritizeAssets(urls: string[]): void {
  if (typeof window === 'undefined') return;
  if (!started) {
    preloadAllGameAssets();
  }

  for (let i = urls.length - 1; i >= 0; i--) {
    const url = urls[i];
    if (!url || decodedImages.has(url) || inFlightUrls.has(url)) continue;
    if (queuedSet.has(url)) {
      const idx = pendingQueue.indexOf(url);
      if (idx !== -1) pendingQueue.splice(idx, 1);
    }
    pendingQueue.unshift(url);
    queuedSet.add(url);
  }
  pumpQueue();
}

export function isAssetDecoded(url: string): boolean {
  return decodedImages.has(url);
}

export function useAssetPreloadStatus(): AssetPreloadStatus {
  const [status, setStatus] = useState<AssetPreloadStatus>(() => getSnapshot());

  useEffect(() => {
    preloadAllGameAssets();
    setStatus(getSnapshot());
    const onChange = () => setStatus(getSnapshot());
    listeners.add(onChange);
    return () => {
      listeners.delete(onChange);
    };
  }, []);

  return status;
}
