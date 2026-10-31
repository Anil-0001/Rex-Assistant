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
- **Phase 2 is 100% COMPLETE & VERIFIED (Build & Tested):**
  1. Iron Man Crimson Red theme redesign (goodbye blue!).
  2. Safety Confirmation Dialog Modal (`dialog_confirmation.xml`) for sensitive/mutating actions.
  3. Dynamic installed app scanner (indexes and launches any installed app: camera, calculator, etc.).
  4. Real-time Internal Storage telemetry (GB free / total) and Network capabilities (Wi-Fi vs Cellular 5G).
- **Phase 3 is 100% COMPLETE & INTEGRATED:**
  1. On-device offline `RexTextToSpeech` (J.A.R.V.I.S. calibrated pitch/rate, emoji/markdown stripping).
  2. Tap-to-Talk `RexSpeechRecognizer` with Android 11+ `<queries>` support and mic scaling.
  3. Reactive Arc Reactor animations for listening and speaking states.
  4. Threading migration to `lifecycleScope.launch(Dispatchers.IO)`.
- **Current Next Step:** **Phase 4 (Cognitive AI Brain & Zero-Cost Cloud Reasoning - Gemini API Structured Tool Calling)**.

## Development Rules for Agent:
1. Do not require Android Studio or heavy emulators (preserve 8 GB RAM on host machines).
2. Use Gradle wrapper CLI (`./gradlew` on Linux, `gradlew.bat` on Windows).
3. Do not run any Git commands (the user will manually handle Git in their own terminal).
4. Maintain ₹0 budget (use free tier APIs, open-source models, and local Kotlin Android code).
5. Communicate in friendly Hinglish when collaborating with the user.
