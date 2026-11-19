# Remix Impostor (Android + Web)

A social-deduction party game built with **React**, **TypeScript**, **Vite**, and **Tailwind CSS**, packaged into a native **Android APK** (`com.aistudio.impostorgame.jpedan`) for direct device installation.

## Game Modes

- **One Phone (Pass & Play)**:
  - 5-step setup wizard: Welcome hero lineup, Mode selection, Player roster (2–16 players) & 22-character **Avatar Deck** (4 stance poses each), Rules & Tactical Special Roles (`Agent Killer`, `Savior Agent`, `Agent Director`, `Blind Suspicion Prediction`), and Word Packs with Pack Inspector & `localStorage` Custom Words.
  - 3D perspective role reveal cards with Web Audio synthesized sound cues.
  - Live speaking arena with circular SVG countdown timer (or Free Talk), private card peeks, Impostor word-steal guess, and Emergency Meeting siren alert.
  - Pass-the-phone secret voting tribunal, dramatic elimination reveal overlay, and multi-round scoreboard.
- **Role Assignment (Quick Deal Lobby)**:
  - Instant role dealing (`Deal`, `Reveal All`, `Hide All`, per-card tap reveal), Category Word Generator, timed discussion with auto-advance, Emergency Meeting alert, secret voter-tab voting, and Round Outcome debrief.

## Install Directly to Android Device

Pre-built APK packages are generated at:
- `apk/Impostor.apk`
- `app/build/outputs/apk/debug/app-debug.apk`

### Option 1: Copy APK to phone
Transfer `apk/Impostor.apk` (or `app/build/outputs/apk/debug/app-debug.apk`) to your Android phone and open it to install.

### Option 2: Install via USB Debugging (`adb`)
```bash
./install-apk.sh        # macOS / Linux
install-apk.bat         # Windows
```

## Build from Source

```bash
npm install
npm run build
./gradlew assembleDebug
cp app/build/outputs/apk/debug/app-debug.apk apk/Impostor.apk
```
