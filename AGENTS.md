# Antigravity Agent Instructions — R.E.X. (Personal Android AI Assistant)

## Project Identity & Architecture
- **Project:** R.E.X. — Responsive Edge Executor (Iron Man / J.A.R.V.I.S. inspired on-device assistant).
- **Target Device:** OnePlus Nord CE 2 Lite 5G (Snapdragon 695, Android 14 API 34, non-rooted).
- **Aesthetic:** Iron Man Crimson Red (`#FF1A3C`), Ember Gold (`#FF8C00`), and Obsidian Dark (`#080406`).
- **Core Philosophy:** ₹0 budget, privacy-first, on-device action registry, modular architecture.
- **Cross-Platform:** Works on Ubuntu Linux (office) and Windows (Asus laptop at home).

## Current Project State & Master Context
- Full historical context, decisions, and current progress are documented in `PROJECT_CONTEXT.md`.
- **Phase 1 is 100% COMPLETE & VERIFIED on phone.**
- **Phase 2 is 100% COMPLETE & VERIFIED on phone:**
  1. Iron Man Crimson Red theme redesign.
  2. Safety Confirmation Dialog Modal (`dialog_confirmation.xml`).
  3. Dynamic installed app scanner.
  4. Internal Storage telemetry & Network capabilities.
- **Phase 3 is 100% COMPLETE & VERIFIED:**
  1. On-device offline `RexTextToSpeech` (J.A.R.V.I.S. calibrated pitch/rate).
  2. Tap-to-Talk `RexSpeechRecognizer` with Android 11+ `<queries>` support.
  3. Reactive Arc Reactor animations for listening and speaking states.
- **Phase 4 is 100% COMPLETE & INTEGRATED:**
  1. Cognitive AI Brain (`GeminiCloudBrain.kt`) using Google AI Studio Gemini 1.5 Flash Free Tier ($0).
  2. Structured Tool Calling: Automatically maps natural voice prompts to Android actions (`getBatteryStatus`, `getStorageStatus`, `getNetworkStatus`, `openWifiSettings`, `launchAppByName`).
  3. Hybrid Orchestrator (`HybridOrchestrator.kt`): Online AI reasoning with automatic zero-latency offline router fallback.
  4. On-Device API Key dialog modal (`dialog_api_key.xml`) with encrypted SharedPrefs.
- **Current Next Step:** **Phase 5 (Structured Memory & Privacy Dashboard - Room / SQLite Persistence)**.

## Development Rules for Agent:
1. Do not require Android Studio or heavy emulators (preserve 8 GB RAM on host machines).
2. Use Gradle wrapper CLI (`./gradlew` on Linux, `gradlew.bat` on Windows).
3. Do not run any Git commands (the user will manually handle Git in their own terminal).
4. Maintain ₹0 budget (use free tier APIs, open-source models, and local Kotlin Android code).
5. Communicate in friendly Hinglish when collaborating with the user.
