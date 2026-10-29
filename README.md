# Personal Android AI Assistant

A privacy-first, voice-driven personal AI assistant for Android 14, built with clean architecture in Kotlin.

## 🚀 Cross-Platform Support (Ubuntu & Windows)

This repository is configured to build seamlessly on both **Ubuntu Linux** and **Windows (Asus Laptop)** without requiring Android Studio.

---

### 💻 Running on Windows (Home Setup)

1. **Prerequisites:**
   - Git installed (`git --version`)
   - Java 17 installed (`java -version`)
   - Android SDK configured (`ANDROID_HOME` pointing to your SDK folder)

2. **Clone & Open:**
   ```cmd
   git clone <YOUR_GITHUB_REPO_URL>
   cd Assis
   ```

3. **Build the Debug APK on Windows:**
   ```cmd
   gradlew.bat assembleDebug
   ```
   The APK will be generated at:
   `app\build\outputs\apk\debug\app-debug.apk`

4. **Serve to Phone over Wi-Fi:**
   ```cmd
   python -m http.server 8080 --directory dist
   ```

---

### 🐧 Running on Ubuntu Linux (Office Setup)

1. **Build APK:**
   ```bash
   ./gradlew assembleDebug
   ```

2. **Serve to Phone over Wi-Fi:**
   ```bash
   python3 -m http.server 8080 --directory dist
   ```

---

### 🔄 Syncing Work Between Office & Home

- **Before leaving Office (Ubuntu):**
  ```bash
  git add .
  git commit -m "Phase update"
  git push origin main
  ```

- **When reaching Home (Windows):**
  ```cmd
  git pull origin main
  ```
