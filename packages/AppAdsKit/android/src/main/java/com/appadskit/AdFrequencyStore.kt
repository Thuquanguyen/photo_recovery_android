package com.appadskit

import android.content.Context
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object AdFrequencyStore {
  @Volatile private var appContext: Context? = null
  @Volatile private var prefsName = "appadskit.ads"

  private const val LAST_INTER = "last_inter_ms"
  private const val LAST_OPEN = "last_app_open_ms"
  private const val LAST_REWARD = "last_reward_ms"
  private const val COUNT_PREFIX = "inter_count_"
  private const val BANNER_CLICKS_DAY = "banner_clicks_day"
  private const val BANNER_CLICKS_COUNT = "banner_clicks_count"

  @Volatile var inviteInterShownThisProcess = false
  @Volatile var supportBackCountThisProcess = 0

  fun configure(context: Context, storageKeyPrefix: String) {
    appContext = context.applicationContext
    prefsName = storageKeyPrefix.ifBlank { "appadskit.ads" }
  }

  private fun prefs() =
    requireNotNull(appContext) { "AdsKit.configure() must run before AdFrequencyStore" }
      .getSharedPreferences(prefsName, Context.MODE_PRIVATE)

  fun lastInterstitialMs(): Long = prefs().getLong(LAST_INTER, 0L)

  fun markInterstitialShown(placement: String) {
    prefs().edit()
      .putLong(LAST_INTER, System.currentTimeMillis())
      .putInt(COUNT_PREFIX + placement, interstitialTriggerCount(placement) + 1)
      .apply()
  }

  fun interstitialTriggerCount(placement: String): Int =
    prefs().getInt(COUNT_PREFIX + placement, 0)

  fun bumpInterstitialTrigger(placement: String): Int {
    val next = interstitialTriggerCount(placement) + 1
    prefs().edit().putInt(COUNT_PREFIX + placement, next).apply()
    return next
  }

  fun canShowResumeAppOpen(minGapMs: Long = AdPolicy.MIN_APP_OPEN_RESUME_MS): Boolean {
    val last = prefs().getLong(LAST_OPEN, 0L)
    return last <= 0L || System.currentTimeMillis() - last >= minGapMs
  }

  fun markAppOpenShown() {
    prefs().edit().putLong(LAST_OPEN, System.currentTimeMillis()).apply()
  }

  fun canShowRewarded(): Boolean {
    if (AdPolicy.REWARD_COOLDOWN_MS <= 0L) return true
    val last = prefs().getLong(LAST_REWARD, 0L)
    return last <= 0L || System.currentTimeMillis() - last >= AdPolicy.REWARD_COOLDOWN_MS
  }

  fun markRewardedShown() {
    val now = System.currentTimeMillis()
    prefs().edit()
      .putLong(LAST_REWARD, now)
      .putLong(LAST_INTER, now)
      .apply()
  }

  fun bumpSupportBack(): Int {
    supportBackCountThisProcess += 1
    return supportBackCountThisProcess
  }

  fun canClickBanner(maxPerDay: Int): Boolean {
    rotateBannerDayIfNeeded()
    return prefs().getInt(BANNER_CLICKS_COUNT, 0) < maxOf(maxPerDay, 0)
  }

  fun recordBannerClick(maxPerDay: Int): Boolean {
    rotateBannerDayIfNeeded()
    val next = prefs().getInt(BANNER_CLICKS_COUNT, 0) + 1
    prefs().edit().putInt(BANNER_CLICKS_COUNT, next).apply()
    return next <= maxOf(maxPerDay, 0)
  }

  private fun rotateBannerDayIfNeeded() {
    val today = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
    val p = prefs()
    if (p.getString(BANNER_CLICKS_DAY, null) != today) {
      p.edit()
        .putString(BANNER_CLICKS_DAY, today)
        .putInt(BANNER_CLICKS_COUNT, 0)
        .apply()
    }
  }
}
