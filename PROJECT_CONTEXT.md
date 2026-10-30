# R.E.X. (Responsive Edge Executor) — Project Context & Master Memory

> **Notice for AI Assistant / Antigravity Agent:**
> This document is the single source of truth for **R.E.X.** (Personal Android AI Assistant). Whenever this repository is opened on any computer (Ubuntu, Windows Asus Laptop, etc.) or under any Google ID, read this file completely to immediately restore full context, past decisions, architectural patterns, and ongoing roadmap without repeating questions.

---

## 1. Project Vision & Core Principles

- **Identity:** **R.E.X.** (**R**esponsive **E**dge e**X**ecutor) — a privacy-first, voice-driven, modular personal AI assistant natively integrated into an unrooted Android 14 smartphone (**OnePlus Nord CE 2 Lite 5G**, Qualcomm Snapdragon 695).
- **Inspiration:** Fictional sophisticated personal assistants like **J.A.R.V.I.S.** from the Iron Man universe, adapted to real-world hardware, security, and computing realities.
- **Strict Budget:** **₹0 target** (Zero-cost). Only free-tier APIs (e.g. Google AI Studio / Gemini API free tier), open-source libraries, and local on-device computation. No mandatory paid subscriptions.
- **Safety & Autonomy Model:** Tiered action security:
  - *Tier 0 (Safe Read):* Battery, time, telemetry $\rightarrow$ execute directly.
  - *Tier 1 (External Launch):* Opening installed apps, opening settings panels $\rightarrow$ execute with notice.
  - *Tier 2 (Mutating / Sensitive):* Modifying states, clearing data $\rightarrow$ mandatory confirmation prompt dialog before execution.
- **Decoupled Architecture:** Separation between the cognitive reasoning layer (SLM / LLM) and the physical Android action executor. AI text output is never directly executed without passing through the validated Action Registry.

---

## 2. Hardware & Cross-Platform Environment

- **Target Phone:** OnePlus Nord CE 2 Lite 5G, Android 14 (API level 34), non-rooted, Snapdragon 695. Tested and verified working.
- **Development Setup 1 (Office):** Ubuntu Linux, 8 GB RAM, OpenJDK 17, Android SDK at `~/Android/Sdk` (Platforms 34, 35, 36; Build-tools 34.0.0, 36.0.0). No Android Studio installed to preserve RAM.
- **Development Setup 2 (Home):** Asus Laptop running **Windows**. Uses `gradlew.bat assembleDebug` and Git clone.
- **Distribution Method (Zero-Cable):** Local HTTP server over Wi-Fi (`python -m http.server 8080 --directory dist`). Phone downloads and installs debug APKs directly in 1-tap via Chrome browser (`http://<PC-IP>:8080`).

---

## 3. Completed Milestones — Phase 1 (100% Tested & Verified)

Phase 1 has been built, compiled, sideloaded, and successfully verified on the user's real OnePlus device:

1. **Lightweight Gradle Wrapper:** Gradle 8.7 wrapper configured with memory caps (`-Xmx2048m`) in `gradle.properties` to ensure smooth performance on 8 GB RAM systems.
2. **Modular Architecture:**
   - `app/src/main/java/com/personal/assistant/core/actions/ActionResult.kt`: Type-safe action results (`Success`, `RequiresConfirmation`, `Error`) and safety tiers.
   - `app/src/main/java/com/personal/assistant/core/actions/AndroidActionExecutor.kt`: Direct, legitimate Android 14 intents for Battery level & charging state, Internet/Wi-Fi settings panel, Bluetooth settings, device hardware telemetry, and installed launcher app execution.
   - `app/src/main/java/com/personal/assistant/core/router/DeterministicRouter.kt`: Zero-latency local command parser supporting English and Hinglish patterns (`"battery kitni hai"`, `"youtube chalao"`, `"wifi setting"`, `"kitne baje hain"`, etc.).
3. **Futuristic Dark UI:**
   - Theme `#0A0E17` with neon cyan `#00F2FE` and electric blue `#4FACFE` accents.
   - Pulsing animated center Orb, real-time live response card, quick action chips, manual command input field, and glowing mic action button.
   - Runtime Android 14 microphone permission handling.
4. **Build & Distribution:**
   - `./gradlew assembleDebug` produces clean 5.8 MB APK (`app-debug.apk`).
   - `dist/index.html` provides a mobile-responsive download portal.

---

## 4. Master 7-Phase Roadmap

- **Phase 1: Foundation, Build Pipeline & Wi-Fi Sideloading** *(COMPLETED & VERIFIED ON PHONE)*
- **Phase 2: Deep Phone Control & Safety Engine** *(CURRENT NEXT STEP)*
  - Custom UI Confirmation Modal for sensitive actions.
  - Dynamic App Scanner (indexing all installed user/system apps dynamically).
  - Storage & Network Telemetry (Free GB available / total, Wi-Fi SSID vs 5G).
- **Phase 3: Voice Interaction Pipeline (Listen & Speak)**
  - Android native offline Text-to-Speech (TTS) for spoken feedback (English + Hindi).
  - Tap-to-Talk Speech-to-Text (STT) integration.
- **Phase 4: Cognitive AI Brain & Zero-Cost Cloud Reasoning**
  - Google AI Studio (Gemini free tier) integration with structured JSON tool-calling.
  - Natural language conversational reasoning mapped to the Action Registry.
- **Phase 5: Local Memory & Privacy Dashboard**
  - Room / SQLite local database for short-term and persistent memory.
  - UI to inspect, edit, and delete memories.
- **Phase 6: Offline Autonomy & Local SLM Exploration**
  - Comprehensive offline fallback rules.
  - Small model (0.5B–1.5B) benchmarking on Snapdragon 695.
- **Phase 7: Advanced Android Features & Experimental Wake-Word**
  - Foreground service with persistent notification.
  - Local keyword spotter (wake-word) experimentation.

---

## 5. Instructions for Windows (Asus Laptop) Session Resume

When opening this project on the Asus Windows laptop:
1. Verify Git branch is `main` (`git pull origin main`).
2. Build the app using:
   ```cmd
   gradlew.bat assembleDebug
   ```
3. Run the local distribution server:
   ```cmd
   python -m http.server 8080 --directory dist
   ```
4. Continue directly with **Phase 2** implementation.
