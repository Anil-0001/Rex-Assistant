# R.E.X. (Responsive Edge Executor) — Project Context & Master Memory

> **Notice for AI Assistant / Antigravity Agent:**
> This document is the single source of truth for **R.E.X.** (Personal Android AI Assistant). Whenever this repository is opened on any computer (Ubuntu, Windows Asus Laptop, etc.) or under any Google ID, read this file completely to immediately restore full context, past decisions, architectural patterns, and ongoing roadmap without repeating questions.

---

## 1. Project Vision & Core Principles

- **Identity:** **R.E.X.** (**R**esponsive **E**dge e**X**ecutor) — a privacy-first, voice-driven, modular personal AI assistant natively integrated into an unrooted Android 14 smartphone (**OnePlus Nord CE 2 Lite 5G**, Qualcomm Snapdragon 695).
- **Inspiration:** Fictional sophisticated personal assistants like **J.A.R.V.I.S.** from the Iron Man universe, adapted to real-world hardware, security, and computing realities.
- **Design Aesthetic:** **Iron Man Crimson Red (`#FF1A3C`) & Ember Gold (`#FF8C00`)** with Obsidian Deep Dark (`#080406`) backgrounds and pulsing Arc Reactor core.
- **Strict Budget:** **₹0 target** (Zero-cost). Only free-tier APIs (e.g. Google AI Studio / Gemini API free tier), open-source libraries, and local on-device computation. No mandatory paid subscriptions.
- **Safety & Autonomy Model:** Tiered action security:
  - *Tier 0 (Safe Read):* Battery, storage, network telemetry, clock $\rightarrow$ execute directly.
  - *Tier 1 (External Launch):* Opening any installed app, opening settings panels $\rightarrow$ execute with notice.
  - *Tier 2 (Mutating / Sensitive):* Modifying states, clearing data $\rightarrow$ mandatory confirmation prompt dialog modal before execution.
- **Decoupled Architecture:** Separation between the cognitive reasoning layer (SLM / LLM) and the physical Android action executor. AI text output is never directly executed without passing through the validated Action Registry.

---

## 2. Hardware & Cross-Platform Environment

- **Target Phone:** OnePlus Nord CE 2 Lite 5G, Android 14 (API level 34), non-rooted, Snapdragon 695. Tested and verified working.
- **Development Setup 1 (Office):** Ubuntu Linux, 8 GB RAM, OpenJDK 17, Android SDK at `~/Android/Sdk` (Platforms 34, 35, 36; Build-tools 34.0.0, 36.0.0). No Android Studio installed to preserve RAM.
- **Development Setup 2 (Home):** Asus Laptop running **Windows**. Uses `gradlew.bat assembleDebug` and Git clone.
- **Distribution Method (Zero-Cable):** Local HTTP server over Wi-Fi (`python -m http.server 8080 --directory dist`). Phone downloads and installs debug APKs directly in 1-tap via Chrome browser (`http://<PC-IP>:8080`).

---

## 3. Completed Milestones

### ✅ Phase 1: Foundation, Build Pipeline & Wi-Fi Sideloading (100% COMPLETE & VERIFIED ON PHONE)
- Gradle 8.7 wrapper configured with memory caps (`-Xmx2048m`) in `gradle.properties`.
- Modular architecture with `ActionResult.kt`, `AndroidActionExecutor.kt`, and `DeterministicRouter.kt`.
- Initial baseline UI and local Wi-Fi distribution server.

### ✅ Phase 2: Deep Phone Control & Safety Engine (100% COMPLETE & VERIFIED)
1. **Redesigned Iron Man Crimson Theme:** Completely eliminated the old blue theme. Upgraded to Crimson Red (`#FF1A3C`), Ember Gold (`#FF8C00`), and Obsidian space dark (`#080406`) across all drawables, vector icons, buttons, and layouts.
2. **Safety Confirmation Modal (`dialog_confirmation.xml`):**
   - High-priority security pop-up for mutating/sensitive actions (e.g., data purge/reset).
   - Features Action ID header, risk explanation, "ABORT" button, and "CONFIRM & EXECUTE" button with executable callback.
3. **Internal Storage Telemetry:** Real-time calculation of free and total GB (`StatFs` on `/data`) with percentage and formatted output.
4. **Network Telemetry:** Active network inspection (`ConnectivityManager` & `NetworkCapabilities`) reporting Wi-Fi 📶, Cellular 📡 (5G/4G), or Offline with downstream bandwidth.
5. **Dynamic Installed App Scanner:** Queries all launchable apps on the device in the background, caching them for fast exact and prefix matching (launches Camera, Calculator, WhatsApp, etc.).
6. **New Quick Action Chips:** Battery, Storage, Network, Index Apps, Wi-Fi Panel, and Safety Test demo.

---

## 4. Master 7-Phase Roadmap

- **Phase 1: Foundation, Build Pipeline & Wi-Fi Sideloading** *(COMPLETED)*
- **Phase 2: Deep Phone Control & Safety Engine** *(COMPLETED)*
- **Phase 3: Voice Interaction Pipeline (Listen & Speak)** *(CURRENT NEXT STEP)*
  - Android native offline Text-to-Speech (TTS) for spoken feedback (English + Hindi).
  - Tap-to-Talk Speech-to-Text (STT) integration.
  - Spoken confirmation prompts.
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

When opening this project on the Asus Windows laptop to start **Phase 3**:
1. Verify Git branch is `main` (`git pull origin main`).
2. Build the app using:
   ```cmd
   gradlew.bat assembleDebug
   ```
3. Run the local distribution server:
   ```cmd
   python -m http.server 8080 --directory dist
   ```
4. Start **Phase 3 (Voice Engine: TTS + STT)** directly.
