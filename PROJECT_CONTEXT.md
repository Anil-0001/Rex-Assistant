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
  - *Tier 0 (Safe Read):* Battery, storage, network telemetry, clock -> execute directly.
  - *Tier 1 (External Launch):* Opening any installed app, opening settings panels -> execute with notice.
  - *Tier 2 (Mutating / Sensitive):* Modifying states, clearing data -> mandatory confirmation prompt dialog modal before execution.
- **Decoupled Architecture:** Separation between the cognitive reasoning layer (SLM / LLM) and the physical Android action executor. AI text output is never directly executed without passing through the validated Action Registry.

---

## 2. Hardware & Cross-Platform Environment

- **Target Phone:** OnePlus Nord CE 2 Lite 5G, Android 14 (API level 34), non-rooted, Snapdragon 695. Tested and verified working.
- **Development Setup 1 (Office):** Ubuntu Linux, 8 GB RAM, OpenJDK 17, Android SDK at `~/Android/Sdk` (Platforms 34, 35, 36; Build-tools 34.0.0, 36.0.0). No Android Studio installed to preserve RAM.
- **Development Setup 2 (Home):** Asus Laptop running **Windows**. Uses `gradlew.bat assembleDebug` and Git clone.
- **Distribution Method (Zero-Cable):** Local HTTP server over Wi-Fi (`python -m http.server 8080 --directory dist`). Phone downloads and installs debug APKs directly in 1-tap via Chrome browser (`http://<PC-IP>:8080`).

---

## 3. Completed Milestones

### ✅ Phase 1: Foundation, Build Pipeline & Wi-Fi Sideloading (100% COMPLETE & VERIFIED)
- Gradle 8.7 wrapper configured with memory caps (`-Xmx2048m`) in `gradle.properties`.
- Modular architecture with `ActionResult.kt`, `AndroidActionExecutor.kt`, and `DeterministicRouter.kt`.
- Initial baseline UI and local Wi-Fi distribution server.

### ✅ Phase 2: Deep Phone Control & Safety Engine (100% COMPLETE & VERIFIED)
- Redesigned Iron Man Crimson Theme (`#FF1A3C`, `#FF8C00`, `#080406`).
- Safety Confirmation Modal (`dialog_confirmation.xml`) for mutating/sensitive actions.
- Internal Storage Telemetry (`StatFs` on `/data`) & Network Telemetry (Wi-Fi vs Cellular 5G).
- Dynamic Installed App Scanner (queries all launchable apps in background).

### ✅ Phase 3: Voice Interaction Pipeline (Listen & Speak) (100% COMPLETE & VERIFIED)
- Offline `RexTextToSpeech` engine with calibrated J.A.R.V.I.S. rate and pitch.
- `RexSpeechRecognizer` tap-to-talk speech recognition with Android 11+ `<queries>` support.
- Reactive pulsing Arc Reactor animations during listening and speaking states.

### ✅ Phase 4: Cognitive AI Brain & Zero-Cost Cloud Reasoning (100% COMPLETE & INTEGRATED)
- **Zero-Cost Cognitive Brain (`GeminiCloudBrain.kt`):** Directly interfaces with Google AI Studio's Gemini 1.5 Flash Free Tier via native HTTPS JSON (0 external SDK dependencies).
- **Structured Tool / Function Calling:** Declares phone actions (`getBatteryStatus`, `getStorageStatus`, `getNetworkStatus`, `openWifiSettings`, `launchAppByName`) to the model. The model automatically emits JSON function calls for physical phone actions.
- **Hybrid Orchestrator (`HybridOrchestrator.kt`):** Seamlessly bridges online cloud reasoning with zero-latency local offline fallback when offline.
- **Secure On-Device Key Storage:** Dialog (`dialog_api_key.xml`) to paste free Gemini API key, stored locally in private SharedPreferences.

---

## 4. Master 7-Phase Roadmap

- **Phase 1: Foundation, Build Pipeline & Wi-Fi Sideloading** *(COMPLETED)*
- **Phase 2: Deep Phone Control & Safety Engine** *(COMPLETED)*
- **Phase 3: Voice Interaction Pipeline (Listen & Speak)** *(COMPLETED)*
- **Phase 4: Cognitive AI Brain & Zero-Cost Cloud Reasoning** *(COMPLETED)*
- **Phase 5: Local Memory & Privacy Dashboard** *(CURRENT NEXT STEP)*
  - Room / SQLite local database for short-term and persistent memory.
  - UI to inspect, edit, and delete memories.
- **Phase 6: Offline Autonomy & Local SLM Exploration**
  - Small model (0.5B-1.5B) benchmarking on Snapdragon 695.
- **Phase 7: Advanced Android Features & Experimental Wake-Word**
  - Foreground service with persistent notification & local wake-word keyword spotter.
