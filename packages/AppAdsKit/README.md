# AppAdsKit

Shared **JSON ad-scenario** library used by CloudDeck (and reusable by other apps).

Same schema as DroidLink / voice_changer_ai. The kit owns **config + policy only** — no Firebase, no GoogleMobileAds. The host app still presents AdMob creatives.

```json
{
  "splashFirstOpenDelaySeconds": 10,
  "bannerMaxClicksPerDay": 2,
  "Open": [{ "name": "open_splash", "sdk": "admob", "id": "ca-app-pub-…/…", "isShow": true }],
  "Inter": [{ "name": "inter_before_rent", "id": "…", "isShow": true, "frequency": 4, "delay": 10 }],
  "Rewarded": [{ "name": "reward_continue", "id": "…", "isShow": true, "delay": 0 }],
  "Native": [{ "name": "native_home", "id": "…", "isShow": true, "isReload": true, "reloadIntervalSeconds": 15 }],
  "Banner": [{ "name": "banner_settings", "id": "…", "isShow": true, "isReload": true, "reloadAfterTime": 120 }]
}
```

See `examples/ads_config.example.json`.

## Policy (same as DroidLink)

| Rule | Default |
|---|---|
| Resume App Open gap | 60s |
| Interstitial min gap | 10s |
| `inter_before_rent` | every 4th + 10s delay |
| Leave Settings | interstitial every 3rd time |
| Banner clicks | 2 / day then hide |
| DEBUG | Google sample unit IDs, even if JSON has prod IDs |
| VIP | skip all display ads (`isVip`) |

Remote override is injected by the host (`AdsKit.applyRemoteJSON` / `AdsKit.applyRemoteJson`) from Firebase RC or your API. The kit does not talk to Firebase.

## iOS (Swift Package)

1. Add this folder as a local Swift package (`AppAdsKit`).
2. Ship `ads_config_ios.json` in the app bundle.
3. At launch:

```swift
import AppAdsKit

AdsKit.configure(
  AdsKit.Settings(
    storageKeyPrefix: "myapp.ads",
    useTestAds: true, // Debug
    isVip: { EntitlementStore.snapshot().isVip },
    units: AdUnitCatalog(
      appId: "ca-app-pub-…~…",
      appOpen: "ca-app-pub-…/…",
      interstitial: "…",
      rewarded: "…",
      banner: "…",
      native: "…"
    )
  )
)
AdsKit.loadBundled(resource: "ads_config_ios")
// Optional remote override:
// AdsKit.applyRemoteJSON(remoteString)
```

4. Gate UI with `AppAdManager.shouldShowNative(_:)` / `shouldShowBanner(_:)` and show interstitials after `AppAdManager.interstitialDecision(_:)`.

## Android (Gradle library)

1. Include the module:

```kotlin
// settings.gradle.kts
include(":app-ads-kit")
project(":app-ads-kit").projectDir = file("../packages/AppAdsKit/android")

// app/build.gradle.kts
implementation(project(":app-ads-kit"))
```

2. Ship `ads_config_android.json` in `src/main/assets`.
3. At launch:

```kotlin
AdsKit.configure(
  context,
  AdsKit.Settings(
    storageKeyPrefix = "myapp.ads",
    useTestAds = BuildConfig.DEBUG,
    isVip = { EntitlementStore.snapshot(context).isVip },
    units = AdUnitCatalog(
      appId = "ca-app-pub-…~…",
      appOpen = "…",
      interstitial = "…",
      rewarded = "…",
      banner = "…",
      native = "…",
    ),
  ),
)
AdsKit.loadFromAssets(context, "ads_config_android.json")
```

Copy this folder into another repo, or add it as a git submodule. Keep one JSON per platform so AdMob unit IDs stay separate.

## Host AdMob cache (copy with the kit)

AppAdsKit does **not** depend on GoogleMobileAds. The host still presents ads. Copy CloudDeck’s helpers (`NativeAds`, `InterstitialAdHelper`, `RewardAdHelper`) so inventory matches RemoteFlow:

1. **Shared cache** — one warm native / interstitial / rewarded for all placements on the same unit.
2. **Cold** — skeleton (native) or silent preload; first fill goes to UI + cache.
3. **Warm bind** — show cache **immediately**, fetch a replacement in the background; swap only when the new fill arrives.
4. **JSON timer** — native `isReload` / `reloadIntervalSeconds` refreshes the visible slot the same way (keep showing, then swap).
5. **No-fill never wipes** — destroy the previous unused creative only after a successful replacement. Never load/replace a fullscreen ad while it is on screen.
6. **Soft max age** — 55 minutes; still show while a refresh is in flight.

Do not add GoogleMobileAds to this kit. The next app copies the host helpers + this package.
