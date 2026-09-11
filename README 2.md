# Photo Recovery (Android)

Native Android rewrite of the Photo Recovery Flutter/iOS app, built from scratch in Kotlin +
Jetpack Compose. See `../photo_recovery_restore/PROJECT_SUMMARY.md` for the full functional
spec this app was built against, including an explicit list of which features are real
(MediaStore-backed) versus cosmetic/marketing (there is no real "undelete deleted photos"
capability on Android — none of the app's screens claim otherwise in code).

## Project info

- Package name: `com.mobile.photo.recovery.io`
- Language / UI toolkit: Kotlin, Jetpack Compose, Material 3
- Architecture: ViewModel (androidx.lifecycle) per screen, Navigation Compose, Coil for
  MediaStore image loading, Media3 ExoPlayer for video preview, Kotlin coroutines/Flow
- minSdk: 26, targetSdk: 35, compileSdk: 35

## Real features

- Home: real free space (`StatFs`) and photo/video counts (`MediaStore`)
- Photo Recovery: browse all photos/videos, copy selected ones into `Pictures/Restored`
- Quick Swipe Clean: swipe left to delete (system confirmation), right to move into the
  app's private Vault, up to skip, down to go back; real video playback via ExoPlayer
- Duplicate Cleaner: full-file MD5 hashing, grouped by hash, real per-file scan progress
- Screenshot Cleaner: matches album names against known screenshot-folder names
- Secure Vault: app-private file copies + `vault_index.json`, restore/delete — **no
  encryption**, matching the original app's real behavior
- Settings: real app version, share sheet, mailto support, external policy link
- Full i18n: en, vi, es, ja, fr, de, ko, zh, pt (125 keys, consistent across all locales)

The Premium screen is implemented per the design mockups but is intentionally unreachable
from any navigation in the app (registered route only), matching the original app exactly.

## Build

```
./gradlew assembleDebug
```

The debug APK is written to `app/build/outputs/apk/debug/app-debug.apk`.
