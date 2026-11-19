# Impostor — Android Party Deduction Game

Standalone offline Android APK built with Kotlin & Jetpack Compose.  
**100% Free & Offline — zero Firebase, zero accounts, zero API keys, and zero paid services.**

## Instant Install on Your Android Phone (via Git / GitHub)
A ready-to-install signed debug APK is already included right inside this repository at:
- **[`apk/Impostor.apk`](./apk/Impostor.apk)**

### Install Directly on Phone (No PC Required)
1. Clone or open this repository on GitHub from your phone.
2. Open **[`apk/Impostor.apk`](./apk/Impostor.apk)** and tap **Download** (or View raw).
3. Tap the downloaded `Impostor.apk` file on your Android phone to install it (enable *Install unknown apps* if prompted).

### Install in 1 Command via Git + ADB (Linux / macOS / Termux / Windows)
```bash
git clone <your-repo-url>
cd Impostor
```
- **Linux / macOS / Termux**: `./install-apk.sh` (installs the prebuilt `apk/Impostor.apk` immediately if no build is needed, or pass `--rebuild` to recompile from source)
- **Windows**: `install-apk.bat`

### Build From Source Yourself (Optional)
Requirements: **JDK 17** + **Android SDK**
```bash
./gradlew assembleDebug
```
The compiled APK is output to `app/build/outputs/apk/debug/app-debug.apk`.

### Pushing Updates Back to GitHub
If pushing back to an existing repository that previously contained `.github/workflows/build-apk.yml` fails from an OAuth integration (or if remote history diverged):
1. Push to a **new repository/branch** from the export UI, **or**
2. Download the project ZIP, extract it into your local `git clone` of the repository (keeping `.git/` intact), and push using `git push` from your terminal.
3. Optional: to re-enable GitHub Actions APK releases from a local Git push, copy `docs/build-apk-workflow.yml` to `.github/workflows/build-apk.yml`.

