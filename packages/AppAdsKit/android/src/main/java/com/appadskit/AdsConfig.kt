package com.appadskit

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

/** Parses the shared ads JSON (bundled assets + optional remote override). */
object AdsConfig {
  @Volatile private var interByName: Map<String, FullscreenPlacement> = emptyMap()
  @Volatile private var openByName: Map<String, FullscreenPlacement> = emptyMap()
  @Volatile private var rewardedByName: Map<String, FullscreenPlacement> = emptyMap()
  @Volatile private var nativeByName: Map<String, NativePlacement> = emptyMap()
  @Volatile private var bannerByName: Map<String, BannerPlacement> = emptyMap()
  @Volatile var splashFirstOpenDelaySeconds: Int = 10
    private set
  @Volatile var bannerMaxClicksPerDay: Int = AdPolicy.DEFAULT_BANNER_MAX_CLICKS_PER_DAY
    private set
  @Volatile var isReady: Boolean = false
    private set

  fun loadFromAssets(context: Context, fileName: String) {
    val raw = context.assets.open(fileName).bufferedReader().use { it.readText() }
    applyJson(raw)
    isReady = true
  }

  fun applyRemoteJson(raw: String) {
    val trimmed = raw.trim()
    if (trimmed.isEmpty() || trimmed == "{}") return
    applyJson(trimmed)
  }

  fun interstitial(name: String): FullscreenPlacement =
    interByName[name] ?: FullscreenPlacement(name, AdsKit.resolvedUnits.interstitial, true, 1, 0)

  fun appOpen(name: String): FullscreenPlacement =
    openByName[name] ?: FullscreenPlacement(
      name,
      AdsKit.resolvedUnits.appOpen,
      name == AdPlacement.OPEN_SPLASH || name == AdPlacement.OPEN_RESUME,
      1,
      if (name == AdPlacement.OPEN_RESUME) 60 else 0,
    )

  fun rewarded(name: String): FullscreenPlacement =
    rewardedByName[name] ?: FullscreenPlacement(name, AdsKit.resolvedUnits.rewarded, true, 1, 0)

  fun native(name: String): NativePlacement =
    nativeByName[name] ?: NativePlacement(
      name,
      AdsKit.resolvedUnits.native,
      true,
      8,
      true,
      (AdPolicy.NATIVE_RELOAD_MS / 1000L).toInt(),
    )

  fun banner(name: String): BannerPlacement =
    bannerByName[name] ?: BannerPlacement(
      name,
      AdsKit.resolvedUnits.banner,
      true,
      true,
      (AdPolicy.BANNER_RELOAD_MS / 1000L).toInt(),
    )

  private fun applyJson(raw: String) {
    val root = JSONObject(raw)
    val units = AdsKit.resolvedUnits
    openByName = parseFullscreen(root.optJSONArray("Open"), units.appOpen)
    interByName = parseFullscreen(root.optJSONArray("Inter"), units.interstitial)
    rewardedByName = parseFullscreen(root.optJSONArray("Rewarded"), units.rewarded)
    nativeByName = parseNative(root.optJSONArray("Native"), units.native)
    bannerByName = parseBanner(root.optJSONArray("Banner"), units.banner)
    var delay = root.optInt("splashFirstOpenDelaySeconds", 10)
    if (delay == 10) delay = root.optInt("splashAppOpenDelaySeconds", 10)
    splashFirstOpenDelaySeconds = delay.coerceIn(0, 120)
    bannerMaxClicksPerDay = root.optInt("bannerMaxClicksPerDay", AdPolicy.DEFAULT_BANNER_MAX_CLICKS_PER_DAY).coerceAtLeast(0)
  }

  private fun parseFullscreen(arr: JSONArray?, fallback: String): Map<String, FullscreenPlacement> {
    if (arr == null) return emptyMap()
    val out = mutableMapOf<String, FullscreenPlacement>()
    for (i in 0 until arr.length()) {
      val item = arr.optJSONObject(i) ?: continue
      val name = item.optString("name").trim()
      if (name.isEmpty()) continue
      out[name] = FullscreenPlacement(
        name = name,
        adUnitId = AdsKit.resolveUnit(item.optString("id"), fallback),
        isShow = item.optBoolean("isShow", false),
        frequency = item.optInt("frequency", 1).coerceAtLeast(1),
        delaySeconds = item.optInt("delay", 0).coerceAtLeast(0),
      )
    }
    return out
  }

  private fun parseNative(arr: JSONArray?, fallback: String): Map<String, NativePlacement> {
    if (arr == null) return emptyMap()
    val out = mutableMapOf<String, NativePlacement>()
    for (i in 0 until arr.length()) {
      val item = arr.optJSONObject(i) ?: continue
      val name = item.optString("name").trim()
      if (name.isEmpty()) continue
      val reload = when {
        item.has("reloadIntervalSeconds") -> item.optInt("reloadIntervalSeconds", 15)
        else -> item.optInt("reloadAfterTime", 15)
      }
      out[name] = NativePlacement(
        name = name,
        adUnitId = AdsKit.resolveUnit(item.optString("id"), fallback),
        isShow = item.optBoolean("isShow", false),
        form = item.optInt("form", 8),
        isReload = item.optBoolean("isReload", true),
        reloadIntervalSeconds = reload.coerceIn(15, 600),
      )
    }
    return out
  }

  private fun parseBanner(arr: JSONArray?, fallback: String): Map<String, BannerPlacement> {
    if (arr == null) return emptyMap()
    val out = mutableMapOf<String, BannerPlacement>()
    for (i in 0 until arr.length()) {
      val item = arr.optJSONObject(i) ?: continue
      val name = item.optString("name").trim()
      if (name.isEmpty()) continue
      val reload = when {
        item.has("reloadAfterTime") -> item.optInt("reloadAfterTime", 120)
        else -> item.optInt("reloadIntervalSeconds", 120)
      }
      out[name] = BannerPlacement(
        name = name,
        adUnitId = AdsKit.resolveUnit(item.optString("id"), fallback),
        isShow = item.optBoolean("isShow", true),
        isReload = item.optBoolean("isReload", true),
        reloadAfterTimeSeconds = reload.coerceIn(15, 600),
      )
    }
    return out
  }
}
