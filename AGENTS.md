# Antigravity Agent Instructions — R.E.X. (Personal Android AI Assistant)

## Project Identity & Architecture
- **Project:** R.E.X. — Responsive Edge Executor (Iron Man / J.A.R.V.I.S. inspired on-device assistant).
- **Target Device:** OnePlus Nord CE 2 Lite 5G (Snapdragon 695, Android 14 API 34, non-rooted).
- **Core Philosophy:** ₹0 budget, privacy-first, on-device action registry, modular architecture.
- **Cross-Platform:** Works on Ubuntu Linux (office) and Windows (Asus laptop at home).

## Current Project State & Master Context
- Full historical context, decisions, and current progress are documented in `PROJECT_CONTEXT.md`.
- **Phase 1 is 100% COMPLETE & VERIFIED on phone** (tested and working on the OnePlus phone).
- **Current Next Step:** **Phase 2 (Deep Phone Control & Safety Engine)**:
  1. Confirmation dialog modal for sensitive actions.
  2. Dynamic installed app scanner.
  3. Storage & Network telemetry.

## Development Rules for Agent:
1. Do not require Android Studio or heavy emulators (preserve 8 GB RAM on host machines).
2. Use Gradle wrapper CLI (`./gradlew` on Linux, `gradlew.bat` on Windows).
3. Do not run any Git commands (the user will manually handle Git in their own terminal).
4. Maintain ₹0 budget (use free tier APIs, open-source models, and local Kotlin Android code).
5. Communicate in friendly Hinglish when collaborating with the user.
