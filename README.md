# ChargeGuard – Battery Charger Alarm
**Created for: muthu krishnann**
**Package: `com.muthukrishnann.chargeguard`**

ChargeGuard is a high-performance native Android application engineered to safeguard device battery health by providing real-time hardware battery telemetry, intelligent cutoff alarms (80%, 85%, 90%, 95%, 100%), background foreground monitoring, and charging analytics.

---

## ⚡ Core Features
- **Real Hardware Battery Telemetry**: Reads actual percentage, charging state (AC, USB, Wireless), voltage, temperature, and battery health using Android `BatteryManager`.
- **Animated Circular Battery Gauge**: Custom Canvas-rendered indicator with dynamic color gradients and charging pulse animations.
- **Selectable Cutoff Limits**: Instant selection chips for 80%, 85%, 90%, 95%, and 100% full charge.
- **Dual Alarm Triggers**:
  - Threshold reached alert (e.g. 85%)
  - Full capacity alert (100%)
- **Prominent Full-Screen Alert**: Overrides lockscreen and other apps with a high-visibility STOP ALARM action.
- **Uninterrupted Background Monitoring**: Foreground Service with sticky broadcast receiver and persistent status notification.
- **Charging Session Analytics**: Automatic Room database logging of start/end timestamps, start/end percentages, peak percentage, and duration.
- **Customizable Settings**: Sound selection, volume slider, vibration toggle, notifications toggle, theme toggle (Dark / Light / System), and battery saver exemption helper.

---

## 📲 How to Test on Your Android Phone

### Option A: Zero-Code Cloud Build via GitHub (Recommended for Instant APK)
You do not need to install Android Studio or write any code:
1. Upload this project folder or import this repository into a new GitHub repository.
2. Click the **Actions** tab on your GitHub repo.
3. The included workflow `.github/workflows/build-apk.yml` will automatically build the app.
4. Click on the latest workflow run and download **`ChargeGuard-muthukrishnann-APK`**.
5. Transfer the `.apk` file to your Android phone via WhatsApp, Telegram, Google Drive, or USB cable.
6. Tap the APK file on your phone and tap **Install** (if prompted, allow 'Install unknown apps' for your browser or file manager).

### Option B: 1-Click Run in Android Studio
1. Download and open [Android Studio](https://developer.android.com/studio).
2. Click **Open** and select the unzipped `ChargeGuard` directory.
3. Wait 1-2 minutes for Gradle sync to complete.
4. Enable **Developer Options** and **USB Debugging** on your Android phone:
   - Go to phone **Settings > About Phone**.
   - Tap **Build Number** 7 times until it says *"You are now a developer"*.
   - Go to **Settings > System > Developer Options** and enable **USB Debugging**.
5. Connect your phone with a USB cable.
6. Select your phone in the Android Studio top toolbar and click the green **Play (Run)** button ▶️.
7. The app will install and open immediately on your phone!

### Option C: Build APK via Terminal
```bash
./gradlew assembleDebug
# The APK will be generated at:
# app/build/outputs/apk/debug/app-debug.apk
```
