package com.appadskit

/** eCPM-driven defaults (same numbers as DroidLink / iOS AppAdsKit). */
object AdPolicy {
  const val MIN_APP_OPEN_RESUME_MS = 60_000L
  const val MIN_INTERSTITIAL_MS = 10_000L
  const val SUPPORT_BACKS_BEFORE_INTER = 3
  const val BANNER_RELOAD_MS = 120_000L
  const val NATIVE_RELOAD_MS = 15_000L
  const val REWARD_COOLDOWN_MS = 0L
  const val DEFAULT_BANNER_MAX_CLICKS_PER_DAY = 2
}
