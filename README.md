# NBA2K MC Editor (Android source prototype)

Package: `com.moddingtools.nba2kmceditor`

This first prototype is deliberately read-only. It uses Android Storage Access Framework (SAF) to load a user-selected file, calculates its size/SHA-256/byte entropy, shows a short HEX preview, creates a private backup under the app's internal `files/backups/` directory, and can export an unchanged copy through SAF.

## Build
1. Install Android Studio (Ladybug or newer) and Android SDK 35.
2. Open this folder as an existing Gradle project.
3. Allow Gradle sync to download the Android Gradle Plugin/Kotlin plugin.
4. Connect an Android device or create an emulator (Android 8.0/API 26+).
5. Select **Build > Build Bundle(s) / APK(s) > Build APK(s)**.
6. The debug APK is normally at `app/build/outputs/apk/debug/app-debug.apk`.

## Why editing is locked
The two supplied `.CMG.Xue` samples have near-maximum byte entropy and do not expose recognizable plain-text field labels in the inspected regions. The file extension alone is not enough to establish a format. Face IDs, accessory flags, and signature IDs must not be assigned guessed offsets. The next reverse-engineering step is to establish the container/encryption/compression scheme and obtain controlled saves where exactly one in-game setting changes.

## Safety
- The original selected file is never overwritten.
- A private `.bak` copy is created immediately after successful load.
- Export writes an unchanged copy to a location chosen by the user.
- No checksum, header, or field is modified in this prototype.
